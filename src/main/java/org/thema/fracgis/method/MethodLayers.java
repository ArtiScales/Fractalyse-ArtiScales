/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.thema.drawshape.layer.Layer;
import org.thema.drawshape.style.LineStyle;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.estimation.EstimationFrame;
import org.thema.fracgis.estimation.ScaleRangeShape;
import org.thema.fracgis.estimation.MultiFracEstimationFrame;

/**
 *
 * @author gvuidel
 */
public class MethodLayers extends DefaultGroupLayer {
        
    JPopupMenu menu;
    DefaultLayer scaleRangeLayer;
    ScaleRangeShape scaleRangeShape;
    Method method;

    public MethodLayers(Method m) {
        super(m.getDetailName());
        setRemovable(true);
        this.method = m;
        menu = super.getContextMenu();
        menu.add(new AbstractAction("Estimation") {
            public void actionPerformed(ActionEvent e) {
                if(method instanceof MonoMethod)
                    new EstimationFrame(null, new EstimationFactory((MonoMethod)method)).setVisible(true);
                else
                    new MultiFracEstimationFrame(null, (MultiFracMethod)method).setVisible(true);
            }
        });
        Envelope env = method.getDataEnvelope();
        scaleRangeShape = new ScaleRangeShape(new Point2D.Double(env.getMinX() + env.getWidth()/2, env.getMinY()-env.getHeight()*0.05), 0, 1);
        scaleRangeLayer = new DefaultLayer("Scale range", scaleRangeShape, new LineStyle(Color.orange.darker()));
        addLayer(scaleRangeLayer);
    }

    public final void addLayer(Layer l) {
        addLayerFirst(l);
    }

    @Override
    public JPopupMenu getContextMenu() {
        return menu;
    }

    public void setRange(double min, double max) {
        scaleRangeShape.setRange(min, max);
    }

    public void setScaleRangeShape(ScaleRangeShape scaleRangeShape) {
        this.scaleRangeShape = scaleRangeShape;
        scaleRangeLayer.setShapes(Arrays.asList(scaleRangeShape));
    }

}
