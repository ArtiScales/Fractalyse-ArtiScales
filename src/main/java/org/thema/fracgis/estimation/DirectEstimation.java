/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.estimation;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.optimization.fitting.CurveFitter;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.data.xy.XYSeries;
import org.thema.fracgis.estimation.EstimationFactory.Type;
import org.thema.fracgis.method.MonoMethod;

/**
 *
 * @author gvuidel
 */
public class DirectEstimation extends AbstractEstimation {


    public abstract class Function implements ParametricUnivariateFunction {
        public abstract double [] getInit();
        public abstract boolean hasParamA();
        public final double getA(double [] params) { 
            if(hasParamA())
                return params[1]; 
            throw new IllegalStateException("Function has no A parameter");
        }
        public final double getD(double [] params) { 
            return params[0]; 
        }
        public abstract String format(double [] coef);
        public abstract String formatShort(double [] coef);
        public abstract double getOLSDerivative(double [] x, double [] y, double d);
        public abstract double[] estimParam(double [] x, double [] y, double d);
    }
    public final class ADCFunction extends Function {

        public double value(double x, double[] param)  {
            return getA(param) * Math.pow(x, getD(param)) + param[2];
        }

        public double[] gradient(double x, double[] param)  {
            double [] grad = new double[3];
            grad[0] = getA(param) * Math.pow(x, getD(param)) * Math.log(x);
            grad[1] = Math.pow(x, getD(param));
            grad[2] = 1;
            return grad;
        }
        
        public double getOLSDerivative(double [] x, double [] y, double d) {
            double[] param = estimParam(x, y, d);
            double sum = 0;
            for(int i = 0; i < x.length; i++)
                sum += Math.pow(x[i], d) * Math.log(x[i]) * (getA(param) * Math.pow(x[i], d) + param[2] - y[i]);
            return sum;            
        }
        
        public double[] estimParam(double [] x, double [] y, double d) {
            double sy = 0;
            double sxd = 0;
            double sx2d = 0;
            double syxd = 0;
            int n = x.length;
            for(int i = 0; i < n; i++) {
                sy += y[i];
                sxd += Math.pow(x[i], d);
                syxd += y[i] * Math.pow(x[i], d);
                sx2d += Math.pow(x[i], 2*d);
            }
            double a = (n * syxd - sy * sxd) / (n * sx2d - sxd*sxd);
            double c = (sy * sx2d - sxd * syxd) / (n * sx2d - sxd*sxd);       
            return new double[] {d, a, c};
        }
        
        @Override
        public String toString() {
            return "y = a * x ^ D + c";
        }

        public double [] getInit() {
            return new double[] {method.getDimSign(), method.getDimSign() == -1 ? method.getCurve().firstEntry().getValue() : 1, 0};
        }
        public String format(double [] coef) {
            return String.format("Dimension : %.4g\na : %g\nc : %g", method.getDimSign()*getD(coef), getA(coef), coef[2]);
        }
        public String formatShort(double [] coef) {
            return String.format(Locale.US, "a%g_c%g", getA(coef), coef[2]);
        }
        
        public boolean hasParamA() { return true; }
        
    }
    public final class ADFunction extends Function {

        public double value(double x, double[] param)  {
            return getA(param) * Math.pow(x, getD(param));
        }

        public double[] gradient(double x, double[] param)  {
            double [] grad = new double[2];
            grad[0] = getA(param) * Math.pow(x, getD(param)) * Math.log(x);
            grad[1] = Math.pow(x, getD(param));
            return grad;
        }
        
        public double getOLSDerivative(double [] x, double [] y, double d) {          
            double[] param = estimParam(x, y, d);
            double sum = 0;
            for(int i = 0; i < x.length; i++)
                sum += Math.pow(x[i], d) * Math.log(x[i]) * (getA(param) * Math.pow(x[i], d) - y[i]);
            return sum;
        }
        
        public double[] estimParam(double [] x, double [] y, double d) {
            double sx2d = 0;
            double syxd = 0;
            for(int i = 0; i < x.length; i++) {
                syxd += y[i] * Math.pow(x[i], d);
                sx2d += Math.pow(x[i], 2*d);
            }
            double a = syxd / sx2d;            
            return new double[] {d, a};
        }
        
        @Override
        public String toString() {
            return "y = a * x ^ D";
        }

        public double [] getInit() {
            return new double[] {method.getDimSign(), method.getDimSign() == -1 ? method.getCurve().firstEntry().getValue() : 1};
        }
        public String format(double [] coef) {
            return String.format("Dimension : %.4g\na : %g", method.getDimSign()*coef[0], coef[1]);
        }
        public String formatShort(double [] coef) {
            return String.format(Locale.US, "a%g", coef[1]);
        }
        
        public boolean hasParamA() { return true; }
    }
    public final class DCFunction extends Function {

        public double value(double x, double[] param)  {
            return Math.pow(x, getD(param)) + param[1];
        }

        public double[] gradient(double x, double[] param)  {
            double [] grad = new double[2];
            grad[0] = Math.pow(x, getD(param)) * Math.log(x);
            grad[1] = 1;
            return grad;
        }

        public double getOLSDerivative(double [] x, double [] y, double d) {
            double[] param = estimParam(x, y, d);
            double sum = 0;
            for(int i = 0; i < x.length; i++)
                sum += Math.pow(x[i], d) * Math.log(x[i]) * (Math.pow(x[i], d) + param[1] - y[i]);
            return sum;
        }
        
        public double[] estimParam(double [] x, double [] y, double d) {
            double c = 0;
            for(int i = 0; i < x.length; i++)
                c += y[i] - Math.pow(x[i], d);
            c /= x.length;
            return new double[]{d, c};
        }
        
        @Override
        public String toString() {
            return "y = x ^ D + c";
        }

        public double [] getInit() {
            return new double[] {method.getDimSign(), 0};
        }
        public String format(double [] coef) {
            return String.format("Dimension : %.4g\nc : %g", method.getDimSign()*coef[0], coef[1]);
        }
        public String formatShort(double [] coef) {
            return String.format(Locale.US, "c%g", coef[1]);
        }
        
        public boolean hasParamA() { return false; }
        
    }
    public final class DFunction extends Function {

        public double value(double x, double[] param)  {
            return Math.pow(x, getD(param));
        }

        public double[] gradient(double x, double[] param)  {
            double [] grad = new double[1];
            grad[0] = Math.pow(x, getD(param)) * Math.log(x);
            return grad;
        }

        public double getOLSDerivative(double [] x, double [] y, double d) {
            double sum = 0;
            for(int i = 0; i < x.length; i++)
                sum += Math.pow(x[i], d) * Math.log(x[i]) * (Math.pow(x[i], d) - y[i]);
            return sum;
        }
        
        public double[] estimParam(double [] x, double [] y, double d) {
            return new double[]{d};
        }
        
        @Override
        public String toString() {
            return "y = x ^ D";
        }

        public double [] getInit() {
            return new double[] {method.getDimSign()};
        }
        public String format(double [] coef) {
            return String.format("Dimension : %.4g", method.getDimSign()*getD(coef));
        }
        public String formatShort(double [] coef) {
            return "";
        }
        
        public boolean hasParamA() { return false; }
    }

    private List<Function> functions;
    private transient Function function;
    private double [] coef;

    public DirectEstimation(MonoMethod method) {
        this(method, 0);
    }
    
    public DirectEstimation(MonoMethod method, int indModel) {
        super(method);
        if(method.getDimSign() == 1)
            functions = Arrays.asList(new ADCFunction(), new ADFunction(), new DCFunction(), new DFunction());
        else
            functions = Arrays.asList(new ADCFunction(), new ADFunction());
        function = functions.get(indModel);
        estimate();
    }

    public List getModels() {
        return functions;
    }
    
    public Function getModel() {
        return function;
    }

    public void setModel(int indModel) {
        function = functions.get(indModel);
        estimate();
    }

    public double[] getCoef() {
        return coef;
    }
    protected void estimate() {
//        long t1 = System.currentTimeMillis();
//        double [] x = new double[curve.size()], 
//                  y = new double[curve.size()];
//        int i = 0;
//        for(Double d : getRangeCurve().keySet()) {
//            x[i] = d;
//            y[i] = curve.get(d);
//            i++;
//        }
//        double [] coef2 = new NonLinearFitter(function, x, y).fit();
//        long t2 = System.currentTimeMillis();
        
        CurveFitter fitter = new CurveFitter(new LevenbergMarquardtOptimizer());
        for(Double d : getRangeCurve().keySet()) {
            if(d.isNaN() || d.isInfinite() || curve.get(d).isNaN() || curve.get(d).isInfinite())
                throw new IllegalArgumentException("Curve contains NaN or infinite value");
            fitter.addObservedPoint(d, curve.get(d));
        }
        
        coef = new double[function.getInit().length];
        Arrays.fill(coef, Double.NaN);
        
        try {
            coef = fitter.fit(function, function.getInit());
        } catch (Exception ex) {
//            Logger.getLogger(DirectEstimation.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Estimation impossible.", ex);
        }
//        long t3 = System.currentTimeMillis();
//        System.out.println("Dicho : " +(t2-t1) + " - Levenberg : " + (t3-t2));
//        if(Math.abs(coef[0] - coef2[0]) / coef[0] > 0.001)
//            throw new RuntimeException("Ecart d'estimation : " + coef[0] + " - " + coef2[0]);
    }

    public double getDimension() {
        return method.getDimSign() * coef[0];
    }

    public double getR2() {
        SummaryStatistics statErr = new SummaryStatistics();
        SummaryStatistics statTot = new SummaryStatistics();

        for(Double d : getRangeCurve().keySet()) {
            statErr.addValue(curve.get(d) - getEstimValue(d));
            statTot.addValue(curve.get(d));
        }
        
        return 1 - statErr.getSumsq() / statTot.getSecondMoment();
    }

    public double getBIC() {
        SummaryStatistics statErr = new SummaryStatistics();

        for(Double d : getRangeCurve().keySet()) 
            statErr.addValue(curve.get(d) - getEstimValue(d));

        int n = getRangeCurve().size();
        double err = statErr.getSumsq();
        if(err == 0)
            return -Double.MAX_VALUE;
        else
            return function.getInit().length *  Math.log(n) + n *  Math.log(err / n);
    }
    
    @Override
    public double[] getBootStrapConfidenceInterval() {
//        long t1 = System.currentTimeMillis();
        ArrayList<Map.Entry<Double, Double>> init = new ArrayList(getRangeCurve().entrySet());
        DescriptiveStatistics stat = new DescriptiveStatistics();
        CurveFitter fitter = new CurveFitter(new LevenbergMarquardtOptimizer());

        double [] coefs = getCoef();//function.getInit();
//        double [] x = new double[init.size()],
//                y = new double[init.size()];
        for(int i = 0; i < 2000; i++) {
            fitter.clearObservations();
            for(int j = 0; j < init.size(); j++) {
                Map.Entry<Double, Double> sample = init.get((int)(Math.random()*init.size()));
                fitter.addObservedPoint(sample.getKey(), sample.getValue());
            }
        
            try {
                double [] c = fitter.fit(function, coefs);
                stat.addValue(method.getDimSign() * c[0]);  
            } catch (Exception ex) {
               Logger.getLogger(DirectEstimation.class.getName()).log(Level.SEVERE, null, ex);
            }        
            
//            for(int j = 0; j < init.size(); j++) {
//                Map.Entry<Double, Double> sample = init.get((int)(Math.random()*init.size()));
//                x[j] = sample.getKey();
//                y[j] = sample.getValue();
//            }
//            double[] fit = new NonLinearFitter(function, x, y).fit();
//            stat.addValue(fit[0]);
        }
//        System.out.println("Bootstrap : " +(System.currentTimeMillis()-t1));
        return new double[] {stat.getPercentile(2.5), stat.getPercentile(97.5)};
    }

    public double getEstimValue(double x) {
        return function.value(x, coef);
    }

    @Override
    protected XYSeries getEstimationSerie() {
        XYSeries serie = new XYSeries("Estimated");
        double d = range.getLength() / 100;
        for(int i = 0; i <= 100; i++)
            serie.add(range.getLowerBound() + i * d, function.value(range.getLowerBound() + i * d, coef));
      
        return serie;
    }

    public String getResultInfo() {
        double[] inter = getBootStrapConfidenceInterval();
        if(function.hasParamA() && method.getDimSign() == -1)
            return String.format("%s\na norm : %g\n\nR2 : %g\nBIC : %g\nBootstrap confidence : [%.4g - %.4g]", function.format(coef),
                    function.getA(coef) / getCurve().firstEntry().getValue(),
                    getR2(), getBIC(), inter[0], inter[1]);
        else
            return String.format("%s\n\nR2 : %g\nBIC : %g\nBootstrap confidence : [%.4g - %.4g]", function.format(coef),
                getR2(), getBIC(), inter[0], inter[1]);
    }

    public String getParamInfo() {
        String s = function.formatShort(coef);
        if(function.hasParamA() && method.getDimSign() == -1)
            s += String.format(Locale.US, "_anorm%g", function.getA(coef) / getCurve().firstEntry().getValue());
        return s;
    }

    public Type getType() {
        return Type.DIRECT;
    }

}
