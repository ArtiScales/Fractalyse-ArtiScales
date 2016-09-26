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
package org.thema.fracgis.method.vector.mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.swing.TaskMonitor;
import org.thema.fracgis.Data;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.sampling.Sampling;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author gvuidel
 */
public class CorrelationMethodTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadVector(0);
    }


    /**
     * Test of execute method, of class CorrelationnMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        DefaultSampling sampling = new DefaultSampling(1, 64, 2, Sampling.Sequence.GEOM);
        CorrelationMethod instance = new CorrelationMethod("testPoint", sampling, Data.covPoint);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0), new ArrayList<>(instance.getCurve().values()));
       
        sampling = new DefaultSampling(1, 243, 3, Sampling.Sequence.GEOM);
        instance = new CorrelationMethod("testFrac", sampling, Data.covFrac);
        instance.execute(new TaskMonitor.EmptyMonitor(), true);
        assertArrayEquals(new double [] {1.0, 3.0, 12.67, 56.64, 273.3, 1258.53}, 
                ArrayUtils.toPrimitive(instance.getCurve().values().toArray(new Double[0])), 1e-2);
        
    }
    
    /**
     * Test of execute method, of class CorrelationnMethod.
     */
    @Test
    public void testExecuteException() {
        System.out.println("execute");
        CorrelationMethod instance = new CorrelationMethod("testLi", new DefaultSampling(), Data.covLine);
        thrown.expect(IllegalArgumentException.class);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        
    }
    
}
