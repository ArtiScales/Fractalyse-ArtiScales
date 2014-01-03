/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.method.vector.mono;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import org.thema.common.parallel.BufferForkJoinTask;
import org.thema.common.parallel.ProgressBar;
import org.thema.common.param.XMLParams;
import org.thema.drawshape.feature.Feature;
import org.thema.drawshape.feature.FeatureCoverage;
import org.thema.drawshape.layer.GeometryLayer;
import org.thema.drawshape.style.SimpleStyle;
import org.thema.fracgis.method.MonoMethod;

/**
 *
 * @author gvuidel
 */
public class DilationMethod extends SimpleVectorMethod {

    private double minSize = 0;
    private double maxSize = 0;
    private double coef = 1.5;
    
    @XMLParams.NoParam
    TreeMap<Double, Double> clusters;
    
    @XMLParams.NoParam
    boolean keepBuffers = false;
    @XMLParams.NoParam
    TreeMap<Double, Geometry> bufGeoms;

    public DilationMethod(String inputName, FeatureCoverage<Feature> coverage, double minSize, double maxSize, double coef) {
        super(inputName, coverage);
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.coef = coef;
    }

    /**
     * For parameter management only
     */
    public DilationMethod() {
        super();
    }

    @Override
    protected void updateParams() {
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        double radius = minSize / 2;
        List<Geometry> geoms = new ArrayList<Geometry>();
        for(Feature f : coverage.getFeatures())
            geoms.add(f.getGeometry());
        Geometry geom = new GeometryFactory().buildGeometry(geoms);
        Geometry bufGeom = geom;
        curve = new TreeMap<Double, Double>();
        clusters = new TreeMap<Double, Double>();
        if(keepBuffers)
            bufGeoms = new TreeMap<Double, Geometry>();
        if(maxSize > 0)
            monitor.setMaximum(1+(int)((Math.log(maxSize) - Math.log(radius))/Math.log(coef)));
        else
            monitor.setMaximum(100);

        while(maxSize > 0 && radius*2 <= maxSize || maxSize <= 0 && bufGeom.getNumGeometries() > 1) {
            if(monitor.isCanceled())
                throw new CancellationException();
            monitor.setNote("Distance : " + (radius*2));
            if(threaded)
                bufGeom = BufferForkJoinTask.threadedBuffer(geom, radius);
            else
                bufGeom = BufferForkJoinTask.buffer(geom, radius, BufferParameters.DEFAULT_QUADRANT_SEGMENTS);
            double refArea = Math.PI*Math.pow(radius, 2);
            curve.put(2*radius, bufGeom.getArea() / refArea);
            clusters.put(2*radius, (double)bufGeom.getNumGeometries());
            
            if(maxSize <= 0)
                monitor.setProgress((int)(100 - 100*Math.log(bufGeom.getNumGeometries()) / Math.log(geoms.size())));
            else
                monitor.incProgress(1);
            
            if(keepBuffers)
                bufGeoms.put(2*radius, bufGeom);
            
            radius *= coef;
        }
        
        if(keepBuffers)
            for(Double dist : bufGeoms.keySet()) {
                GeometryLayer l = new GeometryLayer(String.format("%g", dist), bufGeoms.get(dist), new SimpleStyle(Color.BLACK, Color.BLACK));
                l.setVisible(false);
                getGroupLayer().addLayer(l);
            }
    }

    @Override
    public int getDimSign() {
        return -1;
    }
    
    public TreeMap<Double, Double> getClusters() {
        return clusters;
    }
    
    public double getMax() {
        if(curve.isEmpty())
            return maxSize;
        else
            return curve.lastKey();
    }

    public boolean isKeepBuffers() {
        return keepBuffers;
    }

    public void setKeepBuffers(boolean keepBuffers) {
        this.keepBuffers = keepBuffers;
    }
    
    @Override
    public String getParamsName() {
        return String.format(Locale.US, "coef%g_min%g_max%g", coef, minSize, getMax());
    }
    
    @Override
    public String getName() {
        return "Dilation";
    }

    
}
