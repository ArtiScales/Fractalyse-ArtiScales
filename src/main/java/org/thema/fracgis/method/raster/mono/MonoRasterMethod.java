/*
 * Copyright (C) 2016 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thema.fracgis.method.raster.mono;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.RenderedImage;
import java.util.TreeMap;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;
import org.thema.fracgis.sampling.DefaultSampling;

/**
 * Base class for implementing unifractal dimension calculation with raster data.
 * This class is used for unifractal only.
 * @author Gilles Vuidel
 */
public abstract class MonoRasterMethod extends RasterMethod implements MonoMethod {

    protected TreeMap<Double, Double> curve;
    
    /**
     * Default constructor for batch mode
     */
    public MonoRasterMethod() {
    }
    
    /**
     * Initializes a new raster method for unifractal with data
     * @param inputName input layer name (must be a raster layer)
     * @param sampling scales sampling
     * @param img the raster image
     * @param envelope the image envelope in world coordinate, may be null
     */
    public MonoRasterMethod(String inputName, DefaultSampling sampling, RenderedImage img, Envelope envelope) {
        super(inputName, sampling, img, envelope);
    }
    
    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
}
