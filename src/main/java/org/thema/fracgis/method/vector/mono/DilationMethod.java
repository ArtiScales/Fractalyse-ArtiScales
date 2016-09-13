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
import org.thema.common.ProgressBar;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.drawshape.layer.GeometryLayer;
import org.thema.drawshape.style.SimpleStyle;
import org.thema.fracgis.sampling.DefaultSampling;

/**
 * Computes unifractal dimension by successive dilation.
 *
 * @author Gilles Vuidel
 */
public class DilationMethod extends MonoVectorMethod {
    
    private boolean stopOne;
    
    @ReflectObject.NoParam
    private TreeMap<Double, Double> clusters;
    
    @ReflectObject.NoParam
    private boolean keepBuffers = false;
    @ReflectObject.NoParam
    private TreeMap<Double, Geometry> bufGeoms;

    /**
     * Creates a new dilation method for the given vector data
     * @param inputName the input data layer name 
     * @param sampling the scale sampling
     * @param coverage the input vector data
     * @param stopOne stop when it remains only one cluster ?
     * @param keepBuffers keep buffers for displaying it ?
     */
    public DilationMethod(String inputName, DefaultSampling sampling, FeatureCoverage<Feature> coverage, boolean stopOne, boolean keepBuffers) {
        super(inputName, sampling, coverage);
        this.stopOne = stopOne;
        this.keepBuffers = keepBuffers;
    }

    /**
     * For parameter management only
     */
    public DilationMethod() {
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        double radius = getSampling().getMinSize() / 2;
        List<Geometry> geoms = new ArrayList<>();
        for(Feature f : getCoverage().getFeatures()) {
            geoms.add(f.getGeometry());
        }
        Geometry geom = new GeometryFactory().buildGeometry(geoms);
        Geometry bufGeom = geom;
        curve = new TreeMap<>();
        clusters = new TreeMap<>();
        if(keepBuffers) {
            bufGeoms = new TreeMap<>();
        }
        if(!stopOne) {
            monitor.setMaximum(getSampling().getValues().size());
        } else {
            monitor.setMaximum(100);
        }
        while(!stopOne && radius*2 <= getSampling().getMaxSize() || stopOne && bufGeom.getNumGeometries() > 1) {
            if(monitor.isCanceled()) {
                throw new CancellationException();
            }
            monitor.setNote("Distance : " + (radius*2));
            if(parallel) {
                bufGeom = BufferForkJoinTask.threadedBuffer(geom, radius);
            } else {
                bufGeom = BufferForkJoinTask.buffer(geom, radius, BufferParameters.DEFAULT_QUADRANT_SEGMENTS);
            }
            double refArea = Math.PI*Math.pow(radius, 2);
            curve.put(2*radius, bufGeom.getArea() / refArea);
            clusters.put(2*radius, (double)bufGeom.getNumGeometries());
            
            monitor.incProgress(1);
            
            if(keepBuffers) {
                bufGeoms.put(2*radius, bufGeom);
            }
            
            radius = getSampling().getNext(radius*2) / 2;
        }
        
        if(keepBuffers) {
            for(Double dist : bufGeoms.keySet()) {
                GeometryLayer l = new GeometryLayer(String.format("%g", dist), bufGeoms.get(dist), new SimpleStyle(Color.BLACK, Color.BLACK));
                l.setVisible(false);
                getGroupLayer().addLayerFirst(l);
            }
        }
    }

    @Override
    public int getDimSign() {
        return -1;
    }
    
    /**
     * The method {@link #execute(org.thema.common.ProgressBar, boolean) } must be called before.
     * @return the number of clusters for each step of dilation
     */
    public TreeMap<Double, Double> getClusters() {
        return clusters;
    }
    
    @Override
    public String getParamString() {
        if(stopOne) {
            return String.format(Locale.US, "coef%g_min%g_stop1", getSampling().getCoef(), getSampling().getMinSize());
        } else {
            return super.getParamString();
        }
    }
    
    @Override
    public String getName() {
        return "Dilation";
    }

    
}
