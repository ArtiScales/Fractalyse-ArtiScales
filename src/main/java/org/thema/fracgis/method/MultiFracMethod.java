/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Gilles Vuidel
 */
public interface MultiFracMethod extends Method {
    TreeMap<Double, TreeMap<Double, Double>> getCurves(TreeSet<Double> qs);
    MonoMethod getSimpleMethod(double q);
    TreeMap<Double, Double> getCurve(double q);
}
