/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method.vector.mono;

import java.util.TreeMap;
import org.thema.common.param.XMLParams;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.vector.VectorMethod;

/**
 *
 * @author gvuidel
 */
public abstract class SimpleVectorMethod extends VectorMethod implements MonoMethod {

    @XMLParams.NoParam
    protected TreeMap<Double, Double> curve;

    public SimpleVectorMethod() {
    }

    public SimpleVectorMethod(String inputName, FeatureCoverage coverage) {
        super(inputName, coverage);
        curve = new TreeMap<>();
    }
   
    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
    
}
