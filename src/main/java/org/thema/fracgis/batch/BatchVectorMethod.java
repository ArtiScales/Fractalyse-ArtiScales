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


package org.thema.fracgis.batch;

import org.thema.common.ProgressBar;
import org.thema.common.swing.TaskMonitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import org.thema.common.parallel.*;
import org.thema.common.param.XMLObject;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.fracgis.estimation.Estimation;
import org.thema.fracgis.estimation.EstimationFactory;
import static org.thema.fracgis.method.AbstractMethod.paramsFromString;
import org.thema.fracgis.method.vector.mono.MonoVectorMethod;
import org.thema.msca.GridFeatureCoverage;
import org.thema.msca.SquareGrid;

/**
 * Calculates an unifractal dimension for each zone of a vector layer.
 * Zones can be set from a regular grid or from a polygonal layer.
 * An unifractal vector method is used for calculating the fractal dimension for each zone.
 * @author Gilles Vuidel
 */
public class BatchVectorMethod {
    
    private final FeatureLayer layer;
    private final MonoVectorMethod method;
    
    private double resolution;
    
    private FeatureLayer zoneLayer;
    private String idZone;
    
    private FeatureCoverage<Feature> coverage;
    private FeatureCoverage<? extends Feature> zoneCoverage;

    private List<Feature> results;
    
    /**
     * Creates a BatchVectorMethod with a regular grid partitionning.
     * @param layer the vector data
     * @param method the unfractal vector method for calculating fractal dimension
     * @param resolution the resolution of the grid (size of the cells)
     */
    public BatchVectorMethod(FeatureLayer layer, MonoVectorMethod method, double resolution) {
        this.layer = layer;
        this.method = method;
        this.resolution = resolution;
    }

    /**
     * Creates a BatchVectorMethod with a free partitionning from a vector layer (must be polygonal).
     * @param layer the vector data
     * @param method the unfractal vector method for calculating fractal dimension
     * @param zoneLayer the partitionning layer defining zones (must be polygonal)
     * @param idZone the id field in zoneLayer
     */
    public BatchVectorMethod(FeatureLayer layer, MonoVectorMethod method, FeatureLayer zoneLayer, String idZone) {
        this.layer = layer;
        this.method = method;
        this.zoneLayer = zoneLayer;
        this.idZone = idZone;
    }
    
    /**
     * Perform the calculation.
     * The execution is parallelized by thread. Does not work with MPI.
     * @param mon the progression monitor
     * @throws CancellationException if user cancel the task
     */
    public void execute(ProgressBar mon) {
        coverage = new DefaultFeatureCoverage(layer.getFeatures());
        if(isGrid()) {
            zoneCoverage = new GridFeatureCoverage(new SquareGrid(coverage.getEnvelope(), resolution));
        } else {
            zoneCoverage = new DefaultFeatureCoverage(zoneLayer.getFeatures());
        }
        results = Collections.synchronizedList(new ArrayList<Feature>());
        final List<String> attrNames = new ArrayList<>(Arrays.asList("Dim", "R2", "CI_min", "CI_max", "CI_delta"));
        attrNames.addAll(paramsFromString(method.getParamString()).keySet());
        
        SimpleParallelTask task = new SimpleParallelTask<Feature>((List)zoneCoverage.getFeatures(), mon) {
            @Override
            protected void executeOne(Feature zone) {
                Object zoneId = isGrid() ? zone.getId() : zone.getAttribute(idZone);
                List<Feature> featuresIn = coverage.getFeaturesIn(zone.getGeometry());
                if(featuresIn.isEmpty()) {
                    return;
                }
                List<Feature> featuresClip = new ArrayList<>();
                for(Feature f : featuresIn) {
                    featuresClip.add(new DefaultFeature(f.getId(), zone.getGeometry().intersection(f.getGeometry())));
                }

                MonoVectorMethod m = XMLObject.dupplicate(method);
                m.getSampling().updateMaxSize(zone.getGeometry().getEnvelopeInternal());
                m.setInputData(zoneId.toString(), new DefaultFeatureCoverage(featuresClip));
                m.execute(new TaskMonitor.EmptyMonitor(), false);
                Estimation estim = new EstimationFactory(m).getDefaultEstimation();
                double[] ci = estim.getBootStrapConfidenceInterval();
                ArrayList values = new ArrayList(Arrays.asList(estim.getDimension(), estim.getR2(), ci[0], ci[1], ci[1]-ci[0]));
                values.addAll(paramsFromString(m.getParamString()).values());
                values.addAll(paramsFromString(estim.getParamInfo()).values());
                synchronized(BatchVectorMethod.this) {
                    if(attrNames.size() < values.size()) {
                        attrNames.addAll(paramsFromString(estim.getParamInfo()).keySet());
                    }
                }
                results.add(new DefaultFeature(zoneId, zone.getGeometry(), attrNames, values));
            }
        };
        
        new ParallelFExecutor(task).executeAndWait();
        
        if(task.isCanceled()) {
            throw new CancellationException();
        }
    }

    /**
     * @return the zones with fractal dimension and parameters in the attributes of each Feature
     */
    public List<Feature> getResults() {
        return results;
    }
    
    private boolean isGrid() {
        return zoneLayer == null;
    }
    
}
