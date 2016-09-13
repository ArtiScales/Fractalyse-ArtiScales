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

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.ProgressBar;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;
import org.thema.fracgis.sampling.RasterBoxSampling;

/**
 * Box counting method for raster data.
 * Box counting algorithm calculates the Minkowski–Bouligand dimension.
 * @author Gilles Vuidel
 */
public class BoxCountingRasterMethod extends RasterMethod implements MonoMethod {
    
    private TreeMap<Double, Double> curve;

    /**
     * Creates a new box counting method for the given data
     * @param inputName input layer name (must be a binary raster layer)
     * @param sampling scale sampling
     * @param img raster input data
     * @param env envelope of the raster in world coordinate
     */
    public BoxCountingRasterMethod(String inputName, RasterBoxSampling sampling, RenderedImage img, Envelope env) {
        super(inputName, sampling, img, env);
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        monitor.setMaximum(getImg().getHeight());
        List<Integer> sizes = new ArrayList<>(getSampling().getDiscreteValues());
        if(sizes.get(0) == 1) {
            sizes.remove(0);
        }
        List<WritableRaster> rasters = new ArrayList<>();
        for(int size : sizes) {
            rasters.add(Raster.createBandedRaster(DataBuffer.TYPE_BYTE,
                    (int)Math.ceil(getImg().getWidth() / (double)size),
                    (int)Math.ceil(getImg().getHeight() / (double)size), 1, null));
        }

        int nb = 0;
        RandomIter iter = RandomIterFactory.create(getImg(), null);
        
        for(int y = 0; y < getImg().getHeight(); y++) {
            for(int x = 0; x < getImg().getWidth(); x++) {
                if(iter.getSample(x, y, 0) == 1) {
                    for(int i = 0; i < sizes.size(); i++) {
                        rasters.get(i).setSample(x / sizes.get(i),
                                y / sizes.get(i), 0, 1);
                    }
                    nb++;
                }
            }
            monitor.setProgress(y+1);
        }

        curve = new TreeMap<>();
        if(getSampling().getDiscreteValues().first() == 1) {
            curve.put(getResolution(), (double)nb);
        }
        for(int i = 0; i < sizes.size(); i++) {
            DataBuffer buf = rasters.get(i).getDataBuffer();
            nb = 0;
            for(int j = 0; j < buf.getSize(); j++) {
                if(buf.getElem(j) == 1) {
                    nb++;
                }
            }
            curve.put(getResolution()*sizes.get(i), (double)nb);

        }
    }

    @Override
    public TreeMap<Double, Double> getCurve() {
        return curve;
    }
    
    @Override
    public int getDimSign() {
        return -1;
    }
    
    @Override
    public String getName() {
        return "Boxcounting";
    }
}
