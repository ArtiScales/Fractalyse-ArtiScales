
package org.thema.fracgis.sampling;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.TreeSet;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.sampling.Sampling.Sequence;

/**
 *
 * @author Gilles Vuidel
 */
public class DefaultSamplingTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    private static BufferedImage img10;
    private static Envelope env10, env100;
    private static FeatureCoverage covPoint10;
    private static FeatureCoverage covPoly10;
    
    @BeforeClass
    public static void setUpClass() {
        img10 = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_BINARY);
        env10 = new Envelope(0, 10, 0, 10);
        env100 = new Envelope(0, 100, 0, 100);
        covPoint10 = new DefaultFeatureCoverage(Arrays.asList(
            new DefaultFeature(1, new GeometryFactory().createPoint(new Coordinate(0, 0))),
            new DefaultFeature(2, new GeometryFactory().createPoint(new Coordinate(10, 10)))));
        covPoly10 = new DefaultFeatureCoverage(Arrays.asList(
            new DefaultFeature(1, JTS.toGeometry(env10))));
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of updateMaxSize method, of class DefaultSampling.
     */
    @Test
    public void testUpdateMaxSize() {
        System.out.println("updateMaxSize");
        DefaultSampling sampling = new DefaultSampling();
        sampling.updateMaxSize(env10);
        assertEquals(5.0, sampling.getMaxSize(), 0.0);
    }

    /**
     * Test of updateSampling method, of class DefaultSampling.
     */
    @Test
    public void testUpdateSampling_RenderedImage_Envelope() {
        System.out.println("updateSampling");

        DefaultSampling sampling = new DefaultSampling();
        sampling.updateSampling(img10, env10);
        assertEquals(1.0, sampling.getMinSize(), 0.0);
        assertEquals(5.0, sampling.getMaxSize(), 0.0);
    }

    /**
     * Test of updateSampling method, of class DefaultSampling.
     */
    @Test
    public void testUpdateSampling_GridCoverage2D() {
        System.out.println("updateSampling");
        GridCoverage2D grid = new GridCoverageFactory().create("test", img10, JTS.getEnvelope2D(env10, DefaultGeographicCRS.WGS84));
        DefaultSampling sampling = new DefaultSampling();
        sampling.updateSampling(grid);
        assertEquals(1.0, sampling.getMinSize(), 0.0);
        assertEquals(5.0, sampling.getMaxSize(), 0.0);
    }

    /**
     * Test of updateSampling method, of class DefaultSampling.
     */
    @Test
    public void testUpdateSampling_FeatureCoverage() {
        System.out.println("updateSampling");
        DefaultSampling sampling = new DefaultSampling();
        sampling.updateSampling(covPoint10);
        assertEquals(1.0, sampling.getMinSize(), 0.0);
        assertEquals(5.0, sampling.getMaxSize(), 0.0);
    }

    /**
     * Test of getValues method, of class DefaultSampling.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");
        assertEquals(new TreeSet<>(Arrays.asList(1.0, 2.0, 4.0, 8.0)), new DefaultSampling(1, 10, 2, Sampling.Sequence.GEOM).getValues());
        assertEquals(new TreeSet<>(Arrays.asList(1.0, 3.0, 5.0, 7.0, 9.0)), new DefaultSampling(1, 10, 2, Sampling.Sequence.ARITH).getValues());
    }

    /**
     * Test exception of getDiscreteValues method, of class DefaultSampling.
     */
    @Test
    public void testGetDiscreteValuesException() {
        System.out.println("getDiscreteValues");
        DefaultSampling sampling = new DefaultSampling();
        thrown.expect(IllegalArgumentException.class);
        sampling.getDiscreteValues();
    }
    
    /**
     * Test of getDiscreteValues method, of class DefaultSampling.
     */
    @Test
    public void testGetDiscreteValues() {
        System.out.println("getDiscreteValues");
        
        DefaultSampling sampling = new DefaultSampling(1, 10, 2, Sampling.Sequence.ARITH);
        sampling.updateSampling(img10, env10);
        assertEquals(new TreeSet<>(Arrays.asList(0, 1, 2, 3, 4)), sampling.getDiscreteValues());
        
        sampling = new DefaultSampling(1, 100, 20, Sampling.Sequence.ARITH);
        sampling.updateSampling(img10, env100);
        assertEquals(new TreeSet<>(Arrays.asList(0, 1, 2, 3, 4)), sampling.getDiscreteValues());
        
        sampling = new DefaultSampling(1, 20, 2, Sampling.Sequence.GEOM);
        sampling.updateSampling(img10, env10);
        assertEquals(new TreeSet<>(Arrays.asList(0, 1, 2, 4, 8)), sampling.getDiscreteValues());
        
        sampling = new DefaultSampling(1, 200, 2, Sampling.Sequence.GEOM);
        sampling.updateSampling(img10, env100);
        assertEquals(new TreeSet<>(Arrays.asList(0, 1, 2, 4, 8)), sampling.getDiscreteValues());
    }

    /**
     * Test of getRealMaxSize method, of class DefaultSampling.
     */
    @Test
    public void testGetRealMaxSize() {
        System.out.println("getRealMaxSize");
        DefaultSampling instance = new DefaultSampling();
        assertEquals(0, instance.getRealMaxSize(), 0.0);
        
        instance = new DefaultSampling(10, 2);
        assertEquals(10, instance.getRealMaxSize(), 0.0);
        
        instance = new DefaultSampling(10, 2);
        instance.updateMaxSize(env10);
        assertEquals(10, instance.getRealMaxSize(), 0.0);
        
        instance = new DefaultSampling(0, 2);
        instance.updateMaxSize(env10);
        assertEquals(5, instance.getRealMaxSize(), 0.0);
    }

    /**
     * Test of isDiscrete method, of class DefaultSampling.
     */
    @Test
    public void testIsDiscrete() {
        System.out.println("isDiscrete");
        DefaultSampling sampling = new DefaultSampling();
        assertEquals(false, sampling.isDiscrete());
        
        sampling = new DefaultSampling();
        sampling.updateSampling(img10, env10);
        assertEquals(true, sampling.isDiscrete());
    }

    /**
     * Test of getCoef method, of class DefaultSampling.
     */
    @Test
    public void testGetCoef() {
        System.out.println("getCoef");
        DefaultSampling instance = new DefaultSampling();
        assertEquals(2, instance.getCoef(), 0.0);
        
        instance = new DefaultSampling(0, 1);
        assertEquals(2, instance.getCoef(), 0.0);
        
        instance = new DefaultSampling(0, 0, 0);
        assertEquals(2, instance.getCoef(), 0.0);
        
        instance = new DefaultSampling(0, 0, 0, Sequence.ARITH);
        assertEquals(1, instance.getCoef(), 0.0);
    }
    
    /**
     * Test of getSeq method, of class DefaultSampling.
     */
    @Test
    public void testGetSeq() {
        System.out.println("getSeq");
        DefaultSampling instance = new DefaultSampling();
        assertEquals(Sequence.GEOM, instance.getSeq());
        
        instance = new DefaultSampling(0, 0);
        assertEquals(Sequence.GEOM, instance.getSeq());
        
        instance = new DefaultSampling(0, 0, 0);
        assertEquals(Sequence.GEOM, instance.getSeq());
        
        instance = new DefaultSampling(0, 0, 0, Sequence.ARITH);
        assertEquals(Sequence.ARITH, instance.getSeq());
    }

    /**
     * Test of getDefaultMin method, of class DefaultSampling.
     */
    @Test
    public void testGetDefaultMin() {
        DefaultSampling sampling = new DefaultSampling();
        assertEquals(1, sampling.getDefaultMin(covPoint10.getFeatures()), 0.0);
        assertEquals(10, sampling.getDefaultMin(covPoly10.getFeatures()), 0.1);
    }

    /**
     * Test of getDefaultMax method, of class DefaultSampling.
     */
    @Test
    public void testGetDefaultMax() {
        System.out.println("getDefaultMax");
        DefaultSampling instance = new DefaultSampling();
        assertEquals(5, instance.getDefaultMax(env10), 0.0);
    }

    /**
     * Test of getNext method, of class DefaultSampling.
     */
    @Test
    public void testGetNext() {
        System.out.println("getNext");
        DefaultSampling instance = new DefaultSampling();
        assertEquals(20, instance.getNext(10), 0.0);
        
        instance = new DefaultSampling(0, 1);
        assertEquals(20, instance.getNext(10), 0.0);
        
        instance = new DefaultSampling(0, 3);
        assertEquals(30, instance.getNext(10), 0.0);
        
        instance = new DefaultSampling(0, 0, 10, Sampling.Sequence.ARITH);
        assertEquals(20, instance.getNext(10), 0.0);
    }

    /**
     * Test of getDefaultEstimType method, of class DefaultSampling.
     */
    @Test
    public void testGetDefaultEstimType() {
        System.out.println("getDefaultEstimType");
        DefaultSampling instance = new DefaultSampling();
        assertEquals(EstimationFactory.Type.LOG, instance.getDefaultEstimType());

        instance = new DefaultSampling(0, 0, 0, Sampling.Sequence.ARITH);
        assertEquals(EstimationFactory.Type.DIRECT, instance.getDefaultEstimType());
    }
    
    /**
     * Test of getCeilingScaleIndex method, of class DefaultSampling.
     */
    @Test
    public void testGetCeilingDistIndex() {
        System.out.println("getCeilingDistIndex");
        DefaultSampling instance = new DefaultSampling(1, 8, 2);
        assertEquals(0, instance.getCeilingScaleIndex(0));
        assertEquals(0, instance.getCeilingScaleIndex(1));
        assertEquals(2, instance.getCeilingScaleIndex(4));
        assertEquals(3, instance.getCeilingScaleIndex(5));
        assertEquals(3, instance.getCeilingScaleIndex(8));

        instance = new DefaultSampling(4, 10, 2, Sequence.ARITH);
        assertEquals(0, instance.getCeilingScaleIndex(0));
        assertEquals(0, instance.getCeilingScaleIndex(4));
        assertEquals(1, instance.getCeilingScaleIndex(5));
        assertEquals(2, instance.getCeilingScaleIndex(8));
    }
    
    /**
     * Test of getCeilingScale method, of class DefaultSampling.
     */
    @Test
    public void testGetCeilingDistClass() {
        System.out.println("getCeilingDistClass");
        DefaultSampling instance = new DefaultSampling(1, 8, 2);
        assertEquals(1, instance.getCeilingScale(0), 0);
        assertEquals(1, instance.getCeilingScale(1), 0);
        assertEquals(4, instance.getCeilingScale(4), 0);
        assertEquals(8, instance.getCeilingScale(5), 0);
        assertEquals(8, instance.getCeilingScale(8), 0);

        instance = new DefaultSampling(4, 10, 2, Sequence.ARITH);
        assertEquals(4, instance.getCeilingScale(1), 0);
        assertEquals(4, instance.getCeilingScale(4), 0);
        assertEquals(6, instance.getCeilingScale(5), 0);
        assertEquals(8, instance.getCeilingScale(8), 0);
    }
}
