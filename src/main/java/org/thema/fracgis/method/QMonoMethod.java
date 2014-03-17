/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method;

import com.vividsolutions.jts.geom.Envelope;
import java.util.TreeMap;
import org.thema.common.parallel.ProgressBar;

/**
 *
 * @author gvuidel
 */
public class QMonoMethod extends AbstractMethod implements MonoMethod {

    private final MultiFracMethod method;
    private final double q;

    public QMonoMethod(MultiFracMethod method, double q) {
        super(method.getInputName());
        this.method = method;
        this.q = q;
    }
    
    @Override
    public Envelope getDataEnvelope() {
        return method.getDataEnvelope();
    }

    @Override
    public String getName() {
        return "q" + q + " " + method.getName();
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getDimSign() {
        return method.getDimSign();
    }

    @Override
    public String getParamsName() {
        return method.getParamsName();
    }

    @Override
    public TreeMap<Double, Double> getCurve() {
        return method.getCurve(q);
    }
    
}
