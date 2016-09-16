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

import com.vividsolutions.jts.util.CollectionUtil;
import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.fitting.CurveFitter;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.data.xy.XYSeries;
import org.thema.fracgis.estimation.EstimationFactory.Type;
import org.thema.fracgis.method.MonoMethod;

/**
 * Fractal dimension estimation for non linear models :
 * - a*x^d+c
 * - a*x^d
 * - x^d+c
 * - x^d
 * @author Gilles Vuidel
 */
public class DirectEstimation extends AbstractEstimation {

    /**
     * Bas class for implementing model.
     */
    public abstract class Function implements ParametricUnivariateFunction {
        /** @return initial coefficients for the finding */
        public abstract double [] getInit();
        /** @return true if this model has a parameter */
        public abstract boolean hasParamA();
        /** @return the a coefficient given a coefficients array */
        public final double getA(double [] coefs) { 
            if(hasParamA()) {
                return coefs[1];
            } 
            throw new IllegalStateException("Function has no A parameter");
        }
        /**
         * @return the D coefficient (fractal dimension) given a coefficients array
         */
        public final double getD(double [] coefs) { 
            return coefs[0]; 
        }
        public abstract String format(double [] coef);
        public abstract String formatShort(double [] coef);
        /** Calculates the OLS derivative value given the curve and the D coefficient. */
        public abstract double getOLSDerivative(double [] x, double [] y, double d);
        /** Estimates coefficients given the curve and the D coefficient */
        public abstract double[] estimParam(double [] x, double [] y, double d);
    }
    public final class ADCFunction extends Function {

        @Override
        public double value(double x, double[] param)  {
            return getA(param) * Math.pow(x, getD(param)) + param[2];
        }

        @Override
        public double[] gradient(double x, double[] param)  {
            double [] grad = new double[3];
            grad[0] = getA(param) * Math.pow(x, getD(param)) * Math.log(x);
            grad[1] = Math.pow(x, getD(param));
            grad[2] = 1;
            return grad;
        }
        
        @Override
        public double getOLSDerivative(double [] x, double [] y, double d) {
            double[] param = estimParam(x, y, d);
            double sum = 0;
            for(int i = 0; i < x.length; i++) {
                sum += Math.pow(x[i], d) * Math.log(x[i]) * (getA(param) * Math.pow(x[i], d) + param[2] - y[i]);
            }
            return sum;            
        }
        
        @Override
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

        @Override
        public double [] getInit() {
            return new double[] {method.getDimSign(), method.getDimSign() == -1 ? method.getCurve().firstEntry().getValue() : 1, 0};
        }
        @Override
        public String format(double [] coef) {
            return String.format("Dimension : %.4g\na : %g\nc : %g", method.getDimSign()*getD(coef), getA(coef), coef[2]);
        }
        @Override
        public String formatShort(double [] coef) {
            return String.format(Locale.US, "a%g_c%g", getA(coef), coef[2]);
        }
        
        @Override
        public boolean hasParamA() { return true; }
        
    }
    public final class ADFunction extends Function {

        @Override
        public double value(double x, double[] param)  {
            return getA(param) * Math.pow(x, getD(param));
        }

        @Override
        public double[] gradient(double x, double[] param)  {
            double [] grad = new double[2];
            grad[0] = getA(param) * Math.pow(x, getD(param)) * Math.log(x);
            grad[1] = Math.pow(x, getD(param));
            return grad;
        }
        
        @Override
        public double getOLSDerivative(double [] x, double [] y, double d) {          
            double[] param = estimParam(x, y, d);
            double sum = 0;
            for(int i = 0; i < x.length; i++) {
                sum += Math.pow(x[i], d) * Math.log(x[i]) * (getA(param) * Math.pow(x[i], d) - y[i]);
            }
            return sum;
        }
        
        @Override
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

        @Override
        public double [] getInit() {
            return new double[] {method.getDimSign(), method.getDimSign() == -1 ? method.getCurve().firstEntry().getValue() : 1};
        }
        @Override
        public String format(double [] coef) {
            return String.format("Dimension : %.4g\na : %g", method.getDimSign()*coef[0], coef[1]);
        }
        @Override
        public String formatShort(double [] coef) {
            return String.format(Locale.US, "a%g", coef[1]);
        }
        
        @Override
        public boolean hasParamA() { 
            return true; 
        }
    }
    public final class DCFunction extends Function {

        @Override
        public double value(double x, double[] param)  {
            return Math.pow(x, getD(param)) + param[1];
        }

        @Override
        public double[] gradient(double x, double[] param)  {
            double [] grad = new double[2];
            grad[0] = Math.pow(x, getD(param)) * Math.log(x);
            grad[1] = 1;
            return grad;
        }

        @Override
        public double getOLSDerivative(double [] x, double [] y, double d) {
            double[] param = estimParam(x, y, d);
            double sum = 0;
            for(int i = 0; i < x.length; i++) {
                sum += Math.pow(x[i], d) * Math.log(x[i]) * (Math.pow(x[i], d) + param[1] - y[i]);
            }
            return sum;
        }
        
        @Override
        public double[] estimParam(double [] x, double [] y, double d) {
            double c = 0;
            for(int i = 0; i < x.length; i++) {
                c += y[i] - Math.pow(x[i], d);
            }
            c /= x.length;
            return new double[]{d, c};
        }
        
        @Override
        public String toString() {
            return "y = x ^ D + c";
        }

        @Override
        public double [] getInit() {
            return new double[] {method.getDimSign(), 0};
        }
        @Override
        public String format(double [] coef) {
            return String.format("Dimension : %.4g\nc : %g", method.getDimSign()*coef[0], coef[1]);
        }
        @Override
        public String formatShort(double [] coef) {
            return String.format(Locale.US, "c%g", coef[1]);
        }
        
        @Override
        public boolean hasParamA() { 
            return false; 
        }
        
    }
    public final class DFunction extends Function {

        @Override
        public double value(double x, double[] param)  {
            return Math.pow(x, getD(param));
        }

        @Override
        public double[] gradient(double x, double[] param)  {
            double [] grad = new double[1];
            grad[0] = Math.pow(x, getD(param)) * Math.log(x);
            return grad;
        }

        @Override
        public double getOLSDerivative(double [] x, double [] y, double d) {
            double sum = 0;
            for(int i = 0; i < x.length; i++) {
                sum += Math.pow(x[i], d) * Math.log(x[i]) * (Math.pow(x[i], d) - y[i]);
            }
            return sum;
        }
        
        @Override
        public double[] estimParam(double [] x, double [] y, double d) {
            return new double[]{d};
        }
        
        @Override
        public String toString() {
            return "y = x ^ D";
        }

        @Override
        public double [] getInit() {
            return new double[] {method.getDimSign()};
        }
        @Override
        public String format(double [] coef) {
            return String.format("Dimension : %.4g", method.getDimSign()*getD(coef));
        }
        @Override
        public String formatShort(double [] coef) {
            return "";
        }
        
        @Override
        public boolean hasParamA() { 
            return false; 
        }
    }

    private List<Function> functions;
    private transient Function function;
    private double [] coef;

    /**
     * Calculates a new estimation using the resulting curve of the given unifractal method, with the default model (ax^d+c)
     * @param method the unifractal method
     */
    public DirectEstimation(MonoMethod method) {
        this(method, 0);
    }
    
    /**
     * Calculates a new estimation using the resulting curve of the given unifractal method and the given model.
     * @param method the unifractal method
     * @param indModel the index of the model
     */
    public DirectEstimation(MonoMethod method, int indModel) {
        super(method);
        if(method.getDimSign() == 1) {
            functions = Arrays.asList(new ADCFunction(), new ADFunction(), new DCFunction(), new DFunction());
        } else {
            functions = Arrays.asList(new ADCFunction(), new ADFunction());
        }
        function = functions.get(indModel);
        estimate();
    }

    @Override
    public List getModels() {
        return functions;
    }
    
    @Override
    public Function getModel() {
        return function;
    }

    @Override
    public void setModel(int indModel) {
        function = functions.get(indModel);
        estimate();
    }

    @Override
    public double[] getCoef() {
        return coef;
    }
    
    @Override
    protected final void estimate() {
        double [] x = ArrayUtils.toPrimitive(getRangeCurve().keySet().toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
        double [] y = ArrayUtils.toPrimitive(getRangeCurve().values().toArray(ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY));
        
        coef = new NonLinearFitter(function, x, y).fit();
    }

    @Override
    public double getDimension() {
        return method.getDimSign() * coef[0];
    }

    @Override
    public double getR2() {
        SummaryStatistics statErr = new SummaryStatistics();
        SummaryStatistics statTot = new SummaryStatistics();

        for(Double d : getRangeCurve().keySet()) {
            statErr.addValue(curve.get(d) - getEstimValue(d));
            statTot.addValue(curve.get(d));
        }
        
        return 1 - statErr.getSumsq() / statTot.getSecondMoment();
    }

    /**
     * Calculates the Bayesian Information Criteria.
     * 
     * @return the Bayesian Information Criteria for the regression
     */
    public double getBIC() {
        SummaryStatistics statErr = new SummaryStatistics();

        for(Double d : getRangeCurve().keySet()) {
            statErr.addValue(curve.get(d) - getEstimValue(d));
        }

        int n = getRangeCurve().size();
        double err = statErr.getSumsq();
        if(err == 0) {
            return -Double.MAX_VALUE;
        } else {
            return function.getInit().length *  Math.log(n) + n *  Math.log(err / n);
        }
    }
    
    @Override
    public double[] getBootStrapConfidenceInterval() {
        ArrayList<Map.Entry<Double, Double>> init = new ArrayList(getRangeCurve().entrySet());
        DescriptiveStatistics stat = new DescriptiveStatistics();

        double [] x = new double[init.size()],
                y = new double[init.size()];
        for(int i = 0; i < 2000; i++) {          
            for(int j = 0; j < init.size(); j++) {
                Map.Entry<Double, Double> sample = init.get((int)(Math.random()*init.size()));
                x[j] = sample.getKey();
                y[j] = sample.getValue();
            }
            double[] fit = new NonLinearFitter(function, x, y).fit();
            stat.addValue(method.getDimSign() * fit[0]);
        }
        return new double[] {stat.getPercentile(2.5), stat.getPercentile(97.5)};
    }

    @Override
    public double getEstimValue(double x) {
        return function.value(x, coef);
    }

    @Override
    protected XYSeries getEstimationSerie() {
        XYSeries serie = new XYSeries("Estimated");
        double d = range.getLength() / 100;
        for(int i = 0; i <= 100; i++) {
            serie.add(range.getLowerBound() + i * d, function.value(range.getLowerBound() + i * d, coef));
        }
      
        return serie;
    }

    @Override
    public String getResultInfo() {
        double[] inter = getBootStrapConfidenceInterval();
        if(function.hasParamA() && method.getDimSign() == -1) {
            return String.format("%s\na norm : %g\n\nR2 : %g\nBIC : %g\nBootstrap confidence : [%.4g - %.4g]", function.format(coef),
                    function.getA(coef) / getCurve().firstEntry().getValue(),
                    getR2(), getBIC(), inter[0], inter[1]);
        } else {
            return String.format("%s\n\nR2 : %g\nBIC : %g\nBootstrap confidence : [%.4g - %.4g]", function.format(coef),
                    getR2(), getBIC(), inter[0], inter[1]);
        }
    }

    @Override
    public String getParamInfo() {
        String s = function.formatShort(coef);
        if(function.hasParamA() && method.getDimSign() == -1) {
            s += String.format(Locale.US, "_anorm%g", function.getA(coef) / getCurve().firstEntry().getValue());
        }
        return s;
    }

    @Override
    public Type getType() {
        return Type.DIRECT;
    }

    /**
     * 
     * @param dimSign the sign of the fractal dimension depending on the method used
     * @return the models which can be used depending on the sign
     */
    public static List<String> getModels(int dimSign) {
        if(dimSign == 1) {
            return Arrays.asList("a*x^d+c", "a*x^d", "x^d+c", "x^d");
        } else {
            return Arrays.asList("a*x^d+c", "a*x^d");
        }
    }
}
