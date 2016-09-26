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
package org.thema.fracgis.method.vector.multi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.swing.TaskMonitor;
import org.thema.fracgis.Data;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author Gilles Vuidel
 */
public class MultiFracBoxCountingVectorMethodTest {
    
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadVector(0.01);
    }

    /**
     * Test of execute method, of class MultiFracBoxCountingVectorMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        DefaultSampling sampling = new DefaultSampling(1, 16, 2);
        MultiFracBoxCountingVectorMethod instance = new MultiFracBoxCountingVectorMethod("testPoint", sampling, Data.covPoint);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(4.0, 4.0, 4.0, 4.0, 4.0), new ArrayList<>(instance.getCurve(-1).values()));
        assertEquals(Arrays.asList(2.0, 2.0, 2.0, 2.0, 2.0), new ArrayList<>(instance.getCurve(0).values()));
        assertEquals(Arrays.asList(0.5, 0.5, 0.5, 0.5, 0.5), new ArrayList<>(instance.getCurve(1).values()));
        assertEquals(Arrays.asList(0.5, 0.5, 0.5, 0.5, 0.5), new ArrayList<>(instance.getCurve(2).values()));
        
        instance = new MultiFracBoxCountingVectorMethod("testLine", sampling, Data.covLine);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(128.0, 64.0, 32.0, 16.0, 8.0), new ArrayList<>(instance.getCurve(0).values()));
        assertArrayEquals(new double [] {1/128.0, 1/64.0, 1/32.0, 1/16.0, 1/8.0}, 
                ArrayUtils.toPrimitive(instance.getCurve(2).values().toArray(new Double[0])), 1e-4);
        
        instance = new MultiFracBoxCountingVectorMethod("testSquare", sampling, Data.covSquare);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        //assertEquals(Arrays.asList(4096*4096, 1024*1024, 65536.0, 4096.0, 256.0), new ArrayList<>(instance.getCurve(-1).values()));
        assertEquals(Arrays.asList(4096.0, 1024.0, 256.0, 64.0, 16.0), new ArrayList<>(instance.getCurve(0).values()));
        assertArrayEquals(new double [] {1/4096.0, 1/1024.0, 1/256.0, 1/64.0, 1/16.0}, 
                ArrayUtils.toPrimitive(instance.getCurve(2).values().toArray(new Double[0])), 1e-4);
        
        sampling = new DefaultSampling(1, 243, 3);
        instance = new MultiFracBoxCountingVectorMethod("testFrac", sampling, Data.covFrac);
        instance.execute(new TaskMonitor.EmptyMonitor(), true);
        assertArrayEquals(new double [] {3125.0*3125.0, 390625.0, 15625.0, 625.0, 25.0, 1.0}, 
                ArrayUtils.toPrimitive(instance.getCurve(-1).values().toArray(new Double[0])), 1e-8);
        assertEquals(Arrays.asList(3125.0, 625.0, 125.0, 25.0, 5.0, 1.0), new ArrayList<>(instance.getCurve(0).values()));
        assertArrayEquals(new double [] {1/3125.0, 1/625.0, 1/125.0, 1/25.0, 1/5.0, 1/1.0}, 
                ArrayUtils.toPrimitive(instance.getCurve(2).values().toArray(new Double[0])), 1e-10);
        
    }


}
