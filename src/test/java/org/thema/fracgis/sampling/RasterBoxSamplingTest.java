
package org.thema.fracgis.sampling;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import org.geotools.geometry.jts.JTS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.FeatureCoverage;

/**
 *
 * @author Gilles Vuidel
 */
public class RasterBoxSamplingTest {
    
    private static BufferedImage img10;
    private static Envelope env100;
    
    @BeforeClass
    public static void setUpClass() {
        img10 = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_BINARY);
        env100 = new Envelope(0, 100, 0, 100);
    }

    /**
     * Test of getValues method, of class DefaultSampling.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
        RasterBoxSampling sampling = new RasterBoxSampling(new DefaultSampling(1, 100, 20, Sampling.Sequence.ARITH));
        sampling.updateSampling(img10, env100);
        assertEquals(new TreeSet<>(Arrays.asList(10.0, 30.0, 50.0, 70.0, 90.0)), sampling.getValues());
        
        sampling = new RasterBoxSampling(new DefaultSampling(1, 200, 2, Sampling.Sequence.GEOM));
        sampling.updateSampling(img10, env100);
        assertEquals(new TreeSet<>(Arrays.asList(10.0, 20.0, 40.0, 80.0, 160.0)), sampling.getValues());
        
    }
    
    /**
     * Test of getDiscreteValues method, of class DefaultSampling.
     */
    @Test
    public void testGetDiscreteValues() {
        System.out.println("getDiscreteValues");

        RasterBoxSampling sampling = new RasterBoxSampling(new DefaultSampling(1, 100, 20, Sampling.Sequence.ARITH));
        sampling.updateSampling(img10, env100);
        assertEquals(new TreeSet<>(Arrays.asList(1, 3, 5, 7, 9)), sampling.getDiscreteValues());
        
        sampling = new RasterBoxSampling(new DefaultSampling(1, 200, 2, Sampling.Sequence.GEOM));
        sampling.updateSampling(img10, env100);
        assertEquals(new TreeSet<>(Arrays.asList(1, 2, 4, 8, 16)), sampling.getDiscreteValues());
    }
    
}
