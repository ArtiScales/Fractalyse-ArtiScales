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
public class BoxCountingMethodTest {
    

    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadVector(0.01);
    }


    /**
     * Test of execute method, of class BoxCountingMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        DefaultSampling sampling = new DefaultSampling(1, 64, 2, Sampling.Sequence.GEOM);
        BoxCountingMethod instance = new BoxCountingMethod("testPoint", sampling, Data.covPoint, 1, false);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0), new ArrayList<>(instance.getCurve().values()));

        instance = new BoxCountingMethod("testLine", sampling, Data.covLine, 1, false);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(128.0, 64.0, 32.0, 16.0, 8.0, 4.0, 2.0), new ArrayList<>(instance.getCurve().values()));
        
        instance = new BoxCountingMethod("testSquare", sampling, Data.covSquare, 1, false);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(4096.0, 1024.0, 256.0, 64.0, 16.0, 4.0, 1.0), new ArrayList<>(instance.getCurve().values()));
        
        sampling = new DefaultSampling(new DefaultSampling(1, 243, 3));
        instance = new BoxCountingMethod("testFrac", sampling, Data.covFrac, 1, false);
        instance.execute(new TaskMonitor.EmptyMonitor(), true);
        assertEquals(Arrays.asList(3125.0, 625.0, 125.0, 25.0, 5.0, 1.0), new ArrayList<>(instance.getCurve().values()));
    }
}
