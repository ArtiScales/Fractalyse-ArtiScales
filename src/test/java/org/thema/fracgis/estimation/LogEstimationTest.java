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
import org.thema.fracgis.estimation.AbstractEstimationTest.VirtualMethod;

/**
 *
 * @author Gilles Vuidel
 */
public class LogEstimationTest {
    
    private LogEstimation instance1, instance2, instance3, instance4;
    
    @Before
    public void setUp() {
        VirtualMethod method = new VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0), Arrays.asList(1.0, 4.0, 9.0, 16.0, 25.0));
        method.setDimSign(1);
        instance1 = new LogEstimation(method);

        method = new VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0), Arrays.asList(256.0, 64.0, 16.0, 4.0, 1.0));
        method.setDimSign(-1);
        instance2 = new LogEstimation(method);
        
        method = new VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0), Arrays.asList(1.0, 1.0, 2.0, 1.0, 1.0));
        instance3 = new LogEstimation(method);
        
        method = new VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0), Arrays.asList(256.0, 64.0, 16.0, 8.0, 4.0));
        instance4 = new LogEstimation(method);
    }
    

    /**
     * Test of getDimension method, of class LogEstimation.
     */
    @Test
    public void testGetDimension() {
        System.out.println("getDimension");
        assertEquals(2.0, instance1.getDimension(), 0.0);
        assertEquals(2.0, instance2.getDimension(), 0.0);
        assertEquals(0.0, instance3.getDimension(), 1e-15);
        assertEquals(1.5, instance4.getDimension(), 1e-15);
        
        instance4.setRange(1, 4);
        assertEquals(2.0, instance4.getDimension(), 1e-15);
        
        instance4.setRange(4, 16);
        assertEquals(1.0, instance4.getDimension(), 1e-15);
    }

    /**
     * Test of getR2 method, of class LogEstimation.
     */
    @Test
    public void testGetR2() {
        System.out.println("getR2");
        assertEquals(1.0, instance1.getR2(), 0.0);
        assertEquals(1.0, instance2.getR2(), 0.0);
        assertEquals(0.0, instance3.getR2(), 0.0);
    }

    /**
     * Test of getSignificance method, of class LogEstimation.
     */
    @Test
    public void testGetSignificance() {
        System.out.println("getSignificance");
        assertEquals(0.0, instance1.getSignificance(), 0.0);
        assertEquals(0.0, instance2.getSignificance(), 0.0);
        assertEquals(1.0, instance3.getSignificance(), 0.0);
    }

    /**
     * Test of getConfidenceInterval method, of class LogEstimation.
     */
    @Test
    public void testGetConfidenceInterval() {
        System.out.println("getConfidenceInterval");
        assertEquals(0.0, instance1.getConfidenceInterval(), 0.0);
        assertEquals(0.0, instance2.getConfidenceInterval(), 0.0);
        assertEquals(0.52, instance3.getConfidenceInterval(), 1e-3);
    }

    /**
     * Test of getBootStrapConfidenceInterval method, of class LogEstimation.
     */
    @Test
    public void testGetBootStrapConfidenceInterval() {
        System.out.println("getBootStrapConfidenceInterval");
        assertArrayEquals(new double [] {2.0, 2.0}, instance1.getBootStrapConfidenceInterval(), 1e-10);
        assertArrayEquals(new double [] {2.0, 2.0}, instance2.getBootStrapConfidenceInterval(), 1e-10);
        assertArrayEquals(new double [] {-0.5, 0.5}, instance3.getBootStrapConfidenceInterval(), 0.07);
    }

    /**
     * Test of getCoef method, of class LogEstimation.
     */
    @Test
    public void testGetCoef() {
        System.out.println("getCoef");
        assertArrayEquals(new double [] {2.0, 0.0}, instance1.getCoef(), 0.0);
        assertArrayEquals(new double [] {-2.0, 5.545}, instance2.getCoef(), 1e-3);
    }

    /**
     * Test of getEstimValue method, of class LogEstimation.
     */
    @Test
    public void testGetEstimValue() {
        System.out.println("getEstimValue");
        assertEquals(256, instance2.getEstimValue(1), 1e-10);
        assertEquals(16, instance2.getEstimValue(4), 1e-10);
        assertEquals(1, instance2.getEstimValue(16), 1e-10);
    }

    /**
     * Test of getType method, of class LogEstimation.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");

        assertEquals(EstimationFactory.Type.LOG, instance1.getType());
    }
    
}
