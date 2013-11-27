/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.batch;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.ProgressBar;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.parallel.TaskMonitor;
import org.thema.fracgis.estimation.Estimation;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.method.raster.RadialRasterMethod;

/**
 *
 * @author gvuidel
 */
public class MultiRadialRaster {
    RenderedImage img;
    Rectangle2D envelope;
    double maxSize;
    boolean confidenceInterval;

    // results rasters
    WritableRaster rasterDim, rasterR2, rasterDmin, rasterDmax, rasterDinter;
    
    public MultiRadialRaster(RenderedImage img, Rectangle2D envelope, double maxSize, boolean confidenceInterval) {
        this.img = img;
        this.envelope = envelope;
        this.maxSize = maxSize;
        this.confidenceInterval = confidenceInterval;
    }
    
    public void execute(ProgressBar progress) {
        rasterDim = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
        rasterR2 = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
        if(confidenceInterval) {
            rasterDinter = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
            rasterDmin = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
            rasterDmax = Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT, img.getWidth(), img.getHeight(), 1, img.getWidth(), new int[1]), null);
        }
        
        SimpleParallelTask task = new SimpleParallelTask.IterParallelTask(img.getHeight(), progress) {
            @Override
            protected void executeOne(Integer y) {
                RandomIter r = RandomIterFactory.create(img, null);
                for(int x = 0; x < img.getWidth(); x++)
                    if(r.getSample(x, y, 0) == 1) {
                        Coordinate c = new Coordinate(x, y);
                        RadialRasterMethod method = new RadialRasterMethod("", img, c, maxSize*img.getWidth()/envelope.getWidth());
                        method.execute(new TaskMonitor.EmptyMonitor(), false);
                        try {
                            Estimation estim = new EstimationFactory(method).getEstimation(EstimationFactory.Type.DIRECT, 3);
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
                            if(confidenceInterval) {
                                rasterDinter.setSample(x, y, 0, Float.NaN);
                                rasterDmin.setSample(x, y, 0, Float.NaN);
                                rasterDmax.setSample(x, y, 0, Float.NaN);
                            }
                        }
                    } else {
                        rasterDim.setSample(x, y, 0, -99);
                        rasterR2.setSample(x, y, 0, -99);
                        if(confidenceInterval) {
                            rasterDinter.setSample(x, y, 0, -99);
                            rasterDmin.setSample(x, y, 0, -99);
                            rasterDmax.setSample(x, y, 0, -99);
                        }
                    }
                r.done();
            }
        };
        
        new ParallelFExecutor(task).executeAndWait();
        
    }

    public WritableRaster getRasterDim() {
        return rasterDim;
    }

    public WritableRaster getRasterDmax() {
        return rasterDmax;
    }

    public WritableRaster getRasterDmin() {
        return rasterDmin;
    }

    public WritableRaster getRasterR2() {
        return rasterR2;
    }

    public WritableRaster getRasterDinter() {
        return rasterDinter;
    }
    
    
}
