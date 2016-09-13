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


package org.thema.fracgis.sampling;

import java.util.SortedSet;
import java.util.TreeSet;
import org.thema.data.feature.FeatureCoverage;

/**
 * Sampling for box counting with raster data.
 * DefaultSampling for raster data (discrete) creates sizes in pixel of the form 2i+1. 
 * Useful for the most methods (dilation, correlation, radial) but the box counting method.
 * For this one, sizes are not constraints to odd number.
 * 
 * @author Gilles Vuidel
 */
public class RasterBoxSampling extends DefaultSampling {

    /**
     * Creates a default sampling.
     * min and max sizes are initializes to 0, coef to 2 and the sequence to GEOM.
     * They will be updated by {@link #updateSampling() }
     */
    public RasterBoxSampling() {
    }

    /**
     * Copy constructor
     * @param sampling the sampling to copy
     */
    public RasterBoxSampling(DefaultSampling sampling) {
        super(sampling);
    }

    /**
     * Does not manage vector data
     * @param cov 
     * @throws IllegalArgumentException always
     */
    @Override
    public void updateSampling(FeatureCoverage cov) {
        throw new IllegalArgumentException("RasterBoxSampling cannot manage vector data !");
    }
    
    @Override
    public SortedSet<Double> getValues() {
        TreeSet<Double> values = new TreeSet<>();
        double val = getMinSize();
        while(val <= getMaxSize()) {
            int i = (int) Math.round(val/getResolution());
            values.add(i * getResolution());
            val = getNext(val);
        }
        return values;
    }
    
    @Override
    public SortedSet<Integer> getDiscreteValues() {
        TreeSet<Integer> values = new TreeSet<>();
        double val = getMinSize();
        while(val <= getMaxSize()) {
            int i = (int) Math.round(val/getResolution());
            values.add(i);
            val = getNext(val);
        }
        return values;
    }
}
