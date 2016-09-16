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
import java.util.Collections;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.fracgis.Data;
import org.thema.fracgis.method.vector.mono.BoxCountingMethod;
import org.thema.fracgis.method.vector.mono.DilationMethod;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author Gilles Vuidel
 */
public class BatchVectorMethodTest {
    
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
       
        Data.loadVector(0);
    }
    

    /**
     * Test of execute method, of class BatchVectorMethod.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");

        BatchVectorMethod batch = new BatchVectorMethod(new FeatureLayer("point", Data.covPoint.getFeatures()), new DilationMethod(), 16);
        batch.execute(new TaskMonitor.EmptyMonitor());
        List<Feature> results = batch.getResults();
        assertEquals(2, results.size());
        for(Feature f : results) {
            assertEquals(0.0, (Double)f.getAttribute("Dim"), 1e-10);
            assertEquals(0.0, (Double)f.getAttribute("CI_min"), 1e-10);
            assertEquals(0.0, (Double)f.getAttribute("CI_max"), 1e-10);
            assertEquals(0.0, (Double)f.getAttribute("CI_delta"), 1e-10);
        }
        
        batch = new BatchVectorMethod(new FeatureLayer("line", Data.covLine.getFeatures()), new BoxCountingMethod(), 16);
        batch.execute(new TaskMonitor.EmptyMonitor());
        results = batch.getResults();
        assertEquals(8, results.size());
        for(Feature f : results) {
            assertEquals(0.84, (Double)f.getAttribute("Dim"), 1e-2);
        }
        
        batch = new BatchVectorMethod(new FeatureLayer("square", Data.covSquare.getFeatures()), new DilationMethod(), 32);
        batch.execute(new TaskMonitor.EmptyMonitor());
        results = batch.getResults();
        assertEquals(4, results.size());
        for(Feature f : results) {
            assertEquals(1.49, (Double)f.getAttribute("Dim"), 1e-2);
        }
        
        BoxCountingMethod boxCountingMethod = new BoxCountingMethod("frac", new DefaultSampling(1, 81, 3), new DefaultFeatureCoverage(Collections.EMPTY_LIST), 1, false);
        batch = new BatchVectorMethod(new FeatureLayer("frac", Data.covFrac.getFeatures()), boxCountingMethod, 81);
        batch.execute(new TaskMonitor.EmptyMonitor());
        results = batch.getResults();
        assertEquals(5, results.size());
        for(Feature f : results) {
            assertEquals(1.465, (Double)f.getAttribute("Dim"), 1e-4);
            assertEquals(1.0, (Double)f.getAttribute("R2"), 1e-10);
        }
    }
 
}
