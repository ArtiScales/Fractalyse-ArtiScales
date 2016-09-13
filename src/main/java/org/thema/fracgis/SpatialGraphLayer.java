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


package org.thema.fracgis;

import org.thema.graph.SpatialGraph;
import org.thema.graph.shape.GraphGroupLayer;

/**
 * Graph layer for SpatialGraph.
 * 
 * @author Gilles Vuidel
 */
public class SpatialGraphLayer extends GraphGroupLayer {

    private SpatialGraph graph;
    
    /**
     * Creates a new graph layer for a spatial graph.
     * @param name the name of the layer
     * @param graph the spatial graph associated with this layer
     */
    public SpatialGraphLayer(String name, SpatialGraph graph) {
        super(name, graph.getGraph());
        this.graph = graph;
    }

    /**
     * @return the spatial graph associated with this layer
     */
    public SpatialGraph getSpatialGraph() {
        return graph;
    }

}
