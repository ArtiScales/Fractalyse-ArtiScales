/*
 * Copyright (C) 2016 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.thema.fracgis.method.raster.mono;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import org.thema.common.ProgressBar;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.parallel.AbstractParallelTask;
import org.thema.parallel.ExecutorService;

/**
 * Calculates correlation dimension for binary raster data.
 * The algorithm is based on Grassberger-Procaccia Algorithm.
 * @author Gilles Vuidel
 */

public class CorrelationRasterMethod extends MonoRasterMethod {
    /**
     * Parallel task for computing correlation.
     * This task can be run in threaded mode or MPI.
     */
    private static class CorrelationTask extends AbstractParallelTask<TreeMap<Double, Double>, double[]>
            implements Serializable {
        private Raster raster;
        private double resolution;
        private int dMax;
        private DefaultSampling sampling;
        private double [] sumY;
        private TreeMap<Double, Double> curve;

        private CorrelationTask(ProgressBar monitor, DefaultSampling sampling, RenderedImage img, double resolution) {
            super(monitor);
            this.sampling = sampling;
            dMax = sampling.getDiscreteValues().last();
            this.resolution = resolution;
            raster = img.getData();
        }

        @Override
        public void init() {
            super.init(); 
            sumY = new double[dMax+1];
            Arrays.fill(sumY, 0.0);
        }

        @Override
        public double[] execute(int start, int end) {

            if(raster.getSampleModel().getDataType() != DataBuffer.TYPE_BYTE || raster.getSampleModel().getSampleSize()[0] != 8) {
                return slowMethod(raster, start, end);
            }
            final byte [] buf = ((DataBufferByte)raster.getDataBuffer()).getData();

            final int w = raster.getWidth();
            final int h = raster.getHeight();
            final double [] fy = new double[dMax+1];

            for(int i = start; i < end; i++) {
                for(int j = 0; j < w; j++) {
                    if(raster.getSample(j, i, 0) == 1) {
                        if(isCanceled()) {
                            return null;
                        }
                        final int i1 = i-dMax < 0 ? 0 : i-dMax;
                        final int i2 = i+dMax >= h ? h-1 : i+dMax;

                        final int j1 = j-dMax < 0 ? 0 : j-dMax;
                        final int j2 = j+dMax >= w ? w-1 : j+dMax;

                        for(int k = i1; k <= i2; k++) {
                            int ind = (k) * w + (j1);
                            for(int l = j1; l <= j2; l++) {
                                if(buf[ind] == 1) {
                                    fy[Math.max(Math.abs(k-i), Math.abs(l-j))]++;
                                }
                                ind++;
                            }
                        }
                    }

                }
                incProgress(1);
            }

            return fy;
        }

        /**
         * Slower method for raster which pixels are not on 1 byte (8 bits)
         * @param r
         * @param start
         * @param end
         * @return 
         */
        private double[] slowMethod(Raster r, int start, int end) {
            final int w = r.getWidth();
            final int h = r.getHeight();

            final double [] fy = new double[dMax+1];

            for(int i = start; i < end; i++) {
                for(int j = 0; j < w; j++) {
                    if(r.getSample(j, i, 0) == 1) {
                        if(isCanceled()) {
                            return null;
                        }
                        final int i1 = i-dMax < 0 ? 0 : i-dMax;
                        final int i2 = i+dMax >= h ? h-1 : i+dMax;

                        final int j1 = j-dMax < 0 ? 0 : j-dMax;
                        final int j2 = j+dMax >= w ? w-1 : j+dMax;

                        for(int k = i1; k <= i2; k++) {
                            for(int l = j1; l <= j2; l++) {
                                if(r.getSample(l, k, 0) == 1) {
                                    fy[Math.max(Math.abs(k-i), Math.abs(l-j))]++;
                                }
                            }
                        }
                    }
                }
                incProgress(1);
            }

            return fy;
        }


        @Override
        public int getSplitRange() {
            return raster.getHeight();
        }
        
        @Override
        public void gather(double[] result) {
            for(int i = 0; i <= dMax; i++) {
                sumY[i] += result[i];
            }
        }
        
        @Override
        public void finish() {           
            int nbTot = 0;
            for(int i = 0; i < raster.getHeight(); i++) {
                for(int j = 0; j < raster.getWidth(); j++) {
                    if(raster.getSample(j, i, 0) == 1) {
                        nbTot++;
                    }
                }
            }
            
            for(int i = 0; i <= dMax; i++) {
                sumY[i] /= (double)nbTot;		/* on divise fy par le nombre total de points */
                if(i > 0) {
                    sumY[i] += sumY[i-1];		/* et on fait le cumul */
                }
            }
            SortedSet<Integer> scales = sampling.getDiscreteValues();
            curve = new TreeMap<>();
            for(int i = 0; i <= dMax; i++) {
                if(scales.contains(i)) {
                    curve.put((2*i+1.0)*resolution, sumY[i]*resolution*resolution);
                }
            }
        }

        @Override
        public TreeMap<Double, Double> getResult() {
            return curve;
        }

    }

    /**
     * Default constructor for batch mode
     */
    public CorrelationRasterMethod() {
    }

    /**
     * Creates a new correlation method for the given data
     * @param inputName input layer name (must be a binary raster layer)
     * @param sampling scale sampling
     * @param img raster input data
     * @param env envelope of the raster in world coordinate
     */
    public CorrelationRasterMethod(String inputName, DefaultSampling scaling, RenderedImage img, Envelope env) {
        super(inputName, scaling, img, env);
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        CorrelationTask task = new CorrelationTask(monitor, getSampling(), getImg(), getResolution());
        
        if(parallel) {
            ExecutorService.execute(task);
        } else {
            ExecutorService.executeSequential(task);
        }
        if(task.isCanceled()) {
            throw new CancellationException();
        }
        curve = task.getResult();
    }
    
    @Override
    public int getDimSign() {
        return 1;
    }
    
    @Override
    public String getName() {
        return "Correlation";
    }

}
