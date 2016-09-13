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


package org.thema.fracgis.method.raster.multi;

import org.thema.fracgis.method.QMonoMethod;
import org.thema.fracgis.method.MultiFracMethod;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.ProgressBar;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;
import org.thema.fracgis.sampling.RasterBoxSampling;

/**
 * Multifractal analysis with boxcounting and raster data.
 *
 * @author Gilles Vuidel
 */
public class MultiFracBoxCountingRasterMethod extends RasterMethod implements MultiFracMethod {
    
    private List<WritableRaster> rasters;
    private double total;
    
    private transient TreeMap<Double, TreeMap<Double, Double>> cacheCurves;
    private transient List<Integer> sizes;

    /**
     * Creates a new multifractal box counting method for the given data
     * @param inputName input layer name (must be a raster layer)
     * @param sampling scale sampling
     * @param img raster input data
     * @param env envelope of the raster in world coordinate
     */
    public MultiFracBoxCountingRasterMethod(String inputName, RasterBoxSampling sampling, RenderedImage img, Envelope env) {
        super(inputName, sampling, img, env);
        
        this.cacheCurves = new TreeMap<>();
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        monitor.setMaximum(getImg().getHeight());
        rasters = new ArrayList<>();
        sizes = new ArrayList<>(getSampling().getDiscreteValues());
        for(int size : sizes) {
            int w = (int)Math.ceil(getImg().getWidth() / (double)size);
            rasters.add(Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT,
                    w, (int)Math.ceil(getImg().getHeight() / (double)size), 1, w, new int[]{0}), null));
        }

        RandomIter iter = RandomIterFactory.create(getImg(), null);
        
        total = 0;
        for(int y = 0; y < getImg().getHeight(); y++) {
            for(int x = 0; x < getImg().getWidth(); x++) {
                final double val = iter.getSampleDouble(x, y, 0);
                if(val < 0) {
                    throw new RuntimeException("Negative value not permitted");
                }
                if(Double.isNaN(val)) {
                    continue;
                }     
                for(int i = 0; i < sizes.size(); i++) {
                    final double size = sizes.get(i);
                    final int xi = (int)(x / size);
                    final int yi = (int)(y / size);
                    rasters.get(i).setSample(xi, yi, 0, 
                            rasters.get(i).getSampleFloat(xi, yi, 0) + val);
                }
                total += val;
            }
            monitor.incProgress(1);
        }
    }
    
    @Override
    public MonoMethod getSimpleMethod(final double q) {
        return new QMonoMethod(this, q);
    }
    
    @Override
    public synchronized TreeMap<Double, Double> getCurve(double q) {
        if(!cacheCurves.containsKey(q)) {
            calcCurves(Collections.singleton(q));
        }
        return cacheCurves.get(q);
    }
    
    @Override
    public TreeMap<Double, TreeMap<Double, Double>> getCurves(TreeSet<Double> qs) {
        calcCurves(qs);
        TreeMap<Double, TreeMap<Double, Double>> curves = new TreeMap<>();
        for(Double q : qs) {
            curves.put(q, cacheCurves.get(q));
        }
        return curves;
    }
    
    private void calcCurves(Set<Double> qSet) {
        List<Double> qList = new ArrayList<>();
        for(Double q : qSet) {
            if(!cacheCurves.containsKey(q)) {
                qList.add(q);
                cacheCurves.put(q, new TreeMap<Double, Double>());
            }
        }
        for(int i = 0; i < rasters.size(); i++) {
            DataBuffer buf = rasters.get(i).getDataBuffer();
            double [] sum = new double[qList.size()];
            for(int j = 0; j < buf.getSize(); j++)  {
                float val = buf.getElemFloat(j);
                if(val > 0) {
                    for(int k = 0; k < sum.length; k++) {
                        sum[k] += Math.pow(val / total, qList.get(k));
                    }
                }
            }
            for(int k = 0; k < sum.length; k++) {
                cacheCurves.get(qList.get(k)).put(getResolution()*sizes.get(i), sum[k]);
            }
        }  
    }

    @Override
    public int getDimSign() {
        return -1;
    }
    
    @Override
    public String getName() {
        return "MultiFractal";
    }

}
