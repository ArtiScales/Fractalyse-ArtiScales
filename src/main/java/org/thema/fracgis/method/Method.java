/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import org.thema.common.parallel.ProgressBar;
import org.thema.common.param.XMLParams;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.DefaultLayer;
import org.thema.drawshape.layer.Layer;
import org.thema.drawshape.style.LineStyle;
import org.thema.drawshape.style.SimpleStyle;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.estimation.EstimationFrame;
import org.thema.fracgis.estimation.ScaleRangeShape;

/**
 *
 * @author gvuidel
 */
public abstract class Method  {

    @XMLParams.NoParam
    protected String inputName;
    @XMLParams.NoParam
    protected TreeMap<Double, Double> curve;
    
    // pas utile en version batch ou CLI qui utilise la sérialisation
    transient private MethodLayers glayers;

    public class MethodLayers extends DefaultGroupLayer {
        
        JPopupMenu menu;
        DefaultLayer scaleRangeLayer;
        ScaleRangeShape scaleRangeShape;
        
        public MethodLayers(String name) {
            super(name);
            setRemovable(true);
            menu = super.getContextMenu();
            menu.add(new AbstractAction("Estimation") {
                public void actionPerformed(ActionEvent e) {
                    new EstimationFrame(null, new EstimationFactory(Method.this)).setVisible(true);
                }
            });
            Envelope env = getDataEnvelope();
            scaleRangeShape = new ScaleRangeShape(new Point2D.Double(env.getMinX() + env.getWidth()/2, env.getMinY()-env.getHeight()*0.05), 0, 1);
            scaleRangeLayer = new DefaultLayer("Scale range", scaleRangeShape, new LineStyle(Color.orange.darker()));
            addLayer(scaleRangeLayer);
        }

        public void addLayer(Layer l) {
            addLayerFirst(l);
        }

        @Override
        public JPopupMenu getContextMenu() {
            return menu;
        }
        
        public void setRange(double min, double max) {
            scaleRangeShape.setRange(min, max);
        }

    }

    public Method(String inputName) {
        this.inputName = inputName;
        curve = new TreeMap<Double, Double>();
    }

    public String getDetailName() {
        return inputName + " - " + getName() + " - " + getParamsName();
    }

    public String getInputName() {
        return inputName;
    }
    
    public abstract Envelope getDataEnvelope();
    
    public abstract String getName();

    public abstract void execute(ProgressBar monitor, boolean threaded);
    
    public abstract int getDimSign();
    
    public abstract String getParamsName();

    public synchronized MethodLayers getGroupLayer() {
        if(glayers == null)
            try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    glayers = new MethodLayers(getDetailName());
                }
            });
            } catch (Exception ex) {
                Logger.getLogger(Method.class.getName()).log(Level.SEVERE, null, ex);
            }
        return glayers;
    }

    public TreeMap<Double, Double> getCurve() {
        return curve;
    }

    @Override
    public String toString() {
        return getName();
    }
    
    public LinkedHashMap<String, Double> getParams() {
        return paramsFromString(getParamsName());
    }
    
    public static LinkedHashMap<String, Double> paramsFromString(String str) {
        String[] tokens = str.split("_");
        Pattern p = Pattern.compile("([^0-9]+)([0-9\\.]+)");
        LinkedHashMap<String, Double> params = new LinkedHashMap<String, Double>();
        for(String token : tokens) {
            Matcher m = p.matcher(token);
            m.find();
            params.put(m.group(1), Double.parseDouble(m.group(2)));
        }
        return params;
    }
}
