/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method.network;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import org.thema.common.JTS;
import org.thema.common.parallel.ProgressBar;
import org.thema.drawshape.feature.Feature;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.layer.GeometryLayer;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.PointStyle;
import org.thema.fracgis.method.Method;
import org.thema.graph.SpatialGraph;
import org.thema.graph.pathfinder.DijkstraPathFinder;

/**
 *
 * @author gvuidel
 */
public class DesserteMethod extends Method {

    SpatialGraph network;
    Point point;
    List<Feature> features;
    List<Feature> errorFeatures;

    FeatureLayer errLayer;
    GeometryLayer centreLayer;

    private LocalNetworkMethod localNetworkMethod;

    public DesserteMethod(String inputName, SpatialGraph network, Point startPoint, List<Feature> features)  {
        super(inputName);
        this.network = network;
        this.point = startPoint;
        this.features = features;
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        List<Point> points = new ArrayList<Point>(features.size());
        for(Feature f : features)
            points.add(f.getGeometry().getCentroid());

        monitor.setNote("Calc shortest paths...");
        monitor.setProgress(2);
        network.setSnapToEdge(true);
        double [] dist = network.getCostVector(point,
                points, DijkstraPathFinder.DIST_WEIGHTER);

        monitor.setProgress(50);
        errorFeatures = new ArrayList<Feature>();
        curve = new TreeMap<Double, Double>();
        int i = 0;
        for(double d : dist) {
            if(!Double.isNaN(d) && !Double.isInfinite(d))
                if(curve.containsKey(d))
                    curve.put(d, curve.get(d)+1);
                else
                    curve.put(d, 1.0);
            else
                errorFeatures.add(features.get(i));
            i++;
        }

        double n = 0;
        for(Double d : curve.keySet()) {
            n += curve.get(d);
            curve.put(d, n);
        }

        localNetworkMethod = new LocalNetworkMethod(inputName, network, point, curve.lastKey() / 1000);
        localNetworkMethod.execute(monitor, threaded);

        errLayer = new FeatureLayer("Not connected", errorFeatures, new FeatureStyle(Color.RED, Color.RED));
        centreLayer = new GeometryLayer("Centre", network.getLocation(point).getPointOnNetwork(), new PointStyle(Color.BLUE, 3));
        MethodLayers layers = getGroupLayer();
        layers.addLayer(errLayer);
        layers.addLayer(centreLayer);

        layers.getContextMenu().add(new AbstractAction("Desserte/Access") {
            public void actionPerformed(ActionEvent e) {
                new DesserteFrame(localNetworkMethod.getCurve(), curve).setVisible(true);
            }
        });
    }

    @Override
    public int getDimSign() {
        return 1;
    }
    
    @Override
    public String getParamsName() {
        return String.format(Locale.US, "c%g,%g", point.getX(), point.getY());
    }

    @Override
    public String getName() {
        return "Desserte";
    }

    @Override
    public Envelope getDataEnvelope() {
        return JTS.rectToEnv(network.getGraphLayer().getBounds());
    }
}
