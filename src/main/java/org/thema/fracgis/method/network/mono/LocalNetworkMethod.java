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


package org.thema.fracgis.method.network.mono;

import com.vividsolutions.jts.geom.Point;
import java.util.Locale;
import java.util.TreeMap;
import org.thema.common.ProgressBar;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.graph.SpatialGraph;


/** 
 * Method for computing radius mass network analysis.
 * 
 * @author Gilles Vuidel
 */
public class LocalNetworkMethod extends MonoNetworkMethod {

    private Point point;
    
    /**
     * Creates a new LocalNetworkMethod where the distance is used as mass counting
     * @param inputName network layer name
     * @param sampling the scale sampling
     * @param network network spatial graph
     * @param point the starting point (centre)
     * @param distField distance attribute or NO_WEIGHT or LENGTH_WEIGHT
     */
    public LocalNetworkMethod(String inputName, DefaultSampling sampling, SpatialGraph network, Point point, String distField) {
        super(inputName, sampling, network, distField);
        this.point = point;
    }
    
    /**
     * Creates a new LocalNetworkMethod.
     * @param inputName network layer name
     * @param sampling the scale sampling
     * @param network network spatial graph
     * @param point the starting point (centre)
     * @param distField distance attribute or NO_WEIGHT or LENGTH_WEIGHT
     * @param massField attribute name of the mass or DIST_MASS
     * @param edgeField true if massField is an attibute of edges, false if massField is an attribute of nodes
     */
    public LocalNetworkMethod(String inputName, DefaultSampling sampling, SpatialGraph network, Point point, String distField, String massField, boolean edgeField) {
        super(inputName, sampling, network, distField, massField, edgeField);
        this.point = point;
    }


    @Override
    public void execute(ProgressBar monitor, boolean threaded) {

        network.setSnapToEdge(true);
        
        double [] mass = calcFromOnePoint(point);

        curve = new TreeMap<>();
        for(int i = 1; i < mass.length; i++) {
            mass[i] += mass[i-1];
        }
        int i = 0;
        for(Double val : getSampling().getValues()) {
            curve.put(val, mass[i++]);
        }
    }
    
    @Override
    public String getParamString() {
        return String.format(Locale.US, "%sc%g,%g", super.getParamString(), point.getX(), point.getY());
    }
    
    @Override
    public String getName() {
        return "RadialNetwork";
    }

}
