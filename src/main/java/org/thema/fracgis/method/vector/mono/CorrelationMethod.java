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
import java.util.Collection;
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
        final DefaultFeatureCoverage<DefaultFeature> pointCov = flattenPoints(getCoverage().getFeatures());
        final int [] tot = new int[getSampling().getValues().size()]; 
        
        SimpleParallelTask<DefaultFeature, int[]> task = new SimpleParallelTask<DefaultFeature, int[]>(pointCov.getFeatures(), monitor) {
            @Override
            protected int[] executeOne(DefaultFeature elem) {
                return calcOne(elem.getGeometry(), pointCov, getSampling());
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
            curve.put(val, tot[i++] / (double)pointCov.getFeatures().size());
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

    public static int[] calcOne(Geometry point, FeatureCoverage<? extends Feature> pointCov, DefaultSampling sampling) {
        int [] nb = new int[sampling.getValues().size()];       

        Envelope env = new Envelope(point.getCoordinate());
        env.expandBy(sampling.getRealMaxSize()/2);
        for(Feature f2 : pointCov.getFeatures(env)) {
            double dist = 2*point.getCoordinate().distance(f2.getGeometry().getCoordinate());
            if(dist >= sampling.getRealMaxSize()) {
                continue;
            }
            int ind = sampling.getCeilingScaleIndex(dist);
            nb[ind]++;
        }

        return nb;
    }
    
    public static DefaultFeatureCoverage<DefaultFeature> flattenPoints(Collection<Feature> pointFeatures) {
        List<DefaultFeature> points = new ArrayList<>();
        for(Feature f : pointFeatures) {
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
        
        return new DefaultFeatureCoverage<>(points);
    }
}
