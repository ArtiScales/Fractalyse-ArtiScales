/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.method.raster.mono;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.util.Locale;
import java.util.TreeMap;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.ProgressBar;
import org.thema.fracgis.estimation.RectangularRangeShape;
import org.thema.fracgis.method.MethodLayers;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;

/**
 *
 * @author gvuidel
 */
public class RadialRasterMethod extends RasterMethod implements MonoMethod {
    
    private Coordinate centre = null;
    private double maxSize;
    
    private TreeMap<Double, Double> curve;
    
    /**
     * Constructor for pixel unit centre and maxSize (no envelope supplied)
     * @param inputName
     * @param img
     * @param centre  in pixel image unit, cannot be null
     * @param maxSize in pixel image unit, must be > 0
     */
    public RadialRasterMethod(String inputName, RenderedImage img, Coordinate centre, double maxSize) {
        super(inputName, img, null);
        this.centre = centre;
        this.maxSize = maxSize;
    }
    
    /**
     * Constructor for image with envelope spatial unit
     * @param inputName cannot be null
     * @param img cannot be null
     * @param envelope cannot be null
     * @param centre in envelope unit can be null
     * @param maxSize in envelope unit can be <= 0
     */
    public RadialRasterMethod(String inputName, RenderedImage img, Envelope envelope, Coordinate centre, double maxSize) {
        super(inputName, img, envelope);
        if(centre == null)
            centre = getDefaultCentre(envelope);
        this.centre = centre;
        if(maxSize <= 0)
            maxSize = getDefaultMaxSize(envelope, centre);
        this.maxSize = maxSize;
    }
    
    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        Coordinate c = getTransform().transform(centre, new Coordinate());
        int x = (int) c.x;
        int y = (int) c.y;
        int n = (int)((maxSize / getResolution() - 1) / 2);
        int[] count = new int[n+1];
        
        final int i1 = y-n < 0 ? 0 : y-n;
        final int i2 = y+n >= img.getHeight() ? img.getHeight()-1 : y+n;
        final int j1 = x-n < 0 ? 0 : x-n;
        final int j2 = x+n >= img.getWidth() ? img.getWidth()-1 : x+n;
        monitor.setMaximum(i2-i1+1);
        RandomIter r = RandomIterFactory.create(img, null);
        for(int i = i1; i <= i2; i++) {
            for(int j = j1; j <= j2; j++)
                if(r.getSample(j, i, 0) == 1)
                    count[Math.max(Math.abs(i-y), Math.abs(j-x))]++;
            monitor.incProgress(1);
        }
        r.done();
        for(int i = 1; i < count.length; i++)
            count[i] += count[i-1];
        
        double res = getResolution();
        curve = new TreeMap<>();
        for(int i = 0; i < count.length; i++)
            curve.put((2*i+1) * res, count[i] * res*res);
    }

    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
    
    @Override
    public int getDimSign() {
        return 1;
    }
    
    @Override
    public String getName() {
        return "Radial";
    }

    @Override
    public String getParamsName() {
        return String.format(Locale.US, "cx%g_cy%g_max%g", centre!=null?centre.x:0.0, centre!=null?centre.y:0.0, maxSize);
    }

    @Override
    public MethodLayers getGroupLayer() {
        MethodLayers groupLayer = super.getGroupLayer(); 
        groupLayer.setScaleRangeShape(new RectangularRangeShape(new Point2D.Double(centre.x, centre.y), 0, maxSize));
        return groupLayer;
    }
    
    public static Coordinate getDefaultCentre(Envelope env) {
        return env.centre();
    }
    
    public static double getDefaultMaxSize(Envelope env, Coordinate centre) {
        return 2*Math.min(Math.min(env.getMaxX() - centre.x, env.getMaxY() - centre.y), Math.min(centre.x - env.getMinX(), centre.y - env.getMinY())); 
    }
}
