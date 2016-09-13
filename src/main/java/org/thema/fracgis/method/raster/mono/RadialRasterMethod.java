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


package org.thema.fracgis.method.raster.mono;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeMap;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.ProgressBar;
import org.thema.fracgis.estimation.RectangularRangeShape;
import org.thema.fracgis.method.MethodLayers;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;
import org.thema.fracgis.sampling.RadialSampling;

/**
 * Radial analysis on raster data.
 * 
 * @author Gilles Vuidel
 */
public class RadialRasterMethod extends RasterMethod implements MonoMethod {
    
    private Coordinate centre = null;
    
    private TreeMap<Double, Double> curve;
    
    /**
     * Constructor for data in pixel unit (no world envelope supplied)
     * @param inputName the input layer name (must be a binary raster layer)
     * @param img the input raster data
     * @param centre the starting point in pixel unit
     */
    public RadialRasterMethod(String inputName, RadialSampling scaling, RenderedImage img, Coordinate centre) {
        super(inputName, scaling, img, new Envelope(0, img.getWidth(), 0, img.getHeight()));
        this.centre = centre;
    }
    
    /**
     * Constructor for data with spatial unit (world envelope)
     * @param inputName the input layer name (must be a binary raster layer)
     * @param img the input raster data
     * @param centre the starting point in world coordinate
     * @param envelope envelope of the raster data in world coordinate
     */
    public RadialRasterMethod(String inputName, RadialSampling scaling, RenderedImage img, Envelope envelope, Coordinate centre) {
        super(inputName, scaling, img, envelope);
        this.centre = centre;
    }
    
    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        Coordinate c = getTransform().transform(centre, new Coordinate());
        SortedSet<Integer> scales = getSampling().getDiscreteValues();
        int x = (int) c.x;
        int y = (int) c.y;
        int n = scales.last();
        int[] count = new int[n+1];
        
        final int i1 = y-n < 0 ? 0 : y-n;
        final int i2 = y+n >= getImg().getHeight() ? getImg().getHeight()-1 : y+n;
        final int j1 = x-n < 0 ? 0 : x-n;
        final int j2 = x+n >= getImg().getWidth() ? getImg().getWidth()-1 : x+n;
        monitor.setMaximum(i2-i1+1);
        RandomIter r = RandomIterFactory.create(getImg(), null);
        for(int i = i1; i <= i2; i++) {
            for(int j = j1; j <= j2; j++) {
                if(r.getSample(j, i, 0) == 1) {
                    count[Math.max(Math.abs(i-y), Math.abs(j-x))]++;
                }
            }
            monitor.incProgress(1);
        }
        r.done();
        for(int i = 1; i < count.length; i++) {
            count[i] += count[i-1];
        }
        double res = getResolution();
        curve = new TreeMap<>();
        for(int i = 0; i < count.length; i++) {
            if(scales.contains(i)) {
                curve.put((2*i+1) * res, count[i] * res*res);
            }
        }
    }

    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
    
    @Override
    public int getDimSign() {
        return 1;
    }
    
    @Override
    public String getName() {
        return "Radial";
    }

    @Override
    public String getParamString() {
        return String.format(Locale.US, "cx%g_cy%g_", centre!=null?centre.x:0.0, centre!=null?centre.y:0.0) + super.getParamString();
    }

    @Override
    public MethodLayers getGroupLayer() {
        MethodLayers groupLayer = super.getGroupLayer(); 
        groupLayer.setScaleRangeShape(new RectangularRangeShape(new Point2D.Double(centre.x, centre.y), 0, getSampling().getRealMaxSize()));
        return groupLayer;
    }
}
