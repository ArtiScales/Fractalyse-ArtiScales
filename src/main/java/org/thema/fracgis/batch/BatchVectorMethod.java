/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thema.fracgis.batch;

import org.thema.common.ProgressBar;
import org.thema.common.swing.TaskMonitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thema.common.parallel.*;
import org.thema.common.param.XMLParams;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.fracgis.estimation.Estimation;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.method.AbstractMethod;
import org.thema.fracgis.method.vector.mono.SimpleVectorMethod;
import org.thema.fracgis.method.vector.VectorMethod;
import org.thema.msca.GridFeatureCoverage;
import org.thema.msca.SquareGrid;

/**
 *
 * @author gvuidel
 */
public class BatchVectorMethod {
    
    private final FeatureLayer layer;
    private final SimpleVectorMethod method;
    
    private double resolution;
    
    private FeatureLayer zoneLayer;
    private String idZone;
    
    private FeatureCoverage<Feature> coverage;
    private FeatureCoverage<? extends Feature> zoneCoverage;

    private List<Feature> results;
    
    public BatchVectorMethod(FeatureLayer layer, SimpleVectorMethod method, double resolution) {
        this.layer = layer;
        this.method = method;
        this.resolution = resolution;
    }

    public BatchVectorMethod(FeatureLayer layer, SimpleVectorMethod method, FeatureLayer zoneLayer, String idZone) {
        this.layer = layer;
        this.method = method;
        this.zoneLayer = zoneLayer;
        this.idZone = idZone;
    }
    
    public void execute(ProgressBar mon) {
        coverage = new DefaultFeatureCoverage(layer.getFeatures());
        if(isGrid()) {
            zoneCoverage = new GridFeatureCoverage(new SquareGrid(coverage.getEnvelope(), resolution));
        } else
            zoneCoverage = new DefaultFeatureCoverage(zoneLayer.getFeatures());
        
        results = Collections.synchronizedList(new ArrayList<Feature>());
        final List<String> attrNames = new ArrayList<String>(Arrays.asList("Dim", "R2", "CI_min", "CI_max", "CI_delta"));
        attrNames.addAll(method.getParams().keySet());
        
        SimpleParallelTask task = new SimpleParallelTask<Feature>((List)zoneCoverage.getFeatures(), mon) {
            @Override
            protected void executeOne(Feature zone) {
                Object zoneId = isGrid() ? zone.getId() : zone.getAttribute(idZone);
                List<Feature> featuresIn = coverage.getFeaturesIn(zone.getGeometry());
                if(featuresIn.isEmpty())
                    return;
                List<Feature> featuresClip = new ArrayList<Feature>();
                for(Feature f : featuresIn)
                    featuresClip.add(new DefaultFeature(f.getId(), zone.getGeometry().intersection(f.getGeometry())));
                try {
                    SimpleVectorMethod m = XMLParams.dupplicate(method);
                    m.setInputData(zoneId.toString(), new DefaultFeatureCoverage(featuresClip));
                    m.execute(new TaskMonitor.EmptyMonitor(), false);
                    Estimation estim = new EstimationFactory(m).getDefaultEstimation();
                    double[] ci = estim.getBootStrapConfidenceInterval();
                    ArrayList values = new ArrayList(Arrays.asList(estim.getDimension(), estim.getR2(), ci[0], ci[1], ci[1]-ci[0]));
                    values.addAll(m.getParams().values());
                    values.addAll(AbstractMethod.paramsFromString(estim.getParamInfo()).values());
                    synchronized(BatchVectorMethod.this) {
                        if(attrNames.size() < values.size())
                            attrNames.addAll(AbstractMethod.paramsFromString(estim.getParamInfo()).keySet());
                    }
                    results.add(new DefaultFeature(zoneId, zone.getGeometry(), attrNames, values));
                } catch (Exception ex) {
                    Logger.getLogger(BatchVectorMethod.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException(ex);
                }
            }
        };
        
        new ParallelFExecutor(task).executeAndWait();
        
        if(task.isCanceled())
            throw new CancellationException();
    }

    public List<Feature> getResults() {
        return results;
    }
    
    private boolean isGrid() {
        return zoneLayer == null;
    }
    
}
