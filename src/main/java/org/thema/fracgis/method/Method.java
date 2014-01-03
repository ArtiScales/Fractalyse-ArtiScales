/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method;

import com.vividsolutions.jts.geom.Envelope;
import java.util.LinkedHashMap;
import org.thema.common.parallel.ProgressBar;

/**
 *
 * @author gvuidel
 */
public interface Method {
    
    public String getDetailName();

    public String getInputName();
    
    public Envelope getDataEnvelope();
    
    public String getName();

    public void execute(ProgressBar monitor, boolean threaded);
    
    public int getDimSign();
    
    public String getParamsName();
    
    public LinkedHashMap<String, Double> getParams();
    
    public MethodLayers getGroupLayer();
}
