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

import org.thema.fracgis.sampling.DefaultSampling;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.thema.common.param.ReflectObject;

/**
 * Base class for implementing fractal dimension calculation.
 * Class used for uni and multi fractal.
 * @author Gilles Vuidel
 */
public abstract class AbstractMethod implements Method {

    private DefaultSampling sampling;
    
    @ReflectObject.NoParam
    protected String inputName;
    
    // pas utile en version batch ou CLI qui utilise la sérialisation
    transient private MethodLayers glayers;

    /**
     * Default constructor 
     */
    public AbstractMethod() {
        inputName = "(none)";
        sampling = new DefaultSampling();
    }
    
    /**
     * Initializes a new method 
     * @param inputName the input layer name
     * @param sampling the default sampling
     */
    public AbstractMethod(String inputName, DefaultSampling sampling) {
        this.inputName = inputName;
        this.sampling = sampling;
    }

    @Override
    public DefaultSampling getSampling() {
        return sampling;
    }

    @Override
    public void setSampling(DefaultSampling sampling) {
        this.sampling = sampling;
    }

    @Override
    public String getDetailName() {
        return inputName + " - " + getName() + " - " + getParamString();
    }

    @Override
    public String getInputLayerName() {
        return inputName;
    }

    @Override
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
            } else {
                try {
                    EventQueue.invokeAndWait(run);
                } catch (InterruptedException | InvocationTargetException ex) {
                    Logger.getLogger(AbstractMethod.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return glayers;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getParamString() {
        return sampling.getParamString();
    }
    
    /**
     * Extracts parameters from a string created by {@link #getParamString() }
     * @param str parameters string
     * @return map of parameter's name and value
     */
    public static LinkedHashMap<String, Double> paramsFromString(String str) {
        String[] tokens = str.split("_");
        Pattern p = Pattern.compile("([^0-9]+)([0-9\\.]+|NaN)");
        LinkedHashMap<String, Double> params = new LinkedHashMap<>();
        for(String token : tokens) {
            Matcher m = p.matcher(token);
            m.find();
            params.put(m.group(1), Double.parseDouble(m.group(2)));
        }
        return params;
    }
}
