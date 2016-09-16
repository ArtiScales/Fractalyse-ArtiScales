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
package org.thema.fracgis.estimation;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.thema.fracgis.estimation.DirectEstimation.Function;

/**
 *
 * @author Gilles Vuidel
 */
public class DirectEstimationTest {
    
    
    private DirectEstimation instance1, instance2, instance3, instance4, instance5;
    
    @Before
    public void setUp() {
        AbstractEstimationTest.VirtualMethod method = new AbstractEstimationTest.VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0), Arrays.asList(1.0, 4.0, 9.0, 16.0, 25.0));
        method.setDimSign(1);
        instance1 = new DirectEstimation(method);

        method = new AbstractEstimationTest.VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0), Arrays.asList(256.0, 64.0, 16.0, 4.0, 1.0));
        method.setDimSign(-1);
        instance2 = new DirectEstimation(method);
        
        method = new AbstractEstimationTest.VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0), Arrays.asList(1.0, 1.0, 2.0, 1.0, 1.0));
        method.setDimSign(1);
        instance3 = new DirectEstimation(method);
        
        method = new AbstractEstimationTest.VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0), Arrays.asList(256.0, 64.0, 16.0, 8.0, 4.0));
        method.setDimSign(-1);
        instance4 = new DirectEstimation(method);
        
    }
    

    /**
     * Test of getModels method, of class DirectEstimation.
     */
    @Test
    public void testGetModels() {
        System.out.println("getModels");
        for(Object model : instance2.getModels()) {
            if(model instanceof Function) {
                assertTrue(((Function)model).hasParamA());
            }
        }
    }

    /**
     * Test of getCoef method, of class DirectEstimation.
     */
    @Test
    public void testGetCoef() {
        System.out.println("getCoef");
        assertArrayEquals(new double[]{2.0, 1.0, 0.0}, instance1.getCoef(), 2e-3);
        assertArrayEquals(new double[]{-2.0, 256.0, 0.0}, instance2.getCoef(), 1e-2);
    }

    /**
     * Test of getDimension method, of class DirectEstimation.
     */
    @Test
    public void testGetDimension() {
        System.out.println("getDimension");
        assertEquals(2.0, instance1.getDimension(), 1e-3);
        for(int i = 0; i < instance1.getModels().size(); i++) {
            instance1.setModel(i);
            assertEquals(2.0, instance1.getDimension(), 1e-3);
        }
        assertEquals(2.0, instance2.getDimension(), 1e-3);
        assertEquals(0.0, instance3.getDimension(), 1e-3);
        
        instance4.setRange(1, 4);
        assertEquals(2.0, instance4.getDimension(), 1e-3);
        
        instance4.setRange(4, 16);
        assertEquals(1.0, instance4.getDimension(), 1e-3);
        
    }

    /**
     * Test of getR2 method, of class DirectEstimation.
     */
    @Test
    public void testGetR2() {
        System.out.println("getR2");
        assertEquals(1.0, instance1.getR2(), 1e-7);
        assertEquals(1.0, instance2.getR2(), 1e-7);
        assertEquals(0.0, instance3.getR2(), 1e-7);
    }


    /**
     * Test of getBootStrapConfidenceInterval method, of class DirectEstimation.
     */
    @Test
    public void testGetBootStrapConfidenceInterval() {
        System.out.println("getBootStrapConfidenceInterval");
        instance1.setModel(1);
        assertArrayEquals(new double [] {2.0, 2.0}, instance1.getBootStrapConfidenceInterval(), 1e-3);
        instance2.setModel(1);
        assertArrayEquals(new double [] {2.0, 2.0}, instance2.getBootStrapConfidenceInterval(), 1e-3);
    }

    /**
     * Test of getEstimValue method, of class DirectEstimation.
     */
    @Test
    public void testGetEstimValue() {
        System.out.println("getEstimValue");
        assertEquals(256, instance2.getEstimValue(1), 1e-2);
        assertEquals(16, instance2.getEstimValue(4), 1e-2);
        assertEquals(1, instance2.getEstimValue(16), 1e-2);
    }


    /**
     * Test of getType method, of class DirectEstimation.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        assertEquals(EstimationFactory.Type.DIRECT, instance1.getType());
    }


    
}
