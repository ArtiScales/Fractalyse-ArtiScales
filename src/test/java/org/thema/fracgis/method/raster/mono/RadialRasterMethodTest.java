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

import com.vividsolutions.jts.geom.Coordinate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.swing.TaskMonitor;
import org.thema.fracgis.Data;
import org.thema.fracgis.sampling.RadialSampling;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author Gilles Vuidel
 */
public class RadialRasterMethodTest {

    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadRaster();
    }
    

    /**
     * Test of execute method, of class RadialRasterMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        Coordinate centre = new Coordinate(7.5, 8.5);
        RadialSampling sampling = new RadialSampling(centre, 9);
        RadialRasterMethod instance = new RadialRasterMethod("testPoint", sampling, Data.imgPoint, Data.env16);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0), new ArrayList<>(instance.getCurve().values()));
        
        instance = new RadialRasterMethod("testLine", sampling, Data.imgLine, Data.env16);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(1.0, 3.0, 5.0, 7.0, 9.0), new ArrayList<>(instance.getCurve().values()));
        
        instance = new RadialRasterMethod("testSquare", sampling, Data.imgSquare, Data.env16);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(1.0, 9.0, 25.0, 49.0, 81.0), new ArrayList<>(instance.getCurve().values()));
        
    }
    
}
