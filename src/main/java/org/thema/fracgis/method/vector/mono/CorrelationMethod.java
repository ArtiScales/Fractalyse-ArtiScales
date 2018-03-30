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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.thema.common.ProgressBar;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.sampling.Sampling;
import org.thema.parallel.ExecutorService;
import org.thema.parallel.SimpleParallelTask;

/**
 * Calculates correlation dimension for vector point data.
 * The algorithm is based on Grassberger-Procaccia Algorithm.
 *
 * @author Gilles Vuidel
 */
public class CorrelationMethod extends MonoVectorMethod {
    

    /**
     * Creates a new correlation method for the given vector data
     * @param inputName the input data layer name 
     * @param sampling the scale sampling
     * @param coverage the input vector data, must be Point geometry
     */
    public CorrelationMethod(String inputName, DefaultSampling sampling, FeatureCoverage<Feature> coverage) {
        super(inputName, sampling, coverage);
    }

    /**
     * For parameter management only
     */
    public CorrelationMethod() {
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        List<DefaultFeature> points = new ArrayList<>();
        for(Feature f : getCoverage().getFeatures()) {
            Geometry geom = f.getGeometry();
            if(geom instanceof Point) {
                points.add(new DefaultFeature(points.size(), geom));
            } else if(geom instanceof MultiPoint) {
                for(int i = 0; i < geom.getNumGeometries(); i++) {
                    points.add(new DefaultFeature(points.size(), geom.getGeometryN(i)));
                }
            } else {
                throw new IllegalArgumentException("Correlation method supports point geometry only");
            }
        }
        
        final DefaultFeatureCoverage<DefaultFeature> pointCov = new DefaultFeatureCoverage<>(points);
        final int [] tot = new int[getSampling().getValues().size()]; 
        
        SimpleParallelTask<DefaultFeature, int[]> task = new SimpleParallelTask<DefaultFeature, int[]>(points) {
            @Override
            protected int[] executeOne(DefaultFeature elem) {
                int [] nb = new int[getSampling().getValues().size()];       

                Geometry point = elem.getGeometry();
                Envelope env = new Envelope(point.getCoordinate());
                env.expandBy(getSampling().getRealMaxSize()/2);
                for(Feature f2 : pointCov.getFeatures(env)) {
                    double dist = 2*point.getCoordinate().distance(f2.getGeometry().getCoordinate());
                    if(dist >= getSampling().getRealMaxSize()) {
                        continue;
                    }
                    if(dist < getSampling().getMinSize()) {
                        nb[0]++;
                    } else {
                        int ind;
                        if(getSampling().getSeq() == Sampling.Sequence.GEOM) {
                            ind = 1+(int)(Math.log(dist/getSampling().getMinSize()) / Math.log(getSampling().getCoef()));
                        } else {
                            ind = 1+(int)((dist-getSampling().getMinSize()) / getSampling().getCoef());
                        }
                        nb[ind]++;
                    }
                }
                
                return nb;
            }
            
            @Override
            public void gather(List<int[]> results) {
                for(int [] m : results) {
                    for(int i = 0; i < tot.length; i++) {
                        tot[i] += m[i];
                    }
                }
            }
        };
        
        if(parallel) {
            ExecutorService.execute(task);
        } else {
            ExecutorService.executeSequential(task);
        }
        
        curve = new TreeMap<>();
        for(int i = 1; i < tot.length; i++) {
            tot[i] += tot[i-1];
        }
        int i = 0;
        for(Double val : getSampling().getValues()) {
            curve.put(val, tot[i++] / (double)points.size());
        }
    }

    @Override
    public int getDimSign() {
        return 1;
    }
    
    @Override
    public String getName() {
        return "Correlation";
    }

    
}
