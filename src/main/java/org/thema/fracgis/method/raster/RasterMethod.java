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


package org.thema.fracgis.method.raster;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import java.awt.image.RenderedImage;
import org.thema.fracgis.method.AbstractMethod;
import org.thema.fracgis.sampling.DefaultSampling;

/**
 * Base class for implementing fractal dimension calculation with raster data.
 * Class used for uni and multi fractal.
 * @author Gilles Vuidel
 */
public abstract class RasterMethod extends AbstractMethod {
   
    private RenderedImage img;
    private Envelope envelope;

    /**
     * Initializes a new raster method.
     * @param inputName input layer name (must be a raster layer)
     * @param sampling scales sampling
     * @param img the raster image
     * @param envelope the image envelope in world coordinate, may be null
     */
    public RasterMethod(String inputName, DefaultSampling sampling, RenderedImage img, Envelope envelope) {
        super(inputName, sampling);
        this.img = img;
        this.envelope = envelope;
        sampling.updateSampling(img, envelope);
    }
        
    public final RenderedImage getImg() {
        return img;
    }

    @Override
    public Envelope getDataEnvelope() {
        return envelope;
    }
    
    public double getResolution() {
        if(envelope == null) {
            return 1;
        } else {
            return envelope.getWidth() / img.getWidth();
        }
    }
    
    protected AffineTransformation getTransform() {
        if(envelope == null) {
            return new AffineTransformation();
        }
        
        double sx = img.getWidth()/envelope.getWidth();
        double sy = img.getHeight()/envelope.getHeight();
        AffineTransformation t = AffineTransformation.translationInstance(-envelope.getMinX(), -envelope.getMaxY());
        t.scale(sx, -sy);
        return t;
    }
}
