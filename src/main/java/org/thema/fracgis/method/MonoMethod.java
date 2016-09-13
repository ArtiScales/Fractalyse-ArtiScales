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

/**
 * Interface for implementing unifractal dimension calculation.
 * @author Gilles Vuidel
 */
public interface MonoMethod extends Method {
    /**
     * Returns a map of coordinates (x, y) representing the curve computed by this unifractal method.
     * {@link Method#execute()} must be called before.
     * @return the curve computed by the unifractal method
     */
    public TreeMap<Double, Double> getCurve();
}
