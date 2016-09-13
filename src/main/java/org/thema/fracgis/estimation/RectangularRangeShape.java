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


package org.thema.fracgis.estimation;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 * Shape displaying 2 squares (or circles) representing a distance range (min, max) of a radial analysis.
 * 
 * @author Gilles Vuidel
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
