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


package org.thema.fracgis.sampling;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import org.geotools.coverage.grid.GridCoverage2D;
import org.thema.common.JTS;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.estimation.EstimationFactory.Type;
import org.thema.fracgis.sampling.Sampling.Sequence;

/**
 * Default implementation for sampling.
 * The sampling is defined by a minimum and maximum size, a type of sequence, a coefficient or increment
 * 
 * @author Gilles Vuidel
 */
public class DefaultSampling implements Sampling {
    
    private double minSize;
    private double maxSize;
    private double coef;
    private Sequence seq;
    
    @ReflectObject.NoParam
    private boolean discrete;
    @ReflectObject.NoParam
    private double resolution;
    

    /**
     * Creates a default sampling.
     * min and max sizes are initializes to 0, coef to 2 and the sequence to GEOM.
     * They will be updated by {@link #updateSampling() }
     */
    public DefaultSampling() {
        minSize = 0;
        maxSize = 0;
        coef = 2;
        discrete = false;
        seq = Sequence.GEOM;
    }

    /**
     * Copy constructor.
     * @param s the sampling to copy
     */
    public DefaultSampling(DefaultSampling s) {
        minSize = s.minSize;
        maxSize = s.maxSize;
        coef = s.coef;
        discrete = s.discrete;
        seq = s.seq;
        resolution = s.resolution;
    }

    /**
     * Creates a new geometric sampling.
     * The min size will be updated by {@link #updateSampling() }
     * @param maxSize the maximum size (scale) or 0
     * @param coef coefficient factor, must be > 1
     */
    public DefaultSampling(double maxSize, double coef) {
        this(0, maxSize, coef, Sequence.GEOM);
    }
    
    /**
     * Creates a new geometric sampling.
     * @param minSize the minimum size (scale) or 0
     * @param maxSize the maximum size (scale) or 0
     * @param coef coefficient factor, must be > 1
     */
    public DefaultSampling(double minSize, double maxSize, double coef) {
        this(minSize, maxSize, coef, Sequence.GEOM);
    }
    
    /**
     * 
     * @param minSize the minimum size (scale) or 0
     * @param maxSize the maximum size (scale) or 0
     * @param coef coefficient factor or increment (for arithmetic sequence)
     * @param seq type of sequence arithmetic or geometric
     */
    public DefaultSampling(double minSize, double maxSize, double coef, Sequence seq) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.seq = seq;
        if(seq == Sequence.ARITH && coef <= 0) {
            coef = 1;
        }
        if(seq == Sequence.GEOM && coef <= 1) {
            coef = 2;
        }
        this.coef = coef;
    }
    
    /**
     * If maxsize == 0, sets the default max size depending on the envelope of the data.
     * If maxsize > 0, does nothing
     * @param env the envelope of the layer data
     */
    public void updateMaxSize(Envelope env) {
        if(maxSize <= 0) {
            maxSize = getDefaultMax(env);
        }
        if(maxSize <= minSize) {
            maxSize = getNext(minSize);
        }
    }
    
    /**
     * Update the min size and the max size (if maxsize == 0) based on the given raster data.
     * @param img the raster image
     * @param env the envelope of the raster in world coordinate or null
     */
    public void updateSampling(RenderedImage img, Envelope env) {
        if(env == null) {
            updateSampling(1, new Envelope(0, img.getWidth(), 0, img.getHeight()));
        } else {
            updateSampling(env.getWidth() / img.getWidth(), env);
        }
    }
    
    /**
     * Update the min size and the max size (if maxsize == 0) based on the given raster coverage.
     * @param grid the raster coverage
     */
    public void updateSampling(GridCoverage2D grid) {
        updateSampling(grid.getEnvelope2D().getWidth() / grid.getGridGeometry().getGridRange2D().getWidth(),
                JTS.rectToEnv(grid.getEnvelope2D()));
    }
    
    private void updateSampling(double resolution, Envelope env) {
        discrete = true;

        this.resolution = resolution;
        if(minSize <= resolution) {
            minSize = resolution;
        }
        
        updateMaxSize(env);
    }
    
    /**
     * Update the min size (if minsize == 0) and the max size (if maxsize == 0) based on the given vector coverage.
     * @param cov the vector coverage
     */
    public void updateSampling(FeatureCoverage cov) {
        discrete = false;
        
        if(minSize <= 0) {
            minSize = getDefaultMin(cov.getFeatures());
            if(maxSize > 0 && minSize >= maxSize) {
                minSize = getPrevious(maxSize);
            }
        }
        
        updateMaxSize(cov.getEnvelope());
    }
    
    @Override
    public SortedSet<Double> getValues() {
        TreeSet<Double> values = new TreeSet<>();
        double val = minSize;
        while(val <= maxSize) {
            if(discrete) {
                int i = (int) Math.round((val/resolution - 1) / 2);
                values.add((2*i+1) * resolution);
            } else {
                values.add(val);
            }
            val = getNext(val);
        }
        return values;
    }
    
    /**
     * @return the set of scales in pixel size
     */
    public SortedSet<Integer> getDiscreteValues() {
        if(!discrete) {
            throw new IllegalArgumentException();
        }
        TreeSet<Integer> values = new TreeSet<>();
        double val = minSize;
        while(val <= maxSize) {
            int i = (int) Math.round((val/resolution - 1) / 2);
            values.add(i);
            val = getNext(val);
        }
        return values;
    }
    
    /**
     * Returns the real max size (scale) if all parameters have been set (minsize, maxsize, coef and sequence) 
     * by a constructor or by calling one of {@link #updateSampling } method.
     * @return the real max size (scale) ie. the last element of {@link #getValues() }
     */
    public double getRealMaxSize() {
        if(minSize <= 0) {
            return maxSize;
        }
        
        return getValues().last();
    }
    
    /**
     * @return the minimum size (scale) or zero if it is not set
     */
    public double getMinSize() {
        return minSize;
    }

    /**
     * @return the maximum size (scale) or zero if it is not set
     */
    public double getMaxSize() {
        return maxSize;
    }

    /**
     * @return the factor coefficient (for geometric sequence) or the increment (for arithmetic sequence)
     */
    public double getCoef() {
        return coef;
    }

    /**
     * It's false by default until one of raster {@link #updateSampling } method is called.
     * @return is a discrete sampling ? ie. associated with raster data
     */
    public boolean isDiscrete() {
        return discrete;
    }

    @Override
    public Sequence getSeq() {
        return seq;
    }

    /**
     * It's undefied until one of raster {@link #updateSampling } method is called.
     * @return the resolution (pixel size) of the associated raster data
     */
    protected double getResolution() {
        return resolution;
    }
    
    /**
     * @return a String representing the parameters (coef, min and max) of this sampling
     */
    public String getParamString() {
        return String.format(Locale.US, "coef%g_min%g_max%g", coef, getMinSize(), getRealMaxSize());
    }
    
    /**
     * Estimates the default min size for the given vector data.
     * This method is useful only for vector data. For raster data, the min size is by default the resolution of the raster.
     * @param cov the vector data
     * @return an estimation of the min size for this data
     */
    public double getDefaultMin(Collection<? extends Feature> cov) {
        double minArea = Double.MAX_VALUE;
        for(Feature f : cov) {
            double area = f.getGeometry().getArea();
            if(area < minArea) {
                minArea = area;
            }
        }
        return Math.sqrt(minArea+1);
    }
    
    /**
     * Estimates the default max size for the given envelope.
     * @param env the envelope in world coorindate of the data
     * @return an estimation of the min size for this data
     */
    public double getDefaultMax(Envelope env) {
        double size = Math.min(env.getWidth(), env.getHeight())/2;
        if(size == 0) {
            size = 1;
        }
        return size;
    }

    /**
     * @param val the current value of the sequence
     * @return the next value of the sequence
     */
    public double getNext(double val) {
        if(seq == Sequence.ARITH) {
            return val + coef;
        } else {
            return val * coef;
        }
    }

    private double getPrevious(double val) {
        if(seq == Sequence.ARITH) {
            return val - coef;
        } else {
            return val / coef;
        }
    }

    /**
     * Returns the index of the greatest sampling scale lower or equal to dist.
     * Returns always zero for distances lower than minSize.
     * The index can be greater than the number of scales. To avoid outofbounds exception, you must check the result
     * @param dist a distance
     * @return the index of the greatest sampling scale lower or equal to dist
     */
    public int getCeilingScaleIndex(double dist) {
        int ind;
        if(getSeq() == Sequence.GEOM) {
            ind = (int)Math.ceil(Math.log(dist/getMinSize()) / Math.log(getCoef()));
        } else {
            ind = (int)Math.ceil((dist-getMinSize()) / getCoef());
        }
        if(ind < 0) {
            ind = 0;
        }
        return ind;
    }
    
    /**
     * Returns the greatest sampling scale lower or equal to dist.
     * Returns always minSize for distances lower than minSize.
     * The returned scale can be greater than maxSize. To avoid outofbounds exception, you must check the result
     * @param dist a distance
     * @return the greatest sampling scale lower or equal to dist
     */
    public double getCeilingScale(double dist) {
        if(getSeq() == Sequence.GEOM) {
            return Math.pow(getCoef(), getCeilingScaleIndex(dist)) * getMinSize();
        } else {
            return getCoef() * getCeilingScaleIndex(dist) + getMinSize();
        }
    }
    
    @Override
    public String toString() {
        return "{" + "min=" + minSize + ", max=" + maxSize + ", coef=" + coef + ", seq=" + seq + '}';
    }

    @Override
    public EstimationFactory.Type getDefaultEstimType() {
        return seq == Sequence.ARITH ? Type.DIRECT : Type.LOG;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultSampling other = (DefaultSampling) obj;
        if (Double.doubleToLongBits(this.minSize) != Double.doubleToLongBits(other.minSize)) {
            return false;
        }
        if (Double.doubleToLongBits(this.maxSize) != Double.doubleToLongBits(other.maxSize)) {
            return false;
        }
        if (Double.doubleToLongBits(this.coef) != Double.doubleToLongBits(other.coef)) {
            return false;
        }
        return this.seq == other.seq;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.minSize) ^ (Double.doubleToLongBits(this.minSize) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.maxSize) ^ (Double.doubleToLongBits(this.maxSize) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.coef) ^ (Double.doubleToLongBits(this.coef) >>> 32));
        hash = 67 * hash + Objects.hashCode(this.seq);
        return hash;
    }
    
}
