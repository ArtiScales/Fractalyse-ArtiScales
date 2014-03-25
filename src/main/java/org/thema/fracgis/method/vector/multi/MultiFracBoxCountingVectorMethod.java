/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method.vector.multi;

import org.thema.fracgis.method.QMonoMethod;
import org.thema.fracgis.method.MultiFracMethod;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.thema.common.ProgressBar;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.vector.mono.BoxCountingMethod;
import org.thema.fracgis.method.vector.VectorMethod;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.SimpleCoverageOperation;

/**
 *
 * @author gvuidel
 */


/**
 * 
 * @author gvuidel
 */
public class MultiFracBoxCountingVectorMethod extends VectorMethod implements MultiFracMethod {

    private double minSize = 0;
    private double maxSize = 0;
    private double coef = 2;
    
    private TreeSet<Double> sizes;
    private HashMap<Double, List<SquareGrid>> grids;
    private double totalArea;
    
    private transient TreeMap<Double, TreeMap<Double, Double>> cacheCurves;
    
    public MultiFracBoxCountingVectorMethod(String inputName, FeatureCoverage cover, double min, double max, double coef) {
        super(inputName, cover);
        
        this.minSize = min;
        this.maxSize = max;
        this.coef = coef;
        
        updateParams();
        
        this.cacheCurves = new TreeMap<>();
    }
    
    @Override
    protected final void updateParams() {
        if(minSize == 0) minSize = BoxCountingMethod.getDefaultMin(coverage);
        if(maxSize == 0) maxSize = BoxCountingMethod.getDefaultMax(coverage);
        
        if(minSize > maxSize)
            throw new IllegalArgumentException("Min is greater than max !");
        
        sizes = new TreeSet<>();
        double val = minSize;
        while(val <= maxSize) {
            sizes.add(val);
            val *= coef;
        }
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        totalArea = 0;
        for(Feature f : coverage.getFeatures()) {
            totalArea += f.getGeometry().getArea();
        }
        
        grids = new HashMap<>();
        Envelope env = new Envelope(coverage.getEnvelope());
        env.init(env.getMinX()-sizes.last()*1.01, env.getMaxX(), env.getMinY()-sizes.last()*1.01, env.getMaxY());
        for(double size : sizes) {
            List<SquareGrid> gridSize = new ArrayList<>();
            int nx = (int)Math.ceil(Math.ceil(env.getWidth() / (double)size) / 40000.0);
            int ny = (int)Math.ceil(Math.ceil(env.getHeight() / (double)size) / 40000.0);
            int w = (int)Math.ceil((env.getWidth() / (double)nx) / size);
            int h = (int)Math.ceil((env.getHeight() / (double)ny) / size);
            Coordinate start = new Coordinate(env.getMinX(), env.getMinY());
            for(int x = 0; x < nx; x++) {
                start.y = env.getMinY();
                SquareGrid grid = null;
                for(int y = 0; y < ny; y++) {
                    grid = new SquareGrid(start, size, w, h);
                    grid.addLayer("area", DataBuffer.TYPE_FLOAT, 0.0);
                    start.y = grid.getEnvelope().getMaxY();
                    gridSize.add(grid);
                }
                start.x = grid.getEnvelope().getMaxX();
            }
            grids.put(size, gridSize);
        }

        monitor.setMaximum(sizes.size()*100);
        monitor.setProgress(0);
        for(double size : sizes) {
            monitor.setNote("Resolution : " + size);
            for(SquareGrid grid : grids.get(size)) {     
                SimpleCoverageOperation op = new SimpleCoverageOperation(SimpleCoverageOperation.AREA, "area", coverage);
                op.setMonitor(monitor.getSubProgress(100.0/(grids.get(size).size())));
                if(threaded)
                    grid.executeThreaded(op);
                else
                    grid.execute(op);
            }

        }

    }

    @Override
    public int getDimSign() {
        return -1;
    }

    public double getMax() {
        return sizes.last();
    }

    public double getMin() {
        return minSize;
    }

    @Override
    public String getName() {
        return "MultiFractal";
    }
    
    @Override
    public String getParamsName() {
        return String.format(Locale.US, "coef%g_min%g_max%g", coef, getMin(), getMax());
    }

    @Override
    public MonoMethod getSimpleMethod(final double q) {
        return new QMonoMethod(this, q);
    }
    
    @Override
    public synchronized TreeMap<Double, Double> getCurve(double q) {
        if(!cacheCurves.containsKey(q)) {
            calcCurves(Collections.singleton(q));
        }
        return cacheCurves.get(q);
    }
    
    @Override
    public TreeMap<Double, TreeMap<Double, Double>> getCurves(TreeSet<Double> qs) {
        calcCurves(qs);
        TreeMap<Double, TreeMap<Double, Double>> curves = new TreeMap<>();
        for(Double q : qs) {
            curves.put(q, cacheCurves.get(q));
        }
        return curves;
    }
    
    private void calcCurves(Set<Double> qSet) {
        List<Double> qList = new ArrayList<>();
        for(Double q : qSet)
            if(!cacheCurves.containsKey(q)) {
                qList.add(q);
                cacheCurves.put(q, new TreeMap<Double, Double>());
            }
        for(Double size : grids.keySet()) {
            double [] sum = new double[qList.size()];
            for(SquareGrid grid : grids.get(size)) {
                DataBuffer buf = grid.getRaster("area").getDataBuffer();
                for(int j = 0; j < buf.getSize(); j++) {
                    float val = buf.getElemFloat(j);
                    if(val > 0) {
                        for(int k = 0; k < sum.length; k++)
                            sum[k] += Math.pow(val / totalArea, qList.get(k));
                    }
                }
            }
            for(int k = 0; k < sum.length; k++)
                cacheCurves.get(qList.get(k)).put(size, sum[k]);
        }  
    }
    
}
