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


package org.thema.fracgis.method.vector;

import com.vividsolutions.jts.geom.Envelope;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.method.AbstractMethod;
import org.thema.fracgis.sampling.DefaultSampling;

/**
 * Base class for implementing fractal dimension calculation with vector data.
 * This class is used for uni and multi fractal.
 * 
 * @author Gilles Vuidel
 */
public abstract class VectorMethod extends AbstractMethod {
    @ReflectObject.NoParam
    private FeatureCoverage<Feature> coverage;
    
    /**
     * For batch mode
     */
    public VectorMethod() {
        super();
    }
    
    /**
     * Initializes a new vector method with data
     * @param inputName the input layer name
     * @param sampling the scale sampling
     * @param coverage the vector data
     */
    public VectorMethod(String inputName, DefaultSampling sampling, FeatureCoverage coverage) {
        super(inputName, sampling);
        this.coverage = coverage;
        getSampling().updateSampling(coverage);
    }
    
    @Override
    public Envelope getDataEnvelope() {
        return coverage.getEnvelope();
    }
    
    /**
     * Sets the input data
     * @param inputName the input layer name
     * @param coverage the vector data
     */
    public void setInputData(String inputName, FeatureCoverage coverage) {
        this.inputName = inputName;
        this.coverage = coverage;
        
        getSampling().updateSampling(coverage);
    }

    @Override
    public void setSampling(DefaultSampling sampling) {
        super.setSampling(sampling);
        if(coverage != null) {
            getSampling().updateSampling(coverage);
        }
    }

    /**
     * @return the input vector data
     */
    public FeatureCoverage<Feature> getCoverage() {
        return coverage;
    }
    
    
}
