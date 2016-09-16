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
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.thema.common.ProgressBar;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.vector.mono.MonoVectorMethod;

/**
 *
 * @author Gilles Vuidel
 */

public class AbstractEstimationTest {
    
    private AbstractEstimation instance1, instance2;
    
    @Before
    public void setUp() {
        VirtualMethod method = new VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0), Arrays.asList(256.0, 64.0, 16.0, 4.0, 1.0));
        instance1 = new AbstractEstimationImpl(method);
        
        method = new VirtualMethod();
        method.setCurve(Arrays.asList(1.0, 2.0, 4.0, 8.0, 16.0), Arrays.asList(256.0, 64.0, 16.0, 8.0, 4.0));
        instance2 = new AbstractEstimationImpl(method);
    }
    

    /**
     * Test of getScalingBehaviour method, of class AbstractEstimation.
     */
    @Test
    public void testGetScalingBehaviour() {
        System.out.println("getScalingBehaviour");
        assertArrayEquals(new double[]{2.0, 2.0, 2.0, 2.0}, instance1.getScalingBehaviour()[1], 1e-10);
        assertArrayEquals(new double[]{2.0, 2.0, 1.0, 1.0}, instance2.getScalingBehaviour()[1], 1e-10);
    }

    /**
     * Test of getSmoothedScalingBehaviour method, of class AbstractEstimation.
     */
    @Test
    public void testGetSmoothedScalingBehaviour() {
        System.out.println("getSmoothedScalingBehaviour");
        assertArrayEquals(new double[]{2.0, 2.0, 2.0, 2.0}, instance1.getSmoothedScalingBehaviour(1)[1], 1e-10);
        assertArrayEquals(new double[]{2.0, 2.0, 1.0, 1.0}, instance2.getSmoothedScalingBehaviour(0.1)[1], 1e-2);
        assertArrayEquals(new double[]{1.767, 1.597, 1.402, 1.232}, instance2.getSmoothedScalingBehaviour(0.5)[1], 1e-3);
    }

    /**
     * Test of getInflexPointIndices method, of class AbstractEstimation.
     */
    @Test
    public void testGetInflexPointIndices() {
        System.out.println("getInflexPointIndices");
        assertEquals(Collections.EMPTY_LIST, instance1.getInflexPointIndices(1, 0));
        assertEquals(Collections.singletonList(2), instance2.getInflexPointIndices(0.5, 0));
    }

    /**
     * Test of setRange method, of class AbstractEstimation.
     */
    @Test
    public void testSetRange() {
        System.out.println("setRange");
        instance1.setRange(3, 9);
        assertEquals(new Range(2, 16), instance1.getRange());
        instance1.setRange(-1, 100);
        assertEquals(new Range(1, 16), instance1.getRange());
    }

    /**
     * Test of getRangeCurve method, of class AbstractEstimation.
     */
    @Test
    public void testGetRangeCurve() {
        System.out.println("getRangeCurve");
        assertEquals(instance1.getCurve(), instance1.getRangeCurve());
        instance1.setRange(2, 8);
        assertEquals(3, instance1.getRangeCurve().size());
    }

    
    public static final class VirtualMethod extends MonoVectorMethod {
        private int dimSign = -1;

        public VirtualMethod() {
            setCurve(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0), Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));
        }
        
        @Override
        public String getName() {
            return "Virtual method";
        }
        @Override
        public void execute(ProgressBar monitor, boolean parallel) {
        }
        @Override
        public int getDimSign() {
            return dimSign;
        }
        public void setDimSign(int dimSign) {
            this.dimSign = dimSign;
        }
        public void setCurve(List<Double> x, List<Double> y) {
            this.curve = new TreeMap<>();
            for(int i = 0; i < x.size(); i++) {
                curve.put(x.get(i), y.get(i));
            }
        }

    };
    
    public static class AbstractEstimationImpl extends AbstractEstimation {

        public AbstractEstimationImpl(MonoMethod method) {
            super(method);
        }

        @Override
        public XYSeries getEstimationSerie() {
            return null;
        }

        @Override
        public void estimate() {
        }

        @Override
        public EstimationFactory.Type getType() {
            return EstimationFactory.Type.LOG;
        }

        @Override
        public double getEstimValue(double x) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double getDimension() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double getR2() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getResultInfo() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getParamInfo() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double[] getCoef() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double[] getBootStrapConfidenceInterval() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List getModels() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Object getModel() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setModel(int indModel) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
}
