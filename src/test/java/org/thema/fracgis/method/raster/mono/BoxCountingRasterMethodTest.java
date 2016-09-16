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
package org.thema.fracgis.method.raster.mono;

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
import org.thema.fracgis.sampling.RasterBoxSampling;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author Gilles Vuidel
 */
public class BoxCountingRasterMethodTest {
    

    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadRaster();
    }


    /**
     * Test of execute method, of class BoxCountingRasterMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        RasterBoxSampling sampling = new RasterBoxSampling(new DefaultSampling(1, 16, 2));
        BoxCountingRasterMethod instance = new BoxCountingRasterMethod("testPoint", sampling, Data.imgPoint, Data.env16);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0), new ArrayList<>(instance.getCurve().values()));
        
        instance = new BoxCountingRasterMethod("testLine", sampling, Data.imgLine, Data.env16);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(16.0, 8.0, 4.0, 2.0, 1.0), new ArrayList<>(instance.getCurve().values()));
        
        instance = new BoxCountingRasterMethod("testSquare", sampling, Data.imgSquare, Data.env16);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(256.0, 64.0, 16.0, 4.0, 1.0), new ArrayList<>(instance.getCurve().values()));
        
        sampling = new RasterBoxSampling(new DefaultSampling(1, 729, 3));
        instance = new BoxCountingRasterMethod("testFrac", sampling, Data.imgFrac, Data.envFrac);
        instance.execute(new TaskMonitor.EmptyMonitor(), true);
        assertEquals(Arrays.asList(15625.0, 3125.0, 625.0, 125.0, 25.0, 5.0, 1.0), new ArrayList<>(instance.getCurve().values()));
        
    }


}
