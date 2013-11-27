/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method.raster;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import org.thema.common.parallel.AbstractParallelFTask;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.ProgressBar;

/**
 *
 * @author gvuidel
 */


public class CorrelationMethod extends RasterMethod {

    public static class CorrelationTask extends AbstractParallelFTask<TreeMap<Double, Double>, double[]>
            implements Serializable {
        Raster raster;
        double resolution;
        int dMax;

        private TreeMap<Double, Double> curve;

        public CorrelationTask(ProgressBar monitor, RenderedImage img, double resolution, double maxSize) {
            super(monitor);
            this.dMax = (int)((maxSize / resolution -1) / 2);
            this.resolution = resolution;
            raster = img.getData();
        }

        @Override
        protected double[] execute(int start, int end) {

            if(raster.getSampleModel().getDataType() != DataBuffer.TYPE_BYTE || raster.getSampleModel().getSampleSize()[0] != 8)
                return slowMethod(raster, start, end);

            final byte [] buf = ((DataBufferByte)raster.getDataBuffer()).getData();

            final int w = raster.getWidth();
            final int h = raster.getHeight();
            final double [] fy = new double[dMax+1];


            for(int i = start; i < end; i++) {
                for(int j = 0; j < w; j++)
                    if(raster.getSample(j, i, 0) == 1) {
                        if(isCanceled())
                            return null;

                        final int i1 = i-dMax < 0 ? 0 : i-dMax;
                        final int i2 = i+dMax >= h ? h-1 : i+dMax;

                        final int j1 = j-dMax < 0 ? 0 : j-dMax;
                        final int j2 = j+dMax >= w ? w-1 : j+dMax;


                        for(int k = i1; k <= i2; k++)
                        {
                            int ind = (k) * w + (j1);
                            for(int l = j1; l <= j2; l++)
                            {
                                if(buf[ind] == 1)
                                    fy[Math.max(Math.abs(k-i), Math.abs(l-j))]++;
                                ind++;
                            }
                        }

                }
                incProgress(1);
            }

            return fy;
        }

        private double[] slowMethod(Raster r, int start, int end) {
            final int w = r.getWidth();
            final int h = r.getHeight();

            final double [] fy = new double[dMax+1];


            for(int i = start; i < end; i++) {
                for(int j = 0; j < w; j++)
                    if(r.getSample(j, i, 0) == 1) {
                        if(isCanceled())
                            return null;

                        final int i1 = i-dMax < 0 ? 0 : i-dMax;
                        final int i2 = i+dMax >= h ? h-1 : i+dMax;

                        final int j1 = j-dMax < 0 ? 0 : j-dMax;
                        final int j2 = j+dMax >= w ? w-1 : j+dMax;

                        for(int k = i1; k <= i2; k++)
                            for(int l = j1; l <= j2; l++)
                                if(r.getSample(l, k, 0) == 1)
                                    fy[Math.max(Math.abs(k-i), Math.abs(l-j))]++;


                }
                incProgress(1);
            }

            return fy;
        }


        public int getSplitRange() {
            return raster.getHeight();
        }

        public void finish(Collection results) {
            double [] fy = new double[dMax+1];
            Arrays.fill(fy, 0.0);

            for(Object o : results)
                for(int i = 0; i <= dMax; i++)
                    fy[i] += ((double [])o)[i];

            
            int nbTot = 0;
            for(int i = 0; i < raster.getHeight(); i++)
                for(int j = 0; j < raster.getWidth(); j++)
                    if(raster.getSample(j, i, 0) == 1)
                        nbTot++;

            System.out.println("Nb point : " + nbTot);
            
            for(int i = 0; i <= dMax; i++)
            {
                fy[i] /= (double)nbTot;		/* on divise fy par le nombre total de points */
                if(i > 0)
                    fy[i] += fy[i-1];		/* et on fait le cumul */
            }

            curve = new TreeMap<Double, Double>();
            for(int i = 0; i <= dMax; i++)
                curve.put((2*i+1.0)*resolution, fy[i]*resolution*resolution);
        }

        public TreeMap<Double, Double> getResult() {
            return curve;
        }

    }

    double maxSize;

    public CorrelationMethod(String inputName, RenderedImage img, Envelope env) {
        this(inputName, img, env, getDefaultMax(env));
    }

    public CorrelationMethod(String inputName, RenderedImage img, Envelope env, double maxSize) {
        super(inputName, img, env);
        if(maxSize <= 0)
            maxSize = getDefaultMax(env);
        this.maxSize = maxSize;
    }

    public double getMaxSize() {
        return maxSize;
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        CorrelationTask task = new CorrelationTask(monitor, img, getResolution(), maxSize);
        
        if(threaded)
            new ParallelFExecutor(task).executeAndWait();
        else
            new ParallelFExecutor(task, 1).executeAndWait();

        if(task.isCanceled())
            throw new CancellationException();
        curve = task.getResult();
    }

    @Override
    public int getDimSign() {
        return 1;
    }

    @Override
    public String getParamsName() {
        return String.format(Locale.US, "max%g", maxSize);
    }
    
    @Override
    public String getName() {
        return "Correlation";
    }
    
    public static double getDefaultMax(Envelope env) {
        return 2 * Math.min(env.getWidth(), env.getHeight()) / 8;
    }

}
