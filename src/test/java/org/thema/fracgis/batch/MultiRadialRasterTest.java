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
package org.thema.fracgis.batch;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.swing.TaskMonitor;
import org.thema.fracgis.Data;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author Gilles Vuidel
 */
public class MultiRadialRasterTest {
    
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadRaster();
    }

    /**
     * Test of execute method, of class MultiRadialRaster.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        MultiRadialRaster multiRadial = new MultiRadialRaster(Data.imgPoint, Data.env16, 16, false, 0.0, true);
        multiRadial.execute(new TaskMonitor.EmptyMonitor());
        for(int y = 0; y < 16; y++) {
            for(int x = 0; x < 16; x++) {
                if(x == 7 && y == 7) {
                    assertEquals(0.0, multiRadial.getRasterDim().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(Double.NaN, multiRadial.getRasterR2().getSampleDouble(x, y, 0), 0.0);
                    assertEquals(0.0, multiRadial.getRasterDmin().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(0.0, multiRadial.getRasterDmax().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(0.0, multiRadial.getRasterDinter().getSampleDouble(x, y, 0), 1e-10);
                } else {
                    assertTrue(Double.isNaN(multiRadial.getRasterDim().getSampleDouble(x, y, 0)));
                    assertTrue(Double.isNaN(multiRadial.getRasterR2().getSampleDouble(x, y, 0)));
                    assertTrue(Double.isNaN(multiRadial.getRasterDmin().getSampleDouble(x, y, 0)));
                    assertTrue(Double.isNaN(multiRadial.getRasterDmax().getSampleDouble(x, y, 0)));
                    assertTrue(Double.isNaN(multiRadial.getRasterDinter().getSampleDouble(x, y, 0)));
                }
            }
        }
        
        multiRadial = new MultiRadialRaster(Data.imgLine, Data.env16, 7, false, 0.0, true);
        multiRadial.execute(new TaskMonitor.EmptyMonitor());
        for(int y = 0; y < 16; y++) {
            for(int x = 0; x < 16; x++) {
                if(y == 7 && x >= 3 && x < 13) {
                    assertEquals(1.0, multiRadial.getRasterDim().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(1.0, multiRadial.getRasterR2().getSampleDouble(x, y, 0), 0.0);
                    assertEquals(1.0, multiRadial.getRasterDmin().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(1.0, multiRadial.getRasterDmax().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(0.0, multiRadial.getRasterDinter().getSampleDouble(x, y, 0), 1e-10);
                }
            }
        }
        
        multiRadial = new MultiRadialRaster(Data.imgSquare, Data.env16, 7, true, 0.0, true);
        multiRadial.execute(new TaskMonitor.EmptyMonitor());
        for(int y = 0; y < 16; y++) {
            for(int x = 0; x < 16; x++) {
                if(y >= 3 && y < 13 && x >= 3 && x < 13) {
                    assertEquals(2.0, multiRadial.getRasterDim().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(1.0, multiRadial.getRasterR2().getSampleDouble(x, y, 0), 0.0);
                    assertEquals(2.0, multiRadial.getRasterDmin().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(2.0, multiRadial.getRasterDmax().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(0.0, multiRadial.getRasterDinter().getSampleDouble(x, y, 0), 1e-10);
                    assertEquals(7.0, multiRadial.getRasterDistMax().getSampleDouble(x, y, 0), 1e-10);
                }
            }
        }
        
    }
    
}
