/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.thema.common.param.XMLParams;

/**
 *
 * @author gvuidel
 */
public abstract class AbstractMethod implements Method {

    @XMLParams.NoParam
    protected String inputName;
    
    // pas utile en version batch ou CLI qui utilise la sérialisation
    transient private MethodLayers glayers;


    public AbstractMethod(String inputName) {
        this.inputName = inputName;
    }

    @Override
    public String getDetailName() {
        return inputName + " - " + getName() + " - " + getParamsName();
    }

    @Override
    public String getInputName() {
        return inputName;
    }

    public synchronized MethodLayers getGroupLayer() {
        if(glayers == null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    glayers = new MethodLayers(AbstractMethod.this);
                }
            };
            if(EventQueue.isDispatchThread()) {
                run.run();
            } else
                try {
                    EventQueue.invokeAndWait(run);
                } catch (InterruptedException | InvocationTargetException ex) {
                    Logger.getLogger(AbstractMethod.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        return glayers;
    }

    @Override
    public String toString() {
        return getName();
    }
    
    @Override
    public LinkedHashMap<String, Double> getParams() {
        return paramsFromString(getParamsName());
    }
    
    public static LinkedHashMap<String, Double> paramsFromString(String str) {
        String[] tokens = str.split("_");
        Pattern p = Pattern.compile("([^0-9]+)([0-9\\.]+)");
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        for(String token : tokens) {
            Matcher m = p.matcher(token);
            m.find();
            params.put(m.group(1), Double.parseDouble(m.group(2)));
        }
        return params;
    }
}
