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
import java.util.List;
import java.util.TreeMap;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.thema.fracgis.estimation.EstimationFactory.Type;
import org.thema.fracgis.method.Method;
import org.thema.fracgis.method.MonoMethod;

/**
 * Main interface for implementing estimation of the fractal dimension from an unifractal method.
 * 
 * @author Gilles Vuidel
 */
public interface Estimation {

    /** 
     * @return the type of estimation (DIRECT or LOG)
     */
    Type getType();
    /**
     * 
     * @param x the abscissa
     * @return the estimated ordinate for the abscissa x
     */
    double getEstimValue(double x);
    /**
     * 
     * @return the empirical curve given from the MonoMethod
     */
    TreeMap<Double, Double> getCurve();
    /**
     * @return the estimated fracta dimension
     */
    double getDimension();
    /**
     * @return the r2 of the regression
     */
    double getR2();
    /**
     * @return the plot containing the empirical curve and the estimated one.
     */
    XYPlot getPlot();
    /**
     * Sets the range for the estimation.
     * The regression is computed again with this new range 
     * @param xmin the min abscissa
     * @param xmax the max abscissa
     */
    void setRange(double xmin, double xmax);
    /**
     * @return the range used in the current estimation
     */
    Range getRange();
    /**
     * Returns the scaling behaviour of the empirical curve : dlog(y)/dlog(x).
     * The array dimension is 2 x getCurve().size()-1
     * The first dimension contains 0 for x and 1 for y
     * the second dimension is getCurve().size()-1, due to discrete derivative
     * @return the scaling behaviour of the empirical curve
     */
    double[][] getScalingBehaviour();
    /**
     * Returns the smoothed scaling behaviour of the empirical curve .
     * @param bandwidth the level of smoothing between 0 and 1
     * The array dimension is 2 x getCurve().size()-1
     * The first dimension contains 0 for x and 1 for y
     * the second dimension is getCurve().size()-1, due to discrete derivative
     * @return the smoothed scaling behaviour
     */
    double[][] getSmoothedScalingBehaviour(double bandwidth);
    /**
     * Calculates inflexion points of the smoothed scaling behaviour.
     * @param bandwidth the level of smoothing between 0 and 1
     * @param minInd the minimum index in the scaling behaviour array
     * @return indices (in the scaling behaviour array) of the inflexion points, the list can be empty if no inflexion point is found
     */
    List<Integer> getInflexPointIndices(double bandwidth, int minInd);
    /**
     * @return a String with the resulting info to show in UI
     */
    String getResultInfo();
    /**
     * @return a String containing the parameters of the regression in a human readable manner
     */
    String getParamInfo();
    /**
     * The coefficients of the regression.
     * The size of the array depends on the selected model.
     * The first element corresponds always to the fractal dimension except for the sign (ie. the coefficient is negative for decreasing curve).
     * Use {@link Method#getDimSign() } to correct the sign.
     * @return the coefficients of the regression.
     */
    double[] getCoef();
    /**
     * Calculates the confidence interval of the fractal dimension of the regression by bootstrap method.
     * This method can be slow for curve with many points.
     * @return the confidence interval at 95%
     */
    double[] getBootStrapConfidenceInterval();
    /**
     * @return the models which can be used. The list must contain at least one model.
     */
    List getModels();
    /**
     * @return the current used model
     */
    Object getModel();
    /**
     * Select the model used and calculates again the regression with this model.
     * @param indModel the index of the model in the list of {@link #getModels() }
     */
    void setModel(int indModel);
    /**
     * @return the unifractal method used for creating the empirical curve.
     */
    MonoMethod getMethod();
    /**
     * Saves the curves and the results of the regression in a writer
     * @param w the Writer 
     * @throws IOException 
     */
    void saveToText(Writer w) throws IOException;
}
