/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.estimation;

import org.thema.fracgis.estimation.DirectEstimation.Function;

/**
 * Test estimation par dichotomie
 * Même résultat mais pas plus rapide que Levenberg
 * @author gvuidel
 */
public class NonLinearFitter {
    Function function;
    double [] x, y;

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
            if(res < 0)
                d = d + pas;
            else
                d = d - pas;
            pas = pas / 2;
            res = function.getOLSDerivative(x, y, d);
        }
        
        return function.estimParam(x, y, d);
    }
}
