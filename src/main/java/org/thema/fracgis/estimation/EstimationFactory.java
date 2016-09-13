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


package org.thema.fracgis.estimation;

import org.thema.fracgis.method.MonoMethod;

/**
 * Estimation factory.
 * Creates 2 type of estimator : DIRECT (non-linear) or LOG (linear)
 * @author Gilles Vuidel
 */
public class EstimationFactory {
    public enum Type {DIRECT, LOG}
    
    private final MonoMethod method;
    private final Type defaultEstim;

    /**
     * Creates the factory for a given unifractal method.
     * @param method the unifractal method
     */
    public EstimationFactory(MonoMethod method) {
        this.method = method;
        this.defaultEstim = method.getSampling().getDefaultEstimType();
    }
    
    /**
     * @return the default estimation
     */
    public Estimation getDefaultEstimation() {
        return getEstimation(defaultEstim);
    }
    
    /**
     * Creates the estimation for the given type and the default model.
     * @param type the type of estimation
     * @return the estimation for the given type
     */
    public Estimation getEstimation(Type type) {
        return getEstimation(type, 0);
    }
    
    /**
     * Creates the estimation for the given type and the given model.
     * @param type the type of estimation
     * @param indModel index of the model for the estimation type
     * @return a new estimation
     */
    public Estimation getEstimation(Type type, int indModel) {
        return type == Type.DIRECT ? new DirectEstimation(method, indModel) : new LogEstimation(method);
    }
    
    
}
