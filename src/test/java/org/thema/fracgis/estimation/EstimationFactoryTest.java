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

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.thema.fracgis.estimation.EstimationFactory.Type;

/**
 *
 * @author Gilles Vuidel
 */
public class EstimationFactoryTest {
    
    EstimationFactory instance;
    AbstractEstimationTest.VirtualMethod method;
    
    @Before
    public  void setUpClass() {
        method = new AbstractEstimationTest.VirtualMethod();
        instance = new EstimationFactory(method);
    }

    /**
     * Test of getDefaultEstimation method, of class EstimationFactory.
     */
    @Test
    public void testGetDefaultEstimation() {
        System.out.println("getDefaultEstimation");
        assertTrue(instance.getDefaultEstimation().getType() == method.getSampling().getDefaultEstimType());
    }

    /**
     * Test of getEstimation method, of class EstimationFactory.
     */
    @Test
    public void testGetEstimation_EstimationFactoryType() {
        System.out.println("getEstimation");
        assertTrue(instance.getEstimation(Type.DIRECT).getType() == Type.DIRECT);
        assertTrue(instance.getEstimation(Type.LOG).getType() == Type.LOG);
    }

    /**
     * Test of getEstimation method, of class EstimationFactory.
     */
    @Test
    public void testGetEstimation_EstimationFactoryType_int() {
        System.out.println("getEstimation");
        assertTrue(instance.getEstimation(Type.DIRECT, 0).getType() == Type.DIRECT);
        assertTrue(instance.getEstimation(Type.LOG, 0).getType() == Type.LOG);
        Estimation estimation = instance.getEstimation(Type.DIRECT, 1);
        assertTrue(estimation.getModel().equals(estimation.getModels().get(1)));
    }
    
}
