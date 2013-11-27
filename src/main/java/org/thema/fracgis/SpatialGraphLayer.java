/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis;

import org.thema.graph.SpatialGraph;
import org.thema.graph.shape.GraphGroupLayer;

/**
 *
 * @author gvuidel
 */
public class SpatialGraphLayer extends GraphGroupLayer{

    SpatialGraph graph;
    public SpatialGraphLayer(String name, SpatialGraph graph) {
        super(name, graph.getGraph());
        this.graph = graph;
    }

    public SpatialGraph getSpatialGraph() {
        return graph;
    }

}
