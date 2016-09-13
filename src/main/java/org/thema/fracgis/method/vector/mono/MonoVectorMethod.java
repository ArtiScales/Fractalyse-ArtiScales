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


package org.thema.fracgis.method.vector.mono;

import java.util.TreeMap;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.method.vector.VectorMethod;

/**
 * Base class for implementing unifractal dimension calculation with vector data.
 * This class is used for unifractal only.
 * 
 * @author Gilles Vuidel
 */
public abstract class MonoVectorMethod extends VectorMethod implements MonoMethod {

    @ReflectObject.NoParam
    protected TreeMap<Double, Double> curve;

    /**
     * For parameter management only
     */
    public MonoVectorMethod() {
    }

    /**
     * Initializes an unifractal method for vector data
     * @param inputName the input layer name
     * @param sampling the scale sampling
     * @param coverage the vector data
     */
    public MonoVectorMethod(String inputName, DefaultSampling sampling, FeatureCoverage coverage) {
        super(inputName, sampling, coverage);
        curve = new TreeMap<>();
    }
   
    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
    
}
