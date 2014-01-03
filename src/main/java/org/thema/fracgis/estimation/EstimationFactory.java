/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.estimation;

import org.thema.fracgis.method.MonoMethod;

/**
 *
 * @author gvuidel
 */
public class EstimationFactory {
    public enum Type {DIRECT, LOG}
    
    private final MonoMethod method;
    private final Type defaultEstim;

    public EstimationFactory(MonoMethod method) {
        this.method = method;
        this.defaultEstim = method.getDimSign() == 1 ? Type.DIRECT : Type.LOG;
    }
    
    public Estimation getDefaultEstimation() {
        return getEstimation(defaultEstim);
    }
    
    public Estimation getEstimation(Type type) {
        return getEstimation(type, 0);
    }
    
    public Estimation getEstimation(Type type, int indModel) {
        return type == Type.DIRECT ? new DirectEstimation(method, indModel) : new LogEstimation(method);
    }
    
    
}
