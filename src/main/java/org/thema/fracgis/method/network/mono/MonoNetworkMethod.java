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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import java.util.TreeMap;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.fracgis.method.AbstractMethod;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.graph.SpatialGraph;
import org.thema.graph.Util;
import org.thema.graph.pathfinder.DijkstraPathFinder;
import org.thema.graph.pathfinder.EdgeWeighter;

/**
 *
 * @author gvuidel
 */
public abstract class MonoNetworkMethod extends AbstractMethod implements MonoMethod {
    
    public static final String NO_WEIGHT = "(none)";
    public static final String LENGTH_WEIGHT = "(length)";
    public static final String DIST_MASS = "(distance)";
    
    protected String distField;
    protected String massField;
    protected boolean edgeField;
    
    @ReflectObject.NoParam
    protected SpatialGraph network;
    
    @ReflectObject.NoParam
    protected TreeMap<Double, Double> curve = new TreeMap<>();

    public MonoNetworkMethod(String inputName, DefaultSampling sampling, SpatialGraph network, String distField) {
        super(inputName, sampling);
        this.network = network;
        this.distField = distField;
        this.massField = DIST_MASS;
        this.edgeField = true;
    }
    
    public MonoNetworkMethod(String inputName, DefaultSampling sampling, SpatialGraph network, String distField, String massField, boolean edgeField) {
        super(inputName, sampling);
        this.network = network;
        this.distField = distField;
        this.massField = massField;
        this.edgeField = edgeField;
    }
    
    /** 
     * Default constructor for batch mode
     */
    public MonoNetworkMethod() {    
    }
    
    
    protected double[] calcFromOnePoint(Point point) {

        EdgeWeighter weighter = getWeighter(distField);
        DijkstraPathFinder finder = network.getPathFinder(point, weighter);
        Double [] x = getSampling().getValues().toArray(new Double[0]);
        double [] mass = new double[getSampling().getValues().size()];
        
        if(edgeField) {
            for(Object e : network.getGraph().getEdges()) {
                Edge edge = (Edge) e;
                if(finder.getCost(edge.getNodeA()) == null) {
                    continue;
                }
                
                double d = weighter.getWeight(edge);
                double m = massField.equals(DIST_MASS) ? d : ((Number)((Feature)edge.getObject()).getAttribute(massField)).doubleValue();
                if(d == 0 || m == 0) {
                    continue;
                }
                double start = 2*Math.min(finder.getCost(edge.getNodeA()),
                        finder.getCost(edge.getNodeB()));
                double end = start + 2*d;
                
                if(start > getSampling().getRealMaxSize()) {
                    continue;
                }
                
                int iStart = getSampling().getCeilingScaleIndex(start);
                int iEnd = getSampling().getCeilingScaleIndex(end);

                if(iEnd >= mass.length) {
                    m -= m * (end - x[x.length-1]) / (end-start);
                    iEnd = mass.length-1;
                    end = x[x.length-1];
                }
                if(m > 0) {
                    if(iStart == iEnd) {
                        mass[iStart] += m;
                    } else {
                        mass[iStart] += m * (x[iStart]-start) / (end-start);
                        for(int i = iStart+1; i < iEnd; i++) {
                            mass[i] += m * (x[i]-x[i-1]) / (end-start);
                        }
                        mass[iEnd] += m * (end-x[iEnd-1]) / (end-start);
                    }
                }
                
            }
        } else {
            for(Object n : network.getGraph().getNodes()) {
                Node node = (Node) n;
                if(finder.getCost(node) == null) {
                    continue;
                }
                double dist = 2*finder.getCost(node);
                if(dist > getSampling().getRealMaxSize()) {
                    continue;
                }
                double m = ((Number)((Feature)node.getObject()).getAttribute(massField)).doubleValue();
                int ind = getSampling().getCeilingScaleIndex(dist);
                mass[ind] += m;
            }
        }

        return mass;
    }
    
    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
    
    @Override
    public Envelope getDataEnvelope() {
        return new DefaultFeatureCoverage<>(network.getEdges()).getEnvelope();
    }
    
    @Override
    public int getDimSign() {
        return 1;
    }
    
    public static EdgeWeighter getWeighter(final String distField) {
        if(distField.equals(NO_WEIGHT)) {
            return DijkstraPathFinder.NBEDGE_WEIGHTER;
        } else if(distField.equals(LENGTH_WEIGHT)) {
            return new EdgeWeighter() {
                /** @return length of the geometry edge */
                @Override
                public double getWeight(Edge e) {
                    return Util.getGeometry(e).getLength();
                }

                /** @return dist */
                @Override
                public double getToGraphWeight(double dist) {
                    return 0;
                }
            };
        } else {
            return new EdgeWeighter() {
                @Override
                public double getWeight(Edge edge) {
                    return ((Number)((Feature)edge.getObject()).getAttribute(distField)).doubleValue();
                }

                @Override
                public double getToGraphWeight(double d) {
                    return 0;
                }
            };
        } 
    }
}
