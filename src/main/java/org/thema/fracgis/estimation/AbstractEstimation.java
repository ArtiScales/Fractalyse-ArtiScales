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


package org.thema.fracgis.estimation;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.fracgis.method.MonoMethod;

/**
 * Base class for estimation.
 * 
 * @author Gilles Vuidel
 */
public abstract class AbstractEstimation implements Estimation {

    protected MonoMethod method;
    protected TreeMap<Double, Double> curve;
    protected Range range;

    /**
     * Creates an Estimation based on the result of an unifractal method.
     * @param method the unifractal method
     */
    public AbstractEstimation(MonoMethod method) {
        this.method = method;
        curve = method.getCurve();
        range = new Range(curve.firstKey(), curve.lastKey());
    }

    @Override
    public XYPlot getPlot() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(getEstimationSerie());
        XYSeries serie = new XYSeries("Empirical");
        for(Double d : curve.keySet()) {
            serie.add(d, curve.get(d));
        }
        dataset.addSeries(serie);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesFilled(1, false);
        renderer.setSeriesShapesVisible(0, false);
        XYPlot plot = new XYPlot(dataset, new NumberAxis("X"), new NumberAxis("Y"),
                       renderer);
        return plot;
        
    }

    @Override
    public double [][] getScalingBehaviour() {
        XYSeries serie = new XYSeries("Scaling behaviour");
        Iterator<Double> it = curve.keySet().iterator();
        double precX = it.next();
        while(it.hasNext()) {
            double x = it.next();
            serie.add((x+precX)/2, (Math.log(curve.get(x)) - Math.log(curve.get(precX)))
                    / (Math.log(x) - Math.log(precX)));
            precX = x;
        }
        
        return serie.toArray();
    }
    
    @Override
    public double[][] getSmoothedScalingBehaviour(double bandwidth) {
        double[][] smoothed = convolve(getScalingBehaviour(), bandwidth, getType() == EstimationFactory.Type.LOG);
        return smoothed;
    }

    @Override
    public List<Integer> getInflexPointIndices(double bandwidth, int minInd) {
        return getPointInflex(getScalingBehaviour(), bandwidth, minInd, getType() == EstimationFactory.Type.LOG);
    }
    
    @Override
    public void setRange(double xmin, double xmax) {
        if(curve.floorKey(xmin) == null) {
            xmin = curve.firstKey();
        }
        if(curve.ceilingKey(xmax) == null) {
            xmax = curve.lastKey();
        }
        range = new Range(curve.floorKey(xmin), curve.ceilingKey(xmax));
        estimate();
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }

    @Override
    public MonoMethod getMethod() {
        return method;
    }

    /**
     * The abscissas may not correspond to the empirical curve abscissas
     * @return the estimation curve for plotting purpose
     */
    protected abstract XYSeries getEstimationSerie();

    /**
     * Calculates the regression
     */
    protected abstract void estimate();

    /**
     * @return the part of the empirical curve used for the estimation
     */
    protected NavigableMap<Double, Double> getRangeCurve() {
        return curve.subMap(range.getLowerBound(), true, range.getUpperBound(), true);
    }

    @Override
    public void saveToText(Writer w) throws IOException {
        w.write("Input : " + method.getInputLayerName() + "\n");
        w.write("Method : " + method.getName() + "\n");
        w.write("Parameters : " + method.getParamString() + "\n\n");
        
        w.write(getResultInfo());
        w.write("\n\nX\tY\tScaling behaviour\tEstim\n");
        double[] scalingBehaviour = getScalingBehaviour()[1];
        int i = 0;
        for(Double x : curve.keySet()) {
            double sc = Double.NaN;
            if(i < scalingBehaviour.length) {
                sc = scalingBehaviour[i++];
            }
            if(getRangeCurve().containsKey(x)) { 
                w.write(String.format("%g\t%g\t%g\t%g\n", x, curve.get(x), sc, getEstimValue(x)));
            } else {
                w.write(String.format("%g\t%g\t%g\t\n", x, curve.get(x), sc));
            }
        }

    }

    private double [][] convolve(double [][] curve, double bandwidth, boolean log) {
        int n = curve[0].length;
        double min = curve[0][0];
        double max = curve[0][n-1];
        double [][] smooth = new double[2][n];
        
        double sigma = (max-min) * bandwidth;
        if(log) {
            sigma = (Math.log(max)-Math.log(min)) * bandwidth;
        }
        Gaussian gaussian = new Gaussian(0, sigma);
        
        for(int i = 0; i < n; i++) {
            double x = curve[0][i];
            double sum = 0, sumCoef = 0;
            for(int j = 0; j < n; j++) {
                double xc = log ? (Math.log(curve[0][j]) - Math.log(x)) : (curve[0][j] - x);
                double coef = gaussian.value(xc);
                sum += coef * curve[1][j];
                sumCoef += coef;           
            }
            smooth[0][i] = x;
            smooth[1][i] = sum / sumCoef;
        }
        
        return smooth;
    }
    
    private List<Integer> getPointInflex(double [][] curve, double bandwidth, int minInd, boolean log) {
        int n = curve[0].length;
        double min = curve[0][0];
        double max = curve[0][n-1];
        double [][] smooth = new double[2][n];
        
        double sigma = (max-min) * bandwidth;
        if(log) {
            sigma = (Math.log(max)-Math.log(min)) * bandwidth;
        }
        Gaussian gaussian = new Gaussian(0, sigma);
        
        for(int i = 0; i < n; i++) {
            double x = curve[0][i];
            double sum = 0, sumCoef = 0;
            for(int j = 0; j < n; j++) {
                double xc = log ? (Math.log(curve[0][j]) - Math.log(x)) : (curve[0][j] - x);
                DerivativeStructure xd = new DerivativeStructure(1, 2, 0, xc);
                double coef = gaussian.value(xd).getPartialDerivative(2);
                sum += coef * curve[1][j];
                sumCoef += coef;
            }
            smooth[0][i] = x;
            smooth[1][i] = sum / sumCoef;
        }
        
        List<Integer> ptInflex = new ArrayList<>();
        for(int i = minInd; i < n-1; i++) {
            if(smooth[1][i]*smooth[1][i+1] <= 0 && (smooth[1][i] != 0 || smooth[1][i+1] != 0)) {
                ptInflex.add(i);
            }
        }
        return ptInflex;
    }

}
