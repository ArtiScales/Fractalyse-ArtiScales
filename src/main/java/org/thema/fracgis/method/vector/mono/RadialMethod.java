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

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.ProgressBar;
import org.thema.common.parallel.SimpleParallelTask;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.estimation.RectangularRangeShape;
import org.thema.fracgis.method.MethodLayers;
import org.thema.fracgis.sampling.RadialSampling;

/**
 * Radial analysis on vector data.
 * 
 * @author Gilles Vuidel
 */
public class RadialMethod extends MonoVectorMethod {
    
    private int shape = BufferParameters.CAP_ROUND;    
    
    /**
     * Creates a new radial analysis for vector data.
     * @param inputName the input data layer name 
     * @param sampling the scale sampling
     * @param coverage the input vector data
     * @param cap specifies the buffer geometry that will be created. The styles provided are:
     *  - BufferOp.CAP_ROUND (default)
     *  - BufferOp.CAP_SQUARE
     */
    public RadialMethod(String inputName, RadialSampling sampling, FeatureCoverage coverage, int cap) {
        super(inputName, sampling, coverage);
        if(!coverage.getEnvelope().contains(sampling.getCentre())) {
            throw new IllegalArgumentException("Centre is outside !");
        }
        this.shape = cap;
    }
    
    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        curve = new TreeMap<>();
        
        final Point p = new GeometryFactory().createPoint(getCentre());
        List<Double> sizes = new ArrayList<>(getSampling().getValues());
        SimpleParallelTask<Double> task = new SimpleParallelTask<Double>(sizes, monitor) {
            @Override
            protected void executeOne(Double d) {
                Geometry buf = p.buffer(d/2, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, shape);
                List<Feature> features = getCoverage().getFeaturesIn(buf);
                double sum = 0;
                for(Feature f : features) {
                    if(f.getGeometry() instanceof Puntal) {
                        sum += f.getGeometry().intersection(buf).getNumGeometries();
                    } else if(f.getGeometry() instanceof Lineal) {
                        sum += f.getGeometry().intersection(buf).getLength();
                    } else {
                        sum += f.getGeometry().intersection(buf).getArea();
                    } 
                }
                synchronized(RadialMethod.this) {
                    curve.put(d, sum);
                }
            }
        };
        
        if(parallel) {
            new ParallelFExecutor(task).executeAndWait();
        } else {
            new ParallelFExecutor(task, 1).executeAndWait();
        }
        if(task.isCanceled()) {
            throw new CancellationException();
        }
    }
    
    @Override
    public int getDimSign() {
        return 1;
    }
    
    @Override
    public String getParamString() {
        return String.format(Locale.US, "cx%g_cy%g_", getCentre()!=null?getCentre().x:0.0, getCentre()!=null?getCentre().y:0.0) + super.getParamString();
    }
    
    @Override
    public String getName() {
        return "Radial";
    }
    
    @Override
    public MethodLayers getGroupLayer() {
        MethodLayers groupLayer = super.getGroupLayer(); 
        RectangularRangeShape rangeShape = new RectangularRangeShape(new Point2D.Double(getCentre().x, getCentre().y), 0, getSampling().getMaxSize());
        rangeShape.setShape(new Ellipse2D.Double());
        groupLayer.setScaleRangeShape(rangeShape);
        return groupLayer;
    }
    
    public Coordinate getCentre() {
        return ((RadialSampling)getSampling()).getCentre();
    }
    
}
