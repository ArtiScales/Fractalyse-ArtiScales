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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.thema.fracgis.method.MonoMethod;

/**
 * Fractal dimension estimation for log linear model : log(y) = d*log(x)+b.
 * This is the default estimator for fractal dimension used in most cases.
 * 
 * @author Gilles Vuidel
 */
public class LogEstimation extends AbstractEstimation {

    private transient SimpleRegression regression;

    /**
     * Calculates a new estimation using the resulting curve of the given unifractal method.
     * 
     * @param method the unifractal method
     */
    public LogEstimation(MonoMethod method) {
        super(method);
        Iterator<Double> it = curve.keySet().iterator();
        while(it.hasNext()) {
            if(curve.get(it.next()) <= 0) {
                it.remove();
            }
        }
        range = new Range(curve.firstKey(), curve.lastKey());
        estimate();
    }

    @Override
    public double getDimension() {
        return method.getDimSign() * regression.getSlope();
    }

    @Override
    public double getR2() {
        return regression.getRSquare();
    }
    
    public double getSignificance() {

        return regression.getSignificance();
            // version à la main identique
            //return 2 * new TDistributionImpl(regression.getN()-2).cumulativeProbability(regression.getSlope() / regression.getSlopeStdErr());
    }
    
    public double getConfidenceInterval() {
        try {
            return regression.getSlopeConfidenceInterval();
        } catch (Exception ex) {
            Logger.getLogger(LogEstimation.class.getName()).log(Level.SEVERE, null, ex);
            return Double.NaN;
        }
    }
    
    @Override
    public double[] getBootStrapConfidenceInterval() {
        ArrayList<Entry<Double, Double>> init = new ArrayList(getRangeCurve().entrySet());
        DescriptiveStatistics stat = new DescriptiveStatistics();
        SimpleRegression reg = new SimpleRegression();
        for(int i = 0; i < 10000; i++) {
            reg.clear();
            for(int j = 0; j < init.size(); j++) {
                Entry<Double, Double> sample = init.get((int)(Math.random()*init.size()));
                reg.addData(Math.log(sample.getKey()), Math.log(sample.getValue()));
            }
            stat.addValue(method.getDimSign() * reg.getSlope());            
        }
        return new double[] {stat.getPercentile(2.5), stat.getPercentile(97.5)};
    }

    @Override
    public double[] getCoef() {
        return new double[] {regression.getSlope(), regression.getIntercept()};
    }

    @Override
    public XYPlot getPlot() {
        XYPlot plot = super.getPlot();
        plot.setDomainAxis(new LogarithmicAxis("x"));
        plot.setRangeAxis(new LogarithmicAxis("y"));
        return plot;
    }

    @Override
    public double getEstimValue(double x) {
        return Math.exp(regression.predict(Math.log(x)));
    }

    @Override
    protected XYSeries getEstimationSerie() {
        XYSeries serie = new XYSeries("Estimated");
        serie.add(range.getLowerBound(), Math.exp(regression.predict(Math.log(range.getLowerBound()))));
        serie.add(range.getUpperBound(), Math.exp(regression.predict(Math.log(range.getUpperBound()))));
        return serie;
    }

    @Override
    public String getResultInfo() {
        double[] inter = getBootStrapConfidenceInterval();
        return String.format("Dimension : %.4g\nb : %g\n\nR2 : %g\np-value : %g\nConfidence (95%%): [%.4g - %.4g]\nBootstrap confidence : [%.4g - %.4g]", getDimension(), regression.getIntercept(),
                getR2(), getSignificance(), getDimension() - getConfidenceInterval(), getDimension() + getConfidenceInterval(), inter[0], inter[1]);
    }

    @Override
    public String getParamInfo() {
        return String.format(Locale.US, "b%g", regression.getIntercept());
    }

    @Override
    public final void estimate() {
        regression = new SimpleRegression();
        for(Double x : getRangeCurve().keySet()) {
            regression.addData(Math.log(x), Math.log(curve.get(x)));
        }
    }

    @Override
    public List getModels() {
        return Arrays.asList(getModel());
    }

    @Override
    public String getModel() {
        return "Log(y) = D * Log(x) + b";
    }
    
    /**
     * No-op method. There is only one model.
     * @param indModel 
     */
    @Override
    public void setModel(int indModel) {
    }
    
    @Override
    public EstimationFactory.Type getType() {
        return EstimationFactory.Type.LOG;
    }
}
