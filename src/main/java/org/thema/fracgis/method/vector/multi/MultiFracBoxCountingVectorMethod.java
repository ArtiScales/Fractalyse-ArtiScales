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


package org.thema.fracgis.method.vector.multi;

import org.thema.fracgis.method.QMonoMethod;
import org.thema.fracgis.method.MultiFracMethod;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Puntal;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.thema.common.ProgressBar;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.vector.VectorMethod;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.msca.SquareGrid;
import org.thema.msca.operation.SimpleCoverageOperation;

/**
 * Multifractal analysis with boxcounting and vector data.
 * 
 * @author Gilles Vuidel
 */
public class MultiFracBoxCountingVectorMethod extends VectorMethod implements MultiFracMethod {
    
    private HashMap<Double, List<SquareGrid>> grids;
    private double total;
    private int geomType; 
    
    private transient TreeMap<Double, TreeMap<Double, Double>> cacheCurves;
    
    /**
     * Creates a new multifractal box counting method for vector data.
     * @param inputName the input data layer name 
     * @param sampling the scale sampling
     * @param cover the input vector data
     */
    public MultiFracBoxCountingVectorMethod(String inputName, DefaultSampling sampling, FeatureCoverage cover) {
        super(inputName, sampling, cover);
        this.cacheCurves = new TreeMap<>();
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        total = 0;
        Geometry g = getCoverage().getFeatures().get(0).getGeometry();
        if(g instanceof Puntal) {
            geomType = 1;
        } else if(g instanceof Lineal) {
            geomType = 2;
        } else {
            geomType = 3;
        }  
        for(Feature f : getCoverage().getFeatures()) {
            switch(geomType) {
                case 1:
                    total += f.getGeometry().getNumGeometries();
                    break;
                case 2:
                    total += f.getGeometry().getLength();
                    break;
                default:
                    total += f.getGeometry().getArea();
            }
        }
        SortedSet<Double> sizes = getSampling().getValues();
        grids = new HashMap<>();
        Envelope env = new Envelope(getDataEnvelope());
        env.init(env.getMinX()-sizes.last()*1.001, env.getMaxX(), env.getMinY()-sizes.last()*1.001, env.getMaxY());
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
                    grid.addLayer("sum", DataBuffer.TYPE_FLOAT, 0.0);
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
                SimpleCoverageOperation op;
                switch(geomType) {
                    case 1:
                        op = new SimpleCoverageOperation(SimpleCoverageOperation.NBFEATURE, "sum", getCoverage());
                        break;
                    case 2:
                        op = new SimpleCoverageOperation(SimpleCoverageOperation.LENGTH, "sum", getCoverage());
                        break;
                    default:
                        op = new SimpleCoverageOperation(SimpleCoverageOperation.AREA, "sum", getCoverage());
                }
                
                op.setMonitor(monitor.getSubProgress(100.0/(grids.get(size).size())));
                if(parallel) {
                    grid.executeThreaded(op);
                } else {
                    grid.execute(op);
                }
            }

        }

    }

    @Override
    public int getDimSign() {
        return -1;
    }

    @Override
    public String getName() {
        return "MultiFractal";
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
        for(Double q : qSet) {
            if(!cacheCurves.containsKey(q)) {
                qList.add(q);
                cacheCurves.put(q, new TreeMap<Double, Double>());
            }
        }
        for(Double size : grids.keySet()) {
            double [] sum = new double[qList.size()];
            for(SquareGrid grid : grids.get(size)) {
                DataBuffer buf = grid.getRaster("sum").getDataBuffer();
                for(int j = 0; j < buf.getSize(); j++) {
                    float val = buf.getElemFloat(j);
                    if(val > 0) {
                        for(int k = 0; k < sum.length; k++) {
                            double q = qList.get(k);
                            if(q == 1) { // information dimension D1
                                sum[k] += val / total * Math.log(val / total);
                            } else {
                                sum[k] += Math.pow(val / total, q);
                            }
                        }
                    }
                }
            }
            for(int k = 0; k < sum.length; k++) {
                double q = qList.get(k);
                if(q == 1) { // information dimension D1
                    cacheCurves.get(q).put(size, Math.exp(sum[k]));
                } else {
                    cacheCurves.get(q).put(size, sum[k]);
                }
            }
        }  
    }
    
}
