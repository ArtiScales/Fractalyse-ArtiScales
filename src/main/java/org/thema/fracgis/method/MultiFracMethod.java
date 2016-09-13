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

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Interface for implementing multifractal dimension calculation.
 * 
 * @author Gilles Vuidel
 */
public interface MultiFracMethod extends Method {
    
    /**
     * Calculates the curves for each q moment.
     * {@link Method#execute()} must be called before.
     * @param qs set of q moment
     * @return a map (q moment, curve)
     */
    TreeMap<Double, TreeMap<Double, Double>> getCurves(TreeSet<Double> qs);
    
    /**
     * {@link Method#execute()} must be called before.
     * @param q q moment
     * @return a virtual unifractal method for estimating the fractal dimension of the given q moment
     */
    MonoMethod getSimpleMethod(double q);
    
    /**
     * Returns a map of coordinates (x, y) representing the curve computed by this multifractal method for the given q moment.
     * {@link Method#execute()} must be called before.
     * @param q q moment
     * @return the curve computed for the given q
     */
    TreeMap<Double, Double> getCurve(double q);
}
