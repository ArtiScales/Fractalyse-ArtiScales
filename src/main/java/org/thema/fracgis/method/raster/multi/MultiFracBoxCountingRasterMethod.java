/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import org.thema.common.ProgressBar;
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.raster.RasterMethod;

/**
 *
 * @author gvuidel
 */
public class MultiFracBoxCountingRasterMethod extends RasterMethod implements MultiFracMethod {
    
    private final double coef;
    private final double maxSize;
    
    private List<WritableRaster> rasters;
    private double total;
    
    private transient TreeMap<Double, TreeMap<Double, Double>> cacheCurves;

    public MultiFracBoxCountingRasterMethod(String inputName, RenderedImage img, Envelope env, double coef, double maxSize) {
        super(inputName, img, env);
        this.coef = coef;
        
        if(maxSize <= 0)
            maxSize = getDefaultMaxSize(img, getResolution());
        this.maxSize = maxSize;
        
        this.cacheCurves = new TreeMap<>();
    }

    @Override
    public void execute(ProgressBar monitor, boolean threaded) {
        monitor.setMaximum(img.getHeight());
        int n = (int) Math.ceil(Math.log(maxSize/getResolution()) / Math.log(coef)) + 1;
        rasters = new ArrayList<>();
        List<Double> sizes = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            sizes.add(Math.pow(coef, i));
            int w = (int)Math.ceil(img.getWidth() / Math.pow(coef, i));
            rasters.add(Raster.createWritableRaster(new ComponentSampleModel(DataBuffer.TYPE_FLOAT,
                    w, (int)Math.ceil(img.getHeight() / Math.pow(coef, i)), 1, w, new int[]{0}), null));
        }

        RandomIter iter = RandomIterFactory.create(img, null);
//        double min = Double.MAX_VALUE;
//        for(int y = 0; y < img.getHeight(); y++) {
//            for(int x = 0; x < img.getWidth(); x++) {
//                final double val = iter.getSampleDouble(x, y, 0);
//                if(val < min)
//                    min = val;
//            }
//        }
        
        total = 0;
        for(int y = 0; y < img.getHeight(); y++) {
            for(int x = 0; x < img.getWidth(); x++) {
                final double val = iter.getSampleDouble(x, y, 0);
                if(val < 0)
                    throw new RuntimeException("Negative value not permitted");
//                final double v = val-min;
                if(Double.isNaN(val))// || val < 0.01) 
                    continue;
                
                for(int i = 0; i < n; i++) {
                    final double size = sizes.get(i);
                    final int xi = (int)(x / size);
                    final int yi = (int)(y / size);
                    rasters.get(i).setSample(xi, yi, 0, rasters.get(i).getSampleFloat(xi, yi, 0)
                        + val);
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
        for(Double q : qSet)
            if(!cacheCurves.containsKey(q)) {
                qList.add(q);
                cacheCurves.put(q, new TreeMap<Double, Double>());
            }
        for(int i = 0; i < rasters.size(); i++) {
            DataBuffer buf = rasters.get(i).getDataBuffer();
            double [] sum = new double[qList.size()];
            for(int j = 0; j < buf.getSize(); j++)  {
                float val = buf.getElemFloat(j);
                if(val > 0) {
                    for(int k = 0; k < sum.length; k++)
                        sum[k] += Math.pow(val / total, qList.get(k));
                }
            }
            for(int k = 0; k < sum.length; k++)
                cacheCurves.get(qList.get(k)).put(getResolution()*Math.pow(coef, i), sum[k]);
        }  
    }

    @Override
    public int getDimSign() {
        return -1;
    }
    
    @Override
    public String getParamsName() {
        return String.format(Locale.US, "coef%g_min%g_max%g", coef, getMinSize(), getMaxSize());
    }

    public double getMinSize() {
        return getResolution();
    }
    
    public double getMaxSize() {
        return getResolution()*Math.pow(coef, rasters.size()-1);
    }
    
    @Override
    public String getName() {
        return "MultiFractal";
    }
    
    public static double getDefaultMaxSize(RenderedImage img, double resolution) {
        return resolution * Math.max(img.getWidth(), img.getHeight());
        //return resolution * Math.min(img.getWidth(), img.getHeight())/2;
    }

}
