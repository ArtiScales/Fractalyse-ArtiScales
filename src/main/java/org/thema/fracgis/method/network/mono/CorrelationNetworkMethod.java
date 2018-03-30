/*
 * Copyright (C) 2018 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.geotools.graph.structure.Node;
import org.thema.common.ProgressBar;
import org.thema.data.feature.Feature;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.graph.SpatialGraph;
import org.thema.graph.Util;
import org.thema.parallel.ExecutorService;
import org.thema.parallel.SimpleParallelTask;

/**
 * Correlation method applied on network. 
 * This method is equivalent to sandbox.
 * 
 * @author Gilles Vuidel
 */
public class CorrelationNetworkMethod extends MonoNetworkMethod {

    /**
     * Creates a new CorrelationNetworkMethod where the distance is used as mass counting
     * @param inputName network layer name
     * @param sampling the scale sampling
     * @param network network spatial graph
     * @param distField distance attribute or NO_WEIGHT or LENGTH_WEIGHT
     */
    public CorrelationNetworkMethod(String inputName, DefaultSampling sampling, SpatialGraph network, String distField) {
        super(inputName, sampling, network, distField);
    }
    
    /**
     * Creates a new CorrelationNetworkMethod.
     * @param inputName network layer name
     * @param sampling the scale sampling
     * @param network network spatial graph
     * @param distField distance attribute or NO_WEIGHT or LENGTH_WEIGHT
     * @param massField attribute name of the mass or DIST_MASS
     * @param edgeField true if massField is an attibute of edges, false if massField is an attribute of nodes
     */
    public CorrelationNetworkMethod(String inputName, DefaultSampling sampling, SpatialGraph network, String distField, String massField, boolean edgeField) {
        super(inputName, sampling, network, distField, massField, edgeField);
    }

    /**
     * Default constructor for batch mode
     */
    public CorrelationNetworkMethod() {
    }
    
    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        
        final double[] mass = new double[getSampling().getValues().size()];
        
        SimpleParallelTask<Node, double[]> task = new SimpleParallelTask<Node, double[]>(new ArrayList<Node>(network.getGraph().getNodes())) {
            @Override
            protected double[] executeOne(Node n) {
                return calcFromOnePoint((Point) Util.getGeometry(n));
            }

            @Override
            public void gather(List<double[]> results) {
                for(double [] m : results) {
                    for(int i = 0; i < mass.length; i++) {
                        mass[i] += m[i];
                    }
                }
            }
        };
        
        if(parallel) {
            ExecutorService.execute(task);
        } else {
            ExecutorService.executeSequential(task);
        }
        
        curve = new TreeMap<>();
        for(int i = 1; i < mass.length; i++) {
            mass[i] += mass[i-1];
        }
        int n = network.getGraph().getNodes().size();
        int i = 0;
        for(Double val : getSampling().getValues()) {
            curve.put(val, mass[i++] / n);
        }
    }
    
    @Override
    public String getName() {
        return "Correlation";
    }
}
