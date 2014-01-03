/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.estimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.thema.fracgis.method.AbstractMethod;
import org.thema.fracgis.method.MonoMethod;

/**
 *
 * @author gvuidel
 */
public class LogEstimation extends AbstractEstimation{

    transient SimpleRegression regression;

    public LogEstimation(MonoMethod method) {
        super(method);
        estimate();
    }

    public double getDimension() {
        return method.getDimSign() * regression.getSlope();
    }

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

    public String getResultInfo() {
        double[] inter = getBootStrapConfidenceInterval();
        return String.format("Dimension : %.4g\nb : %g\n\nR2 : %g\np-value : %g\nConfidence (95%%): [%.4g - %.4g]\nBootstrap confidence : [%.4g - %.4g]", getDimension(), regression.getIntercept(),
                getR2(), getSignificance(), getDimension() - getConfidenceInterval(), getDimension() + getConfidenceInterval(), inter[0], inter[1]);
    }

    public String getParamInfo() {
        return String.format(Locale.US, "b%g", regression.getIntercept());
    }

    public final void estimate() {
        regression = new SimpleRegression();
        for(Double x : getRangeCurve().keySet())
            regression.addData(Math.log(x), Math.log(curve.get(x)));
    }

    public List getModels() {
        return Arrays.asList(getModel());
    }

    public String getModel() {
        return "Log(y) = D * Log(x) + b";
    }
    
    public void setModel(int indModel) {
    }
    
    public EstimationFactory.Type getType() {
        return EstimationFactory.Type.LOG;
    }
}
