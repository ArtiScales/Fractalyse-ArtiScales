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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.thema.common.ProgressBar;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.vector.VectorMethod;
import org.thema.fracgis.method.vector.mono.CorrelationMethod;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.parallel.ExecutorService;
import org.thema.parallel.SimpleParallelTask;

/**
 * Multifractal analysis with correlation and vector data.
 * 
 * @author Gilles Vuidel
 */
public class MultiFracCorrelationVectorMethod extends VectorMethod implements MultiFracMethod {
    
    private int nbPoints;
    private double maxSize;
    private transient List<int[]> count;
    private transient TreeMap<Double, TreeMap<Double, Double>> cacheCurves;
    
    /**
     * Creates a new multifractal box counting method for vector data.
     * @param inputName the input data layer name 
     * @param sampling the scale sampling
     * @param cover the input vector data
     */
    public MultiFracCorrelationVectorMethod(String inputName, DefaultSampling sampling, FeatureCoverage cover) {
        super(inputName, sampling, cover);
        this.cacheCurves = new TreeMap<>();
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        final DefaultFeatureCoverage<DefaultFeature> pointCov = CorrelationMethod.flattenPoints(getCoverage().getFeatures());
        nbPoints = pointCov.getFeatures().size();
        maxSize = Math.sqrt(Math.pow(getCoverage().getEnvelope().getWidth(), 2) + Math.pow(getCoverage().getEnvelope().getHeight(), 2));
        count = new ArrayList<>();
        
        SimpleParallelTask<DefaultFeature, int[]> task = new SimpleParallelTask<DefaultFeature, int[]>(pointCov.getFeatures(), monitor) {
            @Override
            protected int[] executeOne(DefaultFeature elem) {
                int [] nb = CorrelationMethod.calcOne(elem.getGeometry(), pointCov, getSampling());
                for(int i = 1; i < nb.length; i++) {
                    nb[i] += nb[i-1];
                }
                return nb;
            }
            
            @Override
            public void gather(List<int[]> results) {
                count.addAll(results);
            }
        };

        if(parallel) {
            ExecutorService.execute(task);
        } else {
            ExecutorService.executeSequential(task);
        }
        
    }

    @Override
    public int getDimSign() {
        return -1;
    }

    @Override
    public String getName() {
        return "MultiFractal correlation";
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
        int i = 0;
        for(double size : getSampling().getValues()) {
            double [] sum = new double[qList.size()];
            for(int [] nb : count) {
                double val = nb[i];
                
                for(int k = 0; k < sum.length; k++) {
                    double q = qList.get(k);
                    if(q == 1) { // information dimension D1
                        sum[k] += val / nbPoints * Math.log(val / nbPoints);
                    } else {
                        sum[k] += Math.pow(val / nbPoints, q-1);
                    }
                }


                for(int k = 0; k < sum.length; k++) {
                    double q = qList.get(k);
                    if(q == 1) { // information dimension D1
                        cacheCurves.get(q).put(size / maxSize, Math.exp(sum[k]));
                    } else {
                        cacheCurves.get(q).put(size / maxSize, sum[k] / nbPoints);
                    }
                }
            }  
            i++;
        }
    }
    
}
