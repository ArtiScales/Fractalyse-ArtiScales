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
package org.thema.fracgis;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.geotools.geometry.jts.JTS;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.FeatureCoverage;
import org.thema.graph.SpatialGraph;
import org.thema.parallel.ParallelExecutor;

/**
 *
 * @author Gilles Vuidel
 */
public class Data {
    // raster data
    public static BufferedImage imgPoint, imgLine, imgSquare, imgFrac;
    public static Envelope env16, envFrac;

    // vector data
    public static FeatureCoverage covPoint, covLine, covSquare, covFrac;
    
    // network vector data
    public static SpatialGraph netCross64, netCross1064, netFrac;
    
    public static void loadNetVector() throws IOException {
        netCross64 = new SpatialGraph(Arrays.asList(
            new DefaultFeature(2, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(0, 64), new Coordinate(64, 64)})),
            new DefaultFeature(3, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(64, 0), new Coordinate(64, 64)})),
            new DefaultFeature(4, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(128, 64), new Coordinate(64, 64)})),
            new DefaultFeature(5, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(64, 128), new Coordinate(64, 64)}))));
        netCross1064 = new SpatialGraph(Arrays.asList(
            new DefaultFeature(1, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(0, 64), new Coordinate(10, 64)})),
            new DefaultFeature(2, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(10, 64), new Coordinate(64, 64)})),
            new DefaultFeature(3, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(64, 0), new Coordinate(64, 64)})),
            new DefaultFeature(4, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(128, 64), new Coordinate(64, 64)})),
            new DefaultFeature(5, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(64, 128), new Coordinate(64, 64)}))));
        netFrac = new SpatialGraph(DefaultFeature.loadFeatures(new File("target/test-classes/org/thema/fracgis/tapis_n5_cross_net.shp")),
            new GeometryPrecisionReducer(new PrecisionModel(100)));
    }
    
    public static void loadVector(double precision) throws IOException {
        covPoint = new DefaultFeatureCoverage(Arrays.asList(
            new DefaultFeature(1, new GeometryFactory().createPoint(new Coordinate(0, 0))),
            new DefaultFeature(2, new GeometryFactory().createPoint(new Coordinate(100, 100)))));
        covLine = new DefaultFeatureCoverage(Arrays.asList(
            new DefaultFeature(1, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(0, 64*(1-precision))})),
            new DefaultFeature(2, new GeometryFactory().createLineString(new Coordinate[] {new Coordinate(100, 0), new Coordinate(100, 64*(1-precision))}))));
        covSquare = new DefaultFeatureCoverage(Arrays.asList(
            new DefaultFeature(1, JTS.toGeometry(new Envelope(0, 64*(1-precision), 0, 64*(1-precision))))));
        covFrac = new DefaultFeatureCoverage(DefaultFeature.loadFeatures(new File("target/test-classes/org/thema/fracgis/tapis_n5_point.shp")));
    }
    
    public static void loadRaster() throws IOException {
        ParallelExecutor.setNbProc(4);
        ParallelFExecutor.setNbProc(4);
        
        imgPoint = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_BINARY);
        imgPoint.setRGB(7, 7, 0xffffff);
        imgLine = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_BINARY);
        for(int i = 0; i < 16; i++) {
            imgLine.setRGB(i, 7, 0xffffff);
        }
        imgSquare = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_BINARY);
        for(int i = 0; i < 16; i++) {
            for(int j = 0; j < 16; j++) {
                imgSquare.setRGB(i, j, 0xffffff);
            }
        }
        env16 = new Envelope(0, 16, 0, 16);

        imgFrac = ImageIO.read(new File("target/test-classes/org/thema/fracgis/frac1.465.tif"));
        WritableRaster r = imgFrac.getRaster();
        for(int i = 0; i < r.getHeight(); i++) {
            for(int j = 0; j < r.getWidth(); j++) {
                r.setSample(j, i, 0, 1 - r.getSample(j, i, 0));
            }
        }
        envFrac = new Envelope(0, imgFrac.getWidth(), 0, imgFrac.getHeight());
    }
}
