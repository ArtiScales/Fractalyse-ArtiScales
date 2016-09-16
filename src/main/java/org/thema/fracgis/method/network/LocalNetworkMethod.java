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


package org.thema.fracgis.method.network;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.util.Locale;
import java.util.TreeMap;
import org.geotools.graph.structure.Edge;
import org.thema.common.ProgressBar;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.fracgis.method.AbstractMethod;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.graph.SpatialGraph;
import org.thema.graph.pathfinder.DijkstraPathFinder;


/** 
 * Method for computing radial network analysis.
 * 
 * @author Gilles Vuidel
 */
public class LocalNetworkMethod extends AbstractMethod implements MonoMethod {

    private final SpatialGraph network;
    private final Point point;
    private final double resolution;

    private TreeMap<Double, Double> curve;
    
    /**
     * Creates a new LocalNetworkMethod.
     * @param inputName network layer name
     * @param network network spatial graph
     * @param point the starting point (centre)
     * @param resolution size of counting step
     */
    public LocalNetworkMethod(String inputName, SpatialGraph network, Point point, double resolution) {
        super(inputName, new DefaultSampling());
        this.network = network;
        this.point = point;
        this.resolution = resolution;
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        curve = new TreeMap<>();

        network.setSnapToEdge(true);
        monitor.setNote("Calc shortest paths...");
        monitor.setProgress(2);
        DijkstraPathFinder finder = network.getPathFinder(point, DijkstraPathFinder.DIST_WEIGHTER);
        
        monitor.setNote("Calc distances...");
        monitor.setProgress(60);
        for(Object e : network.getGraph().getEdges()) {
            Edge edge = (Edge) e;
            if(finder.getCost(edge.getNodeA()) == null) {
                continue;
            }
            double start = Math.min(finder.getCost(edge.getNodeA()),
                    finder.getCost(edge.getNodeB()));

            double end = start + DijkstraPathFinder.DIST_WEIGHTER.getWeight(edge);
            double i = Math.ceil(start/resolution)*resolution;
            if(i > 0) {
                addVal(2*i, i-start);
            }
            i+=resolution;
            for(;i < end; i+=resolution) {
                addVal(2*i, resolution);
            }
            addVal(2*i, end-(i-resolution));
        }

        double n = 0;
        for(Double d : curve.keySet()){
            n += curve.get(d);
            curve.put(d, n);
        }

    }

    private void addVal(double x, double val) {
        if(curve.containsKey(x)) {
            curve.put(x, curve.get(x) + val);
        } else {
            curve.put(x, val);
        }
    }

    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
    
    @Override
    public int getDimSign() {
        return 1;
    }
    
    @Override
    public String getParamString() {
        return String.format(Locale.US, "c%g,%g_res%g", point.getX(), point.getY(), resolution);
    }
    
    @Override
    public String getName() {
        return "RadialNetwork";
    }

    @Override
    public Envelope getDataEnvelope() {
        return new DefaultFeatureCoverage<>(network.getEdges()).getEnvelope();
    }
}
