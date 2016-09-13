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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Sampling for radial analysis.
 * RadialSampling is a DefaultSampling with a coordinate for the starting point.
 * @author Gilles Vuidel
 */
public class RadialSampling extends DefaultSampling {
    private Coordinate centre;

    /**
     * Creates a new RadialSampling with default minsize and coef and arithmetic sequence.
     * 
     * @param centre the starting point
     * @param maxSize the max size, can be 0
     */
    public RadialSampling(Coordinate centre, double maxSize) {
        super(0, maxSize, 0, Sequence.ARITH);
        this.centre = centre;
    }

    /**
     * Creates a new RadialSampling based on a DefaultSampling
     * @param s the default sampling
     * @param centre the starting point
     */
    public RadialSampling(DefaultSampling s, Coordinate centre) {
        super(s);
        this.centre = centre;
    }

    @Override
    public double getDefaultMax(Envelope env) {
        if(centre == null) {
            centre = getDefaultCentre(env);
        }
        return getDefaultMax(env, centre);
    }

    /**
     * Estimates the default starting point from the envelope of the data
     * @param env the envelope in world coordinate of the data
     * @return the centre of the envelope
     */
    public static Coordinate getDefaultCentre(Envelope env) {
        return env.centre();
    }
    
    /**
     * Estimates the default max size
     * @param env the envelope in world coordinate of the data
     * @param centre the starting point in world coordinate
     * @return the max size
     */
    public static double getDefaultMax(Envelope env, Coordinate centre) {
        return 2*Math.min(Math.min(env.getMaxX() - centre.x, env.getMaxY() - centre.y), Math.min(centre.x - env.getMinX(), centre.y - env.getMinY())); 
    }
}