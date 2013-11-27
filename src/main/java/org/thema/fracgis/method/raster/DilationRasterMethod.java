/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.method.raster;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.TreeMap;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.parallel.ProgressBar;

/**
 *
 * @author gvuidel
 */
public class DilationRasterMethod extends RasterMethod {

    int nStep;

    public DilationRasterMethod(String inputName, RenderedImage img, Envelope env, int nStep) {
        super(inputName, img, env);
        this.nStep = nStep;
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        curve = new TreeMap<Double, Double>();
        RandomIter src = RandomIterFactory.create(img, null);
        WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_BYTE, img.getWidth()+2*nStep, img.getHeight() + 2*nStep, 1, null);
        for(int y = 0; y < img.getHeight(); y++)
            for(int x = 0; x < img.getWidth(); x++)
                raster.setSample(x+nStep, y+nStep, 0, src.getSample(x, y, 0));
        
        monitor.setMaximum(nStep*raster.getHeight()*2);
        
        for(int i = 1; i <= nStep; i++) {
            int nb = 0;
            for(int y = 0; y < raster.getHeight(); y++) {
                for(int x = 0; x < raster.getWidth(); x++)
                    if(raster.getSample(x, y, 0) == 1) {
                        nb++;
                        if(raster.getSample(x-1, y-1, 0) == 0) raster.setSample(x-1, y-1, 0, 2);
                        if(raster.getSample(x-1, y, 0) == 0) raster.setSample(x-1, y, 0, 2);
                        if(raster.getSample(x-1, y+1, 0) == 0) raster.setSample(x-1, y+1, 0, 2);
                        if(raster.getSample(x, y-1, 0) == 0) raster.setSample(x, y-1, 0, 2);
                        if(raster.getSample(x, y+1, 0) == 0) raster.setSample(x, y+1, 0, 2);
                        if(raster.getSample(x+1, y-1, 0) == 0) raster.setSample(x+1, y-1, 0, 2);
                        if(raster.getSample(x+1, y, 0) == 0) raster.setSample(x+1, y, 0, 2);
                        if(raster.getSample(x+1, y+1, 0) == 0) raster.setSample(x+1, y+1, 0, 2);
                    }
                monitor.incProgress(1);
            }
            for(int y = 0; y < raster.getHeight(); y++) {
                for(int x = 0; x < raster.getWidth(); x++)
                    if(raster.getSample(x, y, 0) == 2) {
                        raster.setSample(x, y, 0, 1);
                        nb++;
                    }
                monitor.incProgress(1);
            }
            double res = getResolution();            
            curve.put(res*(2*i+1), nb / Math.pow(2*i+1, 2));
        }   
        
    }

    @Override
    public int getDimSign() {
        return -1;
    }

    @Override
    public String getName() {
        return "Dilation";
    }
     
    @Override
    public String getParamsName() {
        return "nstep" + nStep;
    }
    
}
