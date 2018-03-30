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

import org.thema.fracgis.method.network.mono.LocalNetworkMethod;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import org.thema.common.ProgressBar;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.layer.GeometryLayer;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.PointStyle;
import org.thema.fracgis.method.AbstractMethod;
import org.thema.fracgis.method.MethodLayers;
import org.thema.fracgis.method.MonoMethod;
import static org.thema.fracgis.method.network.mono.LocalNetworkMethod.DIST_MASS;
import static org.thema.fracgis.method.network.mono.LocalNetworkMethod.LENGTH_WEIGHT;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.sampling.Sampling;
import org.thema.graph.SpatialGraph;
import org.thema.graph.pathfinder.DijkstraPathFinder;

/**
 * Method for computing "desserte" analysis.
 * 
 * @author Gilles Vuidel
 */
public class DesserteMethod extends AbstractMethod implements MonoMethod {

    private SpatialGraph network;
    private Point point;
    private List<Feature> features;
    private List<Feature> errorFeatures;

    private FeatureLayer errLayer;
    private GeometryLayer centreLayer;

    private LocalNetworkMethod localNetworkMethod;
    
    private TreeMap<Double, Double> curve;

    /**
     * Creates a new DesserteMethod.
     * @param inputName the network layer name
     * @param network the network spatial graph
     * @param startPoint the starting point
     * @param features the buildings features
     */
    public DesserteMethod(String inputName, SpatialGraph network, Point startPoint, List<Feature> features)  {
        super(inputName, new DefaultSampling());
        this.network = network;
        this.point = startPoint;
        this.features = features;
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        List<Point> points = new ArrayList<>(features.size());
        for(Feature f : features) {
            points.add(f.getGeometry().getCentroid());
        }
        monitor.setNote("Calc shortest paths...");
        monitor.setProgress(2);
        network.setSnapToEdge(true);
        double [] dist = network.getCostVector(point,
                points, DijkstraPathFinder.DIST_WEIGHTER);

        monitor.setProgress(50);
        errorFeatures = new ArrayList<>();
        curve = new TreeMap<>();
        int i = 0;
        for(double d : dist) {
            if(!Double.isNaN(d) && !Double.isInfinite(d)) {
                if(curve.containsKey(d)) {
                    curve.put(d, curve.get(d)+1);
                } else {
                    curve.put(d, 1.0);
                }
            } else {
                errorFeatures.add(features.get(i));
            }
            i++;
        }

        double n = 0;
        for(Double d : curve.keySet()) {
            n += curve.get(d);
            curve.put(d, n);
        }

        localNetworkMethod = new LocalNetworkMethod(getInputLayerName(), 
                new DefaultSampling(curve.firstKey(), curve.lastKey(), curve.lastKey() / 1000, Sampling.Sequence.ARITH), 
                network, point, LENGTH_WEIGHT);
        localNetworkMethod.execute(monitor, threaded);

        errLayer = new FeatureLayer("Not connected", errorFeatures, new FeatureStyle(Color.RED, Color.RED));
        centreLayer = new GeometryLayer("Centre", network.getLocation(point).getPointOnNetwork(), new PointStyle(Color.BLUE, 3));
        MethodLayers layers = getGroupLayer();
        layers.addLayerFirst(errLayer);
        layers.addLayerFirst(centreLayer);

        layers.getContextMenu().add(new AbstractAction("Desserte/Access") {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DesserteFrame(localNetworkMethod.getCurve(), curve).setVisible(true);
            }
        });
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
        return String.format(Locale.US, "c%g,%g", point.getX(), point.getY());
    }

    @Override
    public String getName() {
        return "Desserte";
    }

    @Override
    public Envelope getDataEnvelope() {
        return new DefaultFeatureCoverage<>(network.getEdges()).getEnvelope();
    }
}
