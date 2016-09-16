/*
 * Copyright (C) 2016 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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
package org.thema.fracgis.method.network;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.graph.SpatialGraph;

/**
 *
 * @author gvuidel
 */
public class LocalNetworkMethodTest {
    

    private List<DefaultFeature> lines;
    private SpatialGraph graph;
    
    @Before
    public void setUp() throws IOException {
       
        lines = Arrays.asList(
            new DefaultFeature(1, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(0, 64), new Coordinate(10, 64)})),
            new DefaultFeature(2, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(10, 64), new Coordinate(64, 64)})),
            new DefaultFeature(3, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(64, 0), new Coordinate(64, 64)})),
            new DefaultFeature(4, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(128, 64), new Coordinate(64, 64)})),
            new DefaultFeature(5, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(64, 128), new Coordinate(64, 64)})));
        graph = new SpatialGraph(lines);
    }
    

    /**
     * Test of execute method, of class LocalNetworkMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        Point p = new GeometryFactory().createPoint(new Coordinate(64, 64));
        LocalNetworkMethod method = new LocalNetworkMethod("test", graph, p, 8);
        method.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(32.0, 64.0, 96.0, 128.0, 160.0, 192.0, 224.0, 256.0), new ArrayList<>(method.getCurve().values()));
        
        method = new LocalNetworkMethod("test", graph, p, 10);
        method.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(40.0, 80.0, 120.0, 160.0, 200.0, 240.0, 256.0), new ArrayList<>(method.getCurve().values()));
        
    }

    /**
     * Test of getDataEnvelope method, of class LocalNetworkMethod.
     */
    @Test
    public void testGetDataEnvelope() {
        System.out.println("getDataEnvelope");
        LocalNetworkMethod method = new LocalNetworkMethod("test", graph, null, 0);
        assertEquals(new Envelope(0, 128, 0, 128), method.getDataEnvelope());
    }
    
}
