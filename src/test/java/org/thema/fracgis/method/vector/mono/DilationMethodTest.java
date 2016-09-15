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

import com.vividsolutions.jts.operation.buffer.BufferParameters;
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
import org.thema.fracgis.sampling.Sampling;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author Gilles Vuidel
 */
public class DilationMethodTest {
    

    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadVector(0);
    }


    /**
     * Test of execute method, of class DilationMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        
        DilationMethod instance = new DilationMethod("testPoint", new DefaultSampling(), Data.covPoint, true, true);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertArrayEquals(new double [] {2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 1.667}, 
                ArrayUtils.toPrimitive(instance.getCurve().values().toArray(new Double[0])), 1e-3);

        DefaultSampling sampling = new DefaultSampling(1, 64, 2, Sampling.Sequence.GEOM);
        instance = new DilationMethod("testLine", sampling, Data.covLine, false, false);
        instance.setBufParam(new BufferParameters(BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_SQUARE));
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        // = 2 lines * (64 / 2r + 1) 
        assertEquals(Arrays.asList(130.0, 66.0, 34.0, 18.0, 10.0, 6.0, 4.0), new ArrayList<>(instance.getCurve().values()));
        
        instance = new DilationMethod("testSquare", sampling, Data.covSquare, false, false);
        instance.setBufParam(new BufferParameters(BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_SQUARE, BufferParameters.JOIN_MITRE, 100));
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        // 64^2 / (4*r^2) + 64 / r + 1
        assertEquals(Arrays.asList(4225.0, 1089.0, 289.0, 81.0, 25.0, 9.0, 4.0), new ArrayList<>(instance.getCurve().values()));
    }
}
