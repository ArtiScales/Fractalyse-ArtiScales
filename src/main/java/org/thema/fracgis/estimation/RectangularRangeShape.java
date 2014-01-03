/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.estimation;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 *
 * @author gvuidel
 */
public class RectangularRangeShape extends ScaleRangeShape {

    private RectangularShape shape;
    
    public RectangularRangeShape(Point2D centre, double rangeMin, double rangeMax) {
        super(centre, rangeMin, rangeMax);
        shape = new Rectangle2D.Double();
    }

    public void setShape(RectangularShape shape) {
        this.shape = shape;
    }

    @Override
    public Shape getJavaShape(AffineTransform t) {
        Path2D.Double p = new Path2D.Double();
        if(getRangeMin() > 0) {
            shape.setFrame(getCentre().getX() - getRangeMin()/2, getCentre().getY() - getRangeMin()/2, getRangeMin(), getRangeMin());
            p.append(shape, false);
        }
        shape.setFrame(getCentre().getX() - getRangeMax()/2, getCentre().getY() - getRangeMax()/2, getRangeMax(), getRangeMax());
        p.append(shape, false);
        return t.createTransformedShape(p);
    }
    
    
}
