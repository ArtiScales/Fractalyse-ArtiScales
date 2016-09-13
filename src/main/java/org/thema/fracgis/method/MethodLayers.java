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


package org.thema.fracgis.method;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.DefaultLayer;
import org.thema.drawshape.style.LineStyle;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.estimation.EstimationFrame;
import org.thema.fracgis.estimation.ScaleRangeShape;
import org.thema.fracgis.estimation.MultiFracEstimationFrame;

/**
 * Layer for displaying the scale range and retrieving estimation window by contextual menu.
 * 
 * @author Gilles Vuidel
 */
public class MethodLayers extends DefaultGroupLayer {
        
    private JPopupMenu menu;
    private DefaultLayer scaleRangeLayer;
    private ScaleRangeShape scaleRangeShape;
    private Method method;

    /**
     * Creates a new GroupLayer for the method m
     * @param m the method associated with this GroupLayer
     */
    public MethodLayers(Method m) {
        super(m.getDetailName());
        setRemovable(true);
        this.method = m;
        menu = super.getContextMenu();
        menu.add(new AbstractAction("Estimation") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(method instanceof MonoMethod) {
                    new EstimationFrame(null, new EstimationFactory((MonoMethod)method)).setVisible(true);
                } else {
                    new MultiFracEstimationFrame(null, (MultiFracMethod)method).setVisible(true);
                }
            }
        });
        Envelope env = method.getDataEnvelope();
        scaleRangeShape = new ScaleRangeShape(new Point2D.Double(env.getMinX() + env.getWidth()/2, env.getMinY()-env.getHeight()*0.05), 0, 1);
        scaleRangeLayer = new DefaultLayer("Scale range", scaleRangeShape, new LineStyle(Color.orange.darker()));
        addLayerFirst(scaleRangeLayer);
    }
    
    @Override
    public JPopupMenu getContextMenu() {
        return menu;
    }

    /**
     * Sets the range displayed by the scale range shape
     * @param min min distance
     * @param max max distance
     */
    public void setRange(double min, double max) {
        scaleRangeShape.setRange(min, max);
    }

    /**
     * Sets the shape for displaying the scale range.
     * 
     * @param scaleRangeShape the new scale range shape
     */
    public void setScaleRangeShape(ScaleRangeShape scaleRangeShape) {
        this.scaleRangeShape = scaleRangeShape;
        scaleRangeLayer.setShapes(Arrays.asList(scaleRangeShape));
    }

}
