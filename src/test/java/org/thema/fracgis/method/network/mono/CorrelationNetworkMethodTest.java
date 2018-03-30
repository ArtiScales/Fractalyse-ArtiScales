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
package org.thema.fracgis.method.network.mono;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.fracgis.Data;
import static org.thema.fracgis.method.network.mono.LocalNetworkMethod.LENGTH_WEIGHT;
import static org.thema.fracgis.method.network.mono.LocalNetworkMethod.NO_WEIGHT;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.sampling.Sampling.Sequence;
import org.thema.graph.SpatialGraph;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author gvuidel
 */
public class CorrelationNetworkMethodTest {
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadNetVector();
    }
    

    /**
     * Test of execute method, of class CorrelationNetworkMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        CorrelationNetworkMethod method = new CorrelationNetworkMethod("test", new DefaultSampling(16, 512, 2), Data.netCross64, LENGTH_WEIGHT);
        method.execute(new TaskMonitor.EmptyMonitor(), true);
        assertEquals(Arrays.asList(16.0, 32.0, 64.0, 128.0, 256.0, 512.0), new ArrayList<>(method.getCurve().keySet()));
        assertEquals(Arrays.asList((8*4+4*8)/5.0, (16*4+4*16)/5.0, (32*4+4*32)/5.0, (64*4+4*64)/5.0, 256.0, 256.0), new ArrayList<>(method.getCurve().values()));
        
        method = new CorrelationNetworkMethod("test", new DefaultSampling(128, 330, 64, Sequence.ARITH), Data.netCross64, LENGTH_WEIGHT);
        method.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(128.0, 192.0, 256.0, 320.0), new ArrayList<>(method.getCurve().keySet()));
        assertEquals(Arrays.asList((64*4+4*64)/5.0, (64*4+4*(64+3*32))/5.0, 256.0, 256.0), new ArrayList<>(method.getCurve().values()));
        
        method = new CorrelationNetworkMethod("test", new DefaultSampling(0, 6, 2, Sequence.ARITH), Data.netCross64, NO_WEIGHT);
        method.execute(new TaskMonitor.EmptyMonitor(), true);
        assertEquals(Arrays.asList(0.0, 2.0, 4.0, 6.0), new ArrayList<>(method.getCurve().keySet()));
        assertEquals(Arrays.asList(0.0, (4+4*1)/5.0, (4+4*4)/5.0, (4+4*4)/5.0), new ArrayList<>(method.getCurve().values()));
        
        method = new CorrelationNetworkMethod("test", new DefaultSampling(2, 162, 3, Sequence.GEOM), Data.netFrac, LENGTH_WEIGHT);
        method.execute(new TaskMonitor.EmptyMonitor(), true);
        assertEquals(Arrays.asList(2.0, 6.0, 18.0, 54.0, 162.0), new ArrayList<>(method.getCurve().keySet()));
        assertArrayEquals(new double [] {2.0, 8.5, 42.13, 208.76, 955.51}, 
                ArrayUtils.toPrimitive(method.getCurve().values().toArray(new Double[0])), 1e-2);
    }

    /**
     * Test of getDataEnvelope method, of class CorrelationNetworkMethod.
     */
    @Test
    public void testGetDataEnvelope() {
        System.out.println("getDataEnvelope");
        LocalNetworkMethod method = new LocalNetworkMethod("test", new DefaultSampling(), Data.netCross64, null, LENGTH_WEIGHT);
        assertEquals(new Envelope(0, 128, 0, 128), method.getDataEnvelope());
    }
    
}
