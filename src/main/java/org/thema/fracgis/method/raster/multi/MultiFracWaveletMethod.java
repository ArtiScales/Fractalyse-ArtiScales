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
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.common.ProgressBar;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.MultiFracMethod;
import org.thema.fracgis.method.QMonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;
import org.thema.fracgis.sampling.DefaultSampling;

/**
 * Multifractal analysis based on wavelet leaders for raster data.
 * JUST FOR TESTING !!!!
 *
 * @author Gilles Vuidel
 */
public class MultiFracWaveletMethod extends RasterMethod implements MultiFracMethod {

    private transient List<WLBMF.Struct> leaders;
    private transient TreeMap<Double, TreeMap<Double, Double>> cacheCurves;
    
    /**
     * Creates a new multifractal wavelet method for the given data
     * @param inputName input layer name
     * @param sampling scale sampling
     * @param img raster input data
     * @param env envelope of the raster in world coordinate
     */
    public MultiFracWaveletMethod(String inputName, DefaultSampling sampling, RenderedImage img, Envelope envelope) {
        super(inputName, sampling, img, envelope);
        this.cacheCurves = new TreeMap<>();
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        double [][] data = new double[getImg().getHeight()][getImg().getWidth()];
        RandomIter iter = RandomIterFactory.create(getImg(), null);
        for(int i = 0; i < data.length; i++) {
            for(int j = 0; j < data[0].length; j++) {
                data[i][j] = iter.getSampleDouble(j, i, 0);
            }
        }
        leaders = new WLBMF().DxLx2d(data, 3, 0.7);
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
        XYSeriesCollection series = new XYSeriesCollection();
        for(int i = 0; i < leaders.size(); i++) {
            double [] v = leaders.get(i).getVectorMax();
            double [] sum = new double[qList.size()];
            for(int j = 0; j < v.length; j++)  {
                double val = v[j];
                if(val > 1e-15) {
                    for(int k = 0; k < sum.length; k++) {
                        sum[k] += Math.pow(val, qList.get(k));
                    }
                }
            }
            double [] Dq = new double[qList.size()];
            double [] hq = new double[qList.size()];
            for(int j = 0; j < v.length; j++)  {
                double val = v[j];
                if(val > 1e-15) {
                    for(int k = 0; k < sum.length; k++) {
                        double vq = Math.pow(val, qList.get(k));
                        Dq[k] += vq * Math.log(vq / sum[k]) / Math.log(2);
                        hq[k] += vq * Math.log(val) / Math.log(2);
                    }
                }
            }
            
            for(int k = 0; k < sum.length; k++) {
                cacheCurves.get(qList.get(k)).put(getResolution()*Math.pow(2, i+1), sum[k] / v.length);
            }
            XYSeries serie = new XYSeries("j" + (i+1));
            for(int k = 0; k < sum.length; k++) {
                serie.add(hq[k] / sum[k], Dq[k] / sum[k] + Math.log(v.length) / Math.log(2));
                //serie.add((double)qList.get(k), Dq[k] / sum[k] + Math.log(v.length) / Math.log(2));
            }
            series.addSeries(serie);
        }  
        
        ChartFrame frm = new ChartFrame("D(q)", ChartFactory.createXYLineChart("", "", "", series, PlotOrientation.VERTICAL, true, true, true));
        frm.setVisible(true);
        frm.pack();
    }
    
    
    @Override
    public int getDimSign() {
        return -1;
    }

    @Override
    public String getName() {
        return "LWT";
    }
    
}
