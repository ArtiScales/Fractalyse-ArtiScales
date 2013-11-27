/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.method.raster;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import java.awt.image.RenderedImage;
import org.thema.fracgis.method.Method;

/**
 *
 * @author gvuidel
 */
public abstract class RasterMethod extends Method {
   
    protected RenderedImage img;
    protected Envelope envelope;

    public RasterMethod(String inputName, RenderedImage img, Envelope envelope) {
        super(inputName);
        this.img = img;
        this.envelope = envelope;
    }

    public RenderedImage getImg() {
        return img;
    }

    public Envelope getDataEnvelope() {
        return envelope;
    }
    
    public double getResolution() {
        if(envelope == null)
            return 1;
        else
            return envelope.getWidth() / img.getWidth();
    }
    
        
    protected AffineTransformation getTransform() {
        if(envelope == null)
            return new AffineTransformation();
        
        double sx = img.getWidth()/envelope.getWidth();
        double sy = img.getHeight()/envelope.getHeight();
        AffineTransformation t = AffineTransformation.translationInstance(-envelope.getMinX(), -envelope.getMaxY());
        t.scale(sx, -sy);
        return t;
    }
}
