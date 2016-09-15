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

import org.thema.fracgis.method.raster.mono.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
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
public class RadialMethodTest {

    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadVector(0);
    }
    

    /**
     * Test of execute method, of class RadialMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        Coordinate centre = new Coordinate(0, 0);
        RadialSampling sampling = new RadialSampling(centre, 5);
        RadialMethod instance = new RadialMethod("testPoint", sampling, Data.covPoint, centre, BufferParameters.CAP_SQUARE);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0), new ArrayList<>(instance.getCurve().values()));
        
        centre = new Coordinate(0, 32);
        instance = new RadialMethod("testLine", sampling, Data.covLine, centre, BufferParameters.CAP_SQUARE);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0), new ArrayList<>(instance.getCurve().values()));
        
        centre = new Coordinate(32, 32);
        instance = new RadialMethod("testSquare", sampling, Data.covSquare, centre, BufferParameters.CAP_SQUARE);
        instance.execute(new TaskMonitor.EmptyMonitor(), false);
        assertEquals(Arrays.asList(1.0, 4.0, 9.0, 16.0, 25.0), new ArrayList<>(instance.getCurve().values()));
    }
    
}
