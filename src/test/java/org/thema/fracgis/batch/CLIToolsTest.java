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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.fracgis.estimation.EstimationFactory.Type;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.sampling.Sampling;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author Gilles Vuidel
 */
public class CLIToolsTest {

    /**
     * Test of execute method, of class CLITools.
     */
    @Test
    public void testExecute() throws Exception {
        System.out.println("execute");
        CLITools.execute(new String[]{"--rasterize", "res=1", "target/test-classes/org/thema/fracgis/tapis_n5_point.shp"});
        assertTrue(new File("target/test-classes/org/thema/fracgis/tapis_n5_point.tif").exists());
        
        CLITools.execute(new String[]{"-proc", "2", "--rboxcounting", "target/test-classes/org/thema/fracgis/tapis_n5_point.tif"});
        assertEquals(ParallelExecutor.getNbProc(), 2);
        assertEquals(ParallelFExecutor.getNbProc(), 2);
        assertTrue(new File("target/test-classes/org/thema/fracgis/tapis_n5_pointrbox_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        assertTrue(new File("rbox_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        
        CLITools.execute(new String[]{"--rdilation", "seq=arith", "max=100", "target/test-classes/org/thema/fracgis/tapis_n5_point.tif"});
        assertTrue(new File("target/test-classes/org/thema/fracgis/tapis_n5_pointrdil_coef2.00000_min0.00000_max100.000_seqarith_estimdirect.txt").exists());
        assertTrue(new File("rdil_coef2.00000_min0.00000_max100.000_seqarith_estimdirect.txt").exists());
        
        CLITools.execute(new String[]{"--rcorrelation", "target/test-classes/org/thema/fracgis/tapis_n5_point.tif"});
        assertTrue(new File("target/test-classes/org/thema/fracgis/tapis_n5_pointrcor_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        assertTrue(new File("rcor_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        
        CLITools.execute(new String[]{"--boxcounting", "target/test-classes/org/thema/fracgis/tapis_n5_point.shp"});
        assertTrue(new File("target/test-classes/org/thema/fracgis/tapis_n5_pointbox_glid1_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        assertTrue(new File("box_glid1_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        
        CLITools.execute(new String[]{"--boxcounting", "gliding=4", "target/test-classes/org/thema/fracgis/tapis_n5_point.shp"});
        assertTrue(new File("target/test-classes/org/thema/fracgis/tapis_n5_pointbox_glid4_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        assertTrue(new File("box_glid4_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        
        CLITools.execute(new String[]{"--correlation", "target/test-classes/org/thema/fracgis/tapis_n5_point.shp"});
        assertTrue(new File("target/test-classes/org/thema/fracgis/tapis_n5_pointcor_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        assertTrue(new File("cor_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        
        CLITools.execute(new String[]{"--dilation", "target/test-classes/org/thema/fracgis/tapis_n5_point.shp"});
        assertTrue(new File("target/test-classes/org/thema/fracgis/tapis_n5_pointdil_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
        assertTrue(new File("dil_coef2.00000_min0.00000_max0.00000_seqgeom_estimlog.txt").exists());
    }

    /**
     * Test of getSampling method, of class CLITools.
     */
    @Test
    public void testGetSampling() {
        System.out.println("getSampling");
        List<String> args = new ArrayList<>(Arrays.asList("seq=geom", "coef=1.5", "min=1", "max=100"));
        DefaultSampling result = CLITools.getSampling(args);
        assertTrue(args.isEmpty());
        assertEquals(new DefaultSampling(1, 100, 1.5, Sampling.Sequence.GEOM), result);
        
        args = new ArrayList<>(Arrays.asList("coef=10", "min=10", "max=100", "seq=arith"));
        result = CLITools.getSampling(args);
        assertTrue(args.isEmpty());
        assertEquals(new DefaultSampling(10, 100, 10, Sampling.Sequence.ARITH), result);
        
        args = new ArrayList<>(Arrays.asList("whatelse"));
        result = CLITools.getSampling(args);
        assertEquals(1, args.size());
        assertEquals(new DefaultSampling(), result);
    }

    /**
     * Test of getEstim method, of class CLITools.
     */
    @Test
    public void testGetEstim() {
        System.out.println("getEstim");
        
        DefaultSampling sampling = new DefaultSampling();
        assertEquals(sampling.getDefaultEstimType(), CLITools.getEstim(Collections.EMPTY_LIST, sampling));
        
        List<String> args = new ArrayList<>(Arrays.asList("whatelse"));
        assertEquals(sampling.getDefaultEstimType(), CLITools.getEstim(args, sampling));
        
        args = new ArrayList<>(Arrays.asList("estim=log"));
        assertEquals(Type.LOG, CLITools.getEstim(args, sampling));
        assertTrue(args.isEmpty());
        
        args = new ArrayList<>(Arrays.asList("estim=direct", "whatelse"));
        assertEquals(Type.DIRECT, CLITools.getEstim(args, sampling));
        assertEquals(1, args.size());
    }
    
}
