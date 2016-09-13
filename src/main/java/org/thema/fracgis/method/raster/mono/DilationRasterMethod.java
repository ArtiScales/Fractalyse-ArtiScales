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
import java.util.SortedSet;
import java.util.TreeMap;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.ProgressBar;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.method.raster.RasterMethod;

/**
 * Computes unifractal dimension by successive dilation.
 * 
 * @author Gilles Vuidel
 */
public class DilationRasterMethod extends RasterMethod implements MonoMethod {

    private TreeMap<Double, Double> curve;

    /**
     * Creates a new dilation method for the given data
     * @param inputName input layer name (must be a binary raster layer)
     * @param sampling scale sampling
     * @param img raster input data
     * @param env envelope of the raster in world coordinate
     */
    public DilationRasterMethod(String inputName, DefaultSampling sampling, RenderedImage img, Envelope env) {
        super(inputName, sampling, img, env);
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        curve = new TreeMap<>();
        SortedSet<Integer> samples = getSampling().getDiscreteValues();

        int max = samples.last();
        RandomIter src = RandomIterFactory.create(getImg(), null);
        WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_USHORT, getImg().getWidth()+2*max, getImg().getHeight() + 2*max, 1, null);
        int nb = 0;
        for(int y = 0; y < getImg().getHeight(); y++) {
            for(int x = 0; x < getImg().getWidth(); x++) {
                int val = src.getSample(x, y, 0);
                raster.setSample(x+max, y+max, 0, val);
                if(val == 1) {
                    nb++;
                }
            }
        }
        monitor.setMaximum(max*raster.getHeight());
        double res = getResolution();            
        curve.put(res, (double)nb);
        
        for(int i = 1; i <= max; i++) {
            for(int y = 0; y < raster.getHeight(); y++) {
                for(int x = 0; x < raster.getWidth(); x++) {
                    if(raster.getSample(x, y, 0) == i) {
                        if(raster.getSample(x-1, y-1, 0) == 0) {
                            raster.setSample(x-1, y-1, 0, i+1);
                            nb++;
                        }
                        if(raster.getSample(x-1, y, 0) == 0) {
                            raster.setSample(x-1, y, 0, i+1);
                            nb++;
                        }
                        if(raster.getSample(x-1, y+1, 0) == 0) {
                            raster.setSample(x-1, y+1, 0, i+1);
                            nb++;
                        }
                        if(raster.getSample(x, y-1, 0) == 0) {
                            raster.setSample(x, y-1, 0, i+1);
                            nb++;
                        }
                        if(raster.getSample(x, y+1, 0) == 0) {
                            raster.setSample(x, y+1, 0, i+1);
                            nb++;
                        }
                        if(raster.getSample(x+1, y-1, 0) == 0) {
                            raster.setSample(x+1, y-1, 0, i+1);
                            nb++;
                        }
                        if(raster.getSample(x+1, y, 0) == 0) {
                            raster.setSample(x+1, y, 0, i+1);
                            nb++;
                        }
                        if(raster.getSample(x+1, y+1, 0) == 0) {
                            raster.setSample(x+1, y+1, 0, i+1);
                            nb++;
                        }
                    }
                }
                monitor.incProgress(1);
            }
 
            if(samples.contains(i)) {          
                curve.put(res*(2*i+1), nb / Math.pow(2*i+1, 2));
            }
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
        return "Dilation";
    }
     
}
