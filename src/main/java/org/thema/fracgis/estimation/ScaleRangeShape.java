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
import org.thema.drawshape.AbstractDrawableJavaShape;

/**
 * Shape displaying on a map a distance range by a line.
 * 
 * @author Gilles Vuidel
 */
public class ScaleRangeShape extends AbstractDrawableJavaShape {

    private final Point2D centre;
    private double rangeMin, rangeMax;

    public ScaleRangeShape(Point2D centre, double rangeMin, double rangeMax) {
        this.centre = centre;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
    }
    
    @Override
    public Shape getJavaShape(AffineTransform t) {
        Point2D minLeft = t.transform(new Point2D.Double(centre.getX() - rangeMin/2, centre.getY()), null);
        Point2D maxLeft = t.transform(new Point2D.Double(centre.getX() - rangeMax/2, centre.getY()), null);
        Point2D minRight = t.transform(new Point2D.Double(centre.getX() + rangeMin/2, centre.getY()), null);
        Point2D maxRight = t.transform(new Point2D.Double(centre.getX() + rangeMax/2, centre.getY()), null);
        double ty = minLeft.getY();
        
        Path2D p = new Path2D.Double();
        // 2 lignes horizontales
        p.moveTo(minLeft.getX(), ty);
        p.lineTo(maxLeft.getX(), ty);
        p.moveTo(minRight.getX(), ty);
        p.lineTo(maxRight.getX(), ty);
        // 4 lignes verticales
        p.moveTo(minLeft.getX(), ty-3);
        p.lineTo(minLeft.getX(), ty+3);
        p.moveTo(maxLeft.getX(), ty-5);
        p.lineTo(maxLeft.getX(), ty+5);
        p.moveTo(minRight.getX(), ty-3);
        p.lineTo(minRight.getX(), ty+3);
        p.moveTo(maxRight.getX(), ty-5);
        p.lineTo(maxRight.getX(), ty+5);
        return p;
    }

    public void setRange(double min, double max) {
        this.rangeMin = min;
        this.rangeMax = max;
        fireShapeChanged();
    }
    
    public double getRangeMax() {
        return rangeMax;
    }

    public void setRangeMax(double rangeMax) {
        this.rangeMax = rangeMax;
        fireShapeChanged();
    }

    public double getRangeMin() {
        return rangeMin;
    }

    public void setRangeMin(double rangeMin) {
        this.rangeMin = rangeMin;
        fireShapeChanged();
    }

    public Point2D getCentre() {
        return centre;
    }
    
}
