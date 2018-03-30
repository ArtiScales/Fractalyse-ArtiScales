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


package org.thema.fracgis.method.raster.multi;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBufferDouble;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.thema.common.ProgressBar;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.MultiFracMethod;
import org.thema.fracgis.method.QMonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;
import org.thema.fracgis.sampling.DefaultSampling;

/**
 * Multifractal analysis based on WTMM method with CWT
 *
 * @author Gilles Vuidel
 */
public class WTMMMethod extends RasterMethod implements MultiFracMethod {

    private TreeMap<Double, List<Double>> modes;
    private transient TreeMap<Double, TreeMap<Double, Double>> cacheCurves;
    
    /**
     * Creates a new multifractal wavelet method for the given data
     * @param inputName input layer name
     * @param sampling scale sampling
     * @param img raster input data
     * @param env envelope of the raster in world coordinate, may be null
     */
    public WTMMMethod(String inputName, DefaultSampling sampling, RenderedImage img, Envelope envelope) {
        super(inputName, sampling, img, envelope);
        this.cacheCurves = new TreeMap<>();
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        modes = new TreeMap<>();
        for(double var : getSampling().getValues()) {
            CWT cwt = new CWT();
            cwt.calcCWT(getImg().getData(), (int)var);
            
            List<Double> m = new ArrayList<>();
            double[] data = ((DataBufferDouble)cwt.chain.getDataBuffer()).getData();
            for(double val : data) {
                if(val != 0) {
                    m.add(val);
                }
            }
            modes.put(var/getImg().getWidth(), m);
        }
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
        for(Double x : modes.keySet()) {
            List<Double> m = modes.get(x);
            double [] sum = new double[qList.size()];
            for(double val : m)  {
                for(int k = 0; k < sum.length; k++) {
                    sum[k] += Math.pow(val, qList.get(k));
                }
            }
            for(int k = 0; k < sum.length; k++) {
                cacheCurves.get(qList.get(k)).put(x, sum[k]/x);
            }
        }
    }
    
    
    @Override
    public int getDimSign() {
        return -1;
    }

    @Override
    public String getName() {
        return "WTMM";
    }
    
}
