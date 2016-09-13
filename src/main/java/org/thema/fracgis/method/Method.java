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

package org.thema.fracgis.method;

import org.thema.fracgis.sampling.DefaultSampling;
import com.vividsolutions.jts.geom.Envelope;
import org.thema.common.ProgressBar;
import org.thema.fracgis.estimation.ScaleRangeShape;

/**
 * Main interface for implementing fractal dimension calculation.
 * 4 sub interface exist :
 * - mono or multi fractal method
 * - vector or raster data
 * 
 * @author Gilles Vuidel
 */
public interface Method {
    
    /**
     * @return the full name of the method and its parameters
     */
    String getDetailName();

    /**
     * @return the name of the input layer
     */
    String getInputLayerName();
    
    /**
     * @return the envelope in world coordinate of the data (input layer)
     */
    Envelope getDataEnvelope();
    
    /**
     * @return the name of the method
     */
    String getName();

    /**
     * Launch the execution of this method. 
     * @param monitor the progression monitor
     * @param parallel is the computation parallelized ?
     */
    void execute(ProgressBar monitor, boolean parallel);
    
    /**
     * @return the sign (-1 or +1) of the fractal dimension for this method
     */
    int getDimSign();
    
    /**
     * @return a string containing all parameters
     */
    String getParamString();
    
    /**
     * @return a group layer containing at least {@link ScaleRangeShape}
     */
    MethodLayers getGroupLayer();
    
    /**
     * @return the default scale sampling for this method
     */
    DefaultSampling getSampling();
}
