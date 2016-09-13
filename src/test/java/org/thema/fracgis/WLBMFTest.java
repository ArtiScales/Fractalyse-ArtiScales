/*
 * Copyright (C) 2016 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 * http://thema.univ-fcomte.fr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.thema.fracgis;

import org.thema.fracgis.method.raster.multi.WLBMF;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Gilles Vuidel
 */
public class WLBMFTest {
    
    public WLBMFTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of DxLx2d method, of class WLBMF.
     */
    @Test
    public void testDxLx2d() {
        System.out.println("DxLx2d");
        RealMatrix m = MatrixUtils.createRealMatrix(16, 16);
        m.scalarAdd(1);
        int nwt = 3;
        double gamint = 0.0;
        WLBMF instance = new WLBMF();
        instance.DxLx2d(m.getData(), nwt, gamint);
        
    }
    
    @Test
    public void testConv2() {
        System.out.println("conv2");
        double[][] data = new double[][] {
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 }
        };
        
        WLBMF instance = new WLBMF();
        double[][] conv2 = instance.conv2(data, new double[] {0.332671, -0.806892, 0.459878, 0.135011, -0.085441, -0.035226});
        
        double [][] results = new double [][] {
            { 0.332671, -0.474221, -0.014343,  0.120668, -0.297444, 0.474221, 0.014343, -0.120668, -0.035226 },
            { 0.332671, -0.474221, -0.014343,  0.120668, -0.297444, 0.474221, 0.014343, -0.120668, -0.035226 },
            { 0.332671, -0.474221, -0.014343,  0.120668, -0.297444, 0.474221, 0.014343, -0.120668, -0.035226 },
            { 0.332671, -0.474221, -0.014343,  0.120668, -0.297444, 0.474221, 0.014343, -0.120668, -0.035226 }
        };
        for(int i = 0; i < results.length; i++) {
            assertArrayEquals(results[i], conv2[i], 1e-5);
        }
    }
    
    @Test
    public void testConv2Trans() {
        System.out.println("conv2Trans");
        double[][] data = new double[][] {
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 }
        };
        
        WLBMF instance = new WLBMF();
        double[][] conv2 = instance.conv2Trans(data, new double[] {0.332671, -0.806892, 0.459878, 0.135011, -0.085441, -0.035226});
        
        double [][] results = new double [][] {
            { 0.332671,   0.332671,   0.332671,   0.332671  },
            { -0.474221,  -0.474221,  -0.474221,  -0.474221 },
            { -0.014343,  -0.014343,  -0.014343,  -0.014343 },
            {  0.120668,   0.120668,   0.120668,   0.120668 },
            { -0.297444,  -0.297444,  -0.297444,  -0.297444 },
            {  0.474221,   0.474221,   0.474221,   0.474221 },
            {  0.014343,   0.014343,   0.014343,   0.014343 },
            { -0.120668,  -0.120668,  -0.120668,  -0.120668 },
            { -0.035226,  -0.035226,  -0.035226,  -0.035226 }
        };
        for(int i = 0; i < results.length; i++) {
            assertArrayEquals(results[i], conv2[i], 1e-5);
        }
    }
}
