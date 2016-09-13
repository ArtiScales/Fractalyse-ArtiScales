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

import org.thema.fracgis.estimation.DirectEstimation.Function;

/**
 * Test estimation par dichotomie.
 * Même résultat mais pas plus rapide que Levenberg
 * @author Gilles Vuidel
 */
public class NonLinearFitter {
    private Function function;
    private double [] x, y;

    public NonLinearFitter(Function function, double[] x, double[] y) {
        this.function = function;
        this.x = x;
        this.y = y;
    }
    
    public double [] fit() {
        double d = function.getInit()[0];
        double pas = 0.6;
        // normalement la dérivée est croissante mais au cas où...
//        if(function.getOLSDerivative(x, y, 0) > function.getOLSDerivative(x, y, 2*d))
//            pas = -pas;
        double res = function.getOLSDerivative(x, y, d);
        for(int i = 0; i < 10 && res != 0; i++) {       
            if(res < 0) {
                d = d + pas;
            } else {
                d = d - pas;
            }
            pas = pas / 2;
            res = function.getOLSDerivative(x, y, d);
        }
        
        return function.estimParam(x, y, d);
    }
}
