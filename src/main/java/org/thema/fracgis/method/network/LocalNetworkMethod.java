/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method.network;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.util.Locale;
import java.util.TreeMap;
import org.geotools.graph.structure.Edge;
import org.thema.common.JTS;
import org.thema.common.parallel.ProgressBar;
import org.thema.fracgis.method.AbstractMethod;
import org.thema.fracgis.method.MonoMethod;
import org.thema.graph.SpatialGraph;
import org.thema.graph.pathfinder.DijkstraPathFinder;


/** 
 *
 * @author gvuidel
 */
public class LocalNetworkMethod extends AbstractMethod implements MonoMethod {

    private final SpatialGraph network;
    private final Point point;
    private final double resolution;

    private TreeMap<Double, Double> curve;
    
    public LocalNetworkMethod(String inputName, SpatialGraph network, Point point, double resolution) {
        super(inputName);
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
            if(finder.getCost(edge.getNodeA()) == null)
                continue;
            double start = Math.min(finder.getCost(edge.getNodeA()),
                    finder.getCost(edge.getNodeB()));

            double end = start + DijkstraPathFinder.DIST_WEIGHTER.getWeight(edge);
            double i = Math.ceil(start/resolution)*resolution;
            addVal(2*i, i-start);
            for(;i < end; i+=resolution)
                addVal(2*i, resolution);
            addVal(2*i, end-(i-resolution));
        }

        double n = 0;
        for(Double d : curve.keySet()){
            n += curve.get(d);
            curve.put(d, n);
        }

    }

    private void addVal(double x, double val) {
        if(curve.containsKey(x))
                curve.put(x, curve.get(x) + val);
            else
                curve.put(x, val);
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
    public String getParamsName() {
        return String.format(Locale.US, "c%g,%g_res%g", point.getX(), point.getY(), resolution);
    }
    
    @Override
    public String getName() {
        return "RadialNetwork";
    }

    @Override
    public Envelope getDataEnvelope() {
        return JTS.rectToEnv(network.getGraphLayer().getBounds());
    }
}
