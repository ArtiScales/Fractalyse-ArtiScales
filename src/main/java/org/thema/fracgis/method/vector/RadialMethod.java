/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.method.vector;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.parallel.ProgressBar;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.param.XMLParams;
import org.thema.drawshape.feature.Feature;
import org.thema.drawshape.feature.FeatureCoverage;

/**
 *
 * @author gvuidel
 */
public class RadialMethod extends VectorMethod {

    @XMLParams.NoParam
    Coordinate centre = null;
    
    double minSize;
    double maxSize = 0;
    double stepSize = 1;
    int shape = BufferParameters.CAP_ROUND;
    
    /**
     * For parameter management only
     */
    public RadialMethod() {
        super();
    }
    
    public RadialMethod(String inputName, FeatureCoverage coverage, Coordinate centre, double min, double max, double step, int cap) {
        super(inputName, coverage);

        this.centre = centre;
        this.minSize = min;
        this.maxSize = max;
        this.stepSize = step;
        this.shape = cap;
        
        updateParams();
    }

    @Override
    protected final void updateParams() {
        if(centre == null)
            centre = getDefaultCentre(coverage.getEnvelope());
        if(maxSize == 0)
            maxSize = getDefaultMax(coverage.getEnvelope(), centre);
        
        if(minSize > maxSize)
            throw new IllegalArgumentException("Min is greater than max !");
        if(!coverage.getEnvelope().contains(centre))
            throw new IllegalArgumentException("Centre is outside !");
    }
    
    

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        curve = new TreeMap<Double, Double>();
        final Point p = new GeometryFactory().createPoint(centre);
        List<Double> radius = new ArrayList<Double>();
        for(double d = minSize/2; d <= maxSize/2; d+=stepSize/2)
            radius.add(d);
        SimpleParallelTask<Double> task = new SimpleParallelTask<Double>(radius, monitor) {
            @Override
            protected void executeOne(Double r) {
                Geometry buf = p.buffer(r, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, shape);
                List<Feature> features = coverage.getFeaturesIn(buf);
                double area = 0;
                for(Feature f : features)
                    area += f.getGeometry().intersection(buf).getArea();
                synchronized(RadialMethod.this) {
                    curve.put(2*r, area);
                }
            }
        };
        
        if(threaded)
            new ParallelFExecutor(task).executeAndWait();
        else
            new ParallelFExecutor(task, 1).executeAndWait();
        
        if(task.isCanceled())
            throw new CancellationException();
    }

    @Override
    public int getDimSign() {
        return 1;
    }

    @Override
    public String getParamsName() {
        return String.format(Locale.US, "cx%g_cy%g_min%g_max%g_step%g", centre!=null?centre.x:0.0, centre!=null?centre.y:0.0, minSize, maxSize, stepSize);
    }
    
    @Override
    public String getName() {
        return "Radial";
    }
    
    public static Coordinate getDefaultCentre(Envelope env) {
        return env.centre();
    }
    
    public static double getDefaultMax(Envelope env, Coordinate centre) {
        return 2*Math.min(Math.min(env.getMaxX() - centre.x, env.getMaxY() - centre.y), Math.min(centre.x - env.getMinX(), centre.y - env.getMinY())); 
    }

    
}
