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

import com.vividsolutions.jts.geom.Envelope;
import java.util.TreeMap;
import org.thema.common.ProgressBar;

/**
 * Class used for multifractal method to retrieve one q moment estimation.
 * 
 * @author Gilles Vuidel
 */
public class QMonoMethod extends AbstractMethod implements MonoMethod {

    private final MultiFracMethod method;
    private final double q;

    /**
     * Creates a virtual unifractal method based on a multifractal method and a q moment
     * @param method multi fractal method
     * @param q q moment
     */
    public QMonoMethod(MultiFracMethod method, double q) {
        super(method.getInputLayerName(), method.getSampling());
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

    /**
     * This method does nothing.
     * @param monitor
     * @param threaded 
     * @throws UnsupportedOperationException 
     */
    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        throw new UnsupportedOperationException("Nothing to do."); 
    }

    @Override
    public int getDimSign() {
        return method.getDimSign();
    }

    @Override
    public String getParamString() {
        return method.getParamString();
    }

    @Override
    public TreeMap<Double, Double> getCurve() {
        return method.getCurve(q);
    }
    
}
