/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method;

import java.util.TreeMap;

/**
 *
 * @author gvuidel
 */
public interface MonoMethod extends Method {
    public TreeMap<Double, Double> getCurve();
}
