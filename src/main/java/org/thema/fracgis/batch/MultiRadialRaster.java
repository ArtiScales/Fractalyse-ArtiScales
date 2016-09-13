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


package org.thema.fracgis.batch;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.jfree.data.xy.XYSeries;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.swing.TaskMonitor;
import org.thema.fracgis.estimation.DirectEstimation;
import org.thema.fracgis.estimation.Estimation;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.method.raster.mono.RadialRasterMethod;
import org.thema.fracgis.sampling.RadialSampling;

/**
 * Performs a radial analysis for each black pixel of a binary raster.
 * The results are stored in several rasters.
 * 
 * @author Gilles Vuidel
 */
public class MultiRadialRaster {
    private RenderedImage img;
    private Rectangle2D envelope;
    private double maxSize;
    private boolean autoThreshold;
    private double minThreshold;
    private int indModel;
    private boolean confidenceInterval;

    /** results rasters */
    private WritableRaster rasterDim, rasterR2, rasterDistMax, rasterA, rasterDmin, rasterDmax, rasterDinter;
    
    /**
     * Creates a new MultiRadialRaster.
     * @param img the raster source
     * @param envelope the envelope of the raster source
     * @param maxSize the max size of the radial analysis
     * @param autoThreshold if true the max size is calculated for each analysis based on main inflexion point of the scaling behaviour
     * @param minThreshold the minimum max size. used only if autoThreshold == true
     * @param indModel index of the non linear model used for the estimation
     * @param confidenceInterval if true, calculates the confidence interval. slow down the calculation
     */
    public MultiRadialRaster(RenderedImage img, Rectangle2D envelope, double maxSize, boolean autoThreshold, 
            double minThreshold, int indModel, boolean confidenceInterval) {
        this.img = img;
        this.envelope = envelope;
        this.maxSize = maxSize;
        this.autoThreshold = autoThreshold;
        this.minThreshold = minThreshold;
        this.indModel = indModel;
        this.confidenceInterval = confidenceInterval;
    }
    
    /**
     * Perform the calculation.
     * The execution is parallelized by thread. Does not work with MPI.
     * @param mon the progression monitor
     * @throws CancellationException if user cancel the task
     */
    public void execute(ProgressBar progress) {
        rasterDim = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
        rasterR2 = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
        rasterA = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
        if(confidenceInterval) {
            rasterDinter = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
            rasterDmin = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
            rasterDmax = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
        }
        if(autoThreshold) {
            rasterDistMax = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
        }
        SimpleParallelTask task = new SimpleParallelTask.IterParallelTask(img.getHeight(), progress) {
            @Override
            protected void executeOne(Integer y) {
                RandomIter r = RandomIterFactory.create(img, null);
                for(int x = 0; x < img.getWidth(); x++) {
                    if(r.getSample(x, y, 0) == 1) {
                        Coordinate c = new Coordinate(x, y);
                        RadialRasterMethod method = new RadialRasterMethod("", new RadialSampling(c, maxSize/getResolution()), img, c);
                        method.execute(new TaskMonitor.EmptyMonitor(), false);
                        try {
                            DirectEstimation estim = (DirectEstimation) new EstimationFactory(method).getEstimation(EstimationFactory.Type.DIRECT, indModel);
                            if(autoThreshold) {
                                double max = getThreshold(estim);
                                estim.setRange(0, max);
                                rasterDistMax.setSample(x, y, 0, max*getResolution());
                            }
                            if(estim.getModel().hasParamA()) {
                                rasterA.setSample(x, y, 0, estim.getModel().getA(estim.getCoef()));
                            }
                            rasterDim.setSample(x, y, 0, estim.getDimension());
                            rasterR2.setSample(x, y, 0, estim.getR2());
                            if(confidenceInterval) {
                                double[] dInter = estim.getBootStrapConfidenceInterval();
                                rasterDinter.setSample(x, y, 0, dInter[1]-dInter[0]);
                                rasterDmin.setSample(x, y, 0, dInter[0]);
                                rasterDmax.setSample(x, y, 0, dInter[1]);
                            }
                        } catch(Exception ex) {
                            Logger.getLogger(MultiRadialRaster.class.getName()).log(Level.WARNING, null, ex);
                            rasterDim.setSample(x, y, 0, Float.NaN);
                            rasterR2.setSample(x, y, 0, Float.NaN);
                            rasterA.setSample(x, y, 0, Float.NaN);
                            if(confidenceInterval) {
                                rasterDinter.setSample(x, y, 0, Float.NaN);
                                rasterDmin.setSample(x, y, 0, Float.NaN);
                                rasterDmax.setSample(x, y, 0, Float.NaN);
                            }
                            if(autoThreshold) {
                                rasterDistMax.setSample(x, y, 0, Float.NaN);
                            }
                        }
                    } else {
                        rasterDim.setSample(x, y, 0, Float.NaN);
                        rasterR2.setSample(x, y, 0, Float.NaN);
                        rasterA.setSample(x, y, 0, Float.NaN);
                        if(confidenceInterval) {
                            rasterDinter.setSample(x, y, 0, Float.NaN);
                            rasterDmin.setSample(x, y, 0, Float.NaN);
                            rasterDmax.setSample(x, y, 0, Float.NaN);
                        }
                        if(autoThreshold) {
                            rasterDistMax.setSample(x, y, 0, Float.NaN);
                        }
                    }
                }
                r.done();
            }
        };
        
        new ParallelFExecutor(task).executeAndWait();
        
        if(task.isCanceled()) {
            throw new CancellationException();
        }
    }
    
    private double getResolution() {
        return envelope.getWidth() / img.getWidth();
    }

    /**
     * Returns the raster containing the fractal dimension estimated with radial analysis for each black pixel. 
     * White pixels contain NaN. When an error occured during the estimation of a black pixel, the pixel is set to NaN.
     * @return the raster containing the fractal dimension
     */
    public WritableRaster getRasterDim() {
        return rasterDim;
    }

    /**
     * Returns the raster containing the R2 of the regression for each black pixel. 
     * White pixels contain NaN. When an error occured during the estimation of a black pixel, the pixel is set to NaN.
     * @return the raster containing the R2
     */
    public WritableRaster getRasterR2() {
        return rasterR2;
    }
    
    /**
     * Returns the raster containing the a parameter of the regression for each black pixel. 
     * White pixels contain NaN. If the selected model does not contain the a parameter or when an error occured during the estimation of a black pixel, the pixel is set to NaN.
     * @return the raster containing the a parameter
     */
    public WritableRaster getRasterA() {
        return rasterA;
    }
    
    /**
     * Returns the raster containing the maximum of the confidence interval for each black pixel if confidenceInterval == true.
     * Returns null otherwise
     * White pixels contain NaN. When an error occured during the estimation of a black pixel, the pixel is set to NaN.
     * @return the raster containing the confidence interval maximum of the fractal dimension
     */
    public WritableRaster getRasterDmax() {
        return rasterDmax;
    }

    /**
     * Returns the raster containing the minimum of the confidence interval for each black pixel if confidenceInterval == true.
     * Returns null otherwise
     * White pixels contain NaN. When an error occured during the estimation of a black pixel, the pixel is set to NaN.
     * @return the raster containing the confidence interval minimum of the fractal dimension
     */
    public WritableRaster getRasterDmin() {
        return rasterDmin;
    }

    /**
     * Returns the raster containing the confidence interval (Dmax-Dmin) for each black pixel if confidenceInterval == true.
     * Returns null otherwise
     * White pixels contain NaN. When an error occured during the estimation of a black pixel, the pixel is set to NaN.
     * @return the raster containing the confidence interval (Dmax-Dmin) of the fractal dimension
     */
    public WritableRaster getRasterDinter() {
        return rasterDinter;
    }

    /**
     * Returns the raster containing max size used for each black pixel if autoThreshold == true.
     * Returns null otherwise
     * White pixels contain NaN. When an error occured during the estimation of a black pixel, the pixel is set to NaN.
     * @return the raster containing the fractal dimension
     */
    public WritableRaster getRasterDistMax() {
        return rasterDistMax;
    }

    /**
     * Calculates the max size from an estimation based on the main inflexion point of the scaling behaviour.
     * 
     * @param estim the radial estimation
     * @return the main inflexion point of the scaling behaviour or the max distance if no inflexion point is found
     */
    private double getThreshold(Estimation estim) {
        double bandwitdh = 0.05;
        final double inc = 0.05;
        int indMin = 0;
        double[] scx = estim.getScalingBehaviour()[0];
        while(indMin < scx.length && scx[indMin] < (minThreshold / getResolution())) {
            indMin++;
        }
        
        List<Integer> precPtInflex = null, ptInflex = estim.getInflexPointIndices(bandwitdh, indMin);
        while(ptInflex.size() > 1 && bandwitdh < 1) {
            bandwitdh += inc;
            precPtInflex = ptInflex;
            ptInflex = estim.getInflexPointIndices(bandwitdh, indMin);
        }
        
        if(ptInflex.isEmpty()) {
            if(precPtInflex != null) {
                return scx[precPtInflex.get(0)];
            } else {
                return scx[scx.length-1];
            }
        } else {
            if(ptInflex.size() == 1) {
                return scx[precPtInflex.get(0)];
            } else {
                return Double.NaN;
            }
        }
    }
}
