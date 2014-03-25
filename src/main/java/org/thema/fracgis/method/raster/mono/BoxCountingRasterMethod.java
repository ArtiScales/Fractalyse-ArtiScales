/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method.raster.mono;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.ProgressBar;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;

/**
 *
 * @author gvuidel
 */
public class BoxCountingRasterMethod extends RasterMethod implements MonoMethod {
    
    private final double coef;
    private final double maxSize;
    
    private TreeMap<Double, Double> curve;

    public BoxCountingRasterMethod(String inputName, RenderedImage img, Envelope env, double coef, double maxSize) {
        super(inputName, img, env);
        this.coef = coef;
        
        if(maxSize <= 0)
            maxSize = getDefaultMaxSize(img, getResolution());
        this.maxSize = maxSize;
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        monitor.setMaximum(img.getHeight());
        int n = (int) Math.ceil(Math.log(maxSize/getResolution()) / Math.log(coef));
        List<WritableRaster> rasters = new ArrayList<WritableRaster>();
        for(int i = 1; i < n; i++)
            rasters.add(Raster.createBandedRaster(DataBuffer.TYPE_BYTE,
                    (int)Math.ceil(img.getWidth() / Math.pow(coef, i)),
                    (int)Math.ceil(img.getHeight() / Math.pow(coef, i)), 1, null));

        int nb = 0;
        RandomIter iter = RandomIterFactory.create(img, null);
        
        for(int y = 0; y < img.getHeight(); y++) {
            for(int x = 0; x < img.getWidth(); x++)
                if(iter.getSample(x, y, 0) == 1) {
                    for(int i = 1; i < n; i++)
                        rasters.get(i-1).setSample((int)(x / Math.pow(coef, i)),
                                (int)(y / Math.pow(coef, i)), 0, 1);
                    nb++;
                }
            monitor.setProgress(y+1);
        }

        curve = new TreeMap<Double, Double>();
        curve.put(getResolution(), (double)nb);
        for(int i = 1; i < n; i++) {
            DataBuffer buf = rasters.get(i-1).getDataBuffer();
            nb = 0;
            for(int j = 0; j < buf.getSize(); j++)
                if(buf.getElem(j) == 1)
                    nb++;
            curve.put(getResolution()*Math.pow(coef, i), (double)nb);

        }
    }

    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
    
    @Override
    public int getDimSign() {
        return -1;
    }
    
    @Override
    public String getParamsName() {
        return String.format(Locale.US, "coef%g_min%g_max%g", coef, getMinSize(), getMaxSize());
    }

    public double getMinSize() {
        return getResolution();
    }
    
    public double getMaxSize() {
        return curve.lastKey();
    }
    
    @Override
    public String getName() {
        return "Boxcounting";
    }
    
    public static double getDefaultMaxSize(RenderedImage img, double resolution) {
        return resolution * Math.min(img.getWidth(), img.getHeight())/2;
    }
}
