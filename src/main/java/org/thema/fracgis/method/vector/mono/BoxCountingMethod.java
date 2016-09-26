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


package org.thema.fracgis.method.vector.mono;

import com.vividsolutions.jts.geom.*;
import java.awt.Color;
import java.util.*;
import org.thema.common.JTS;
import org.thema.parallel.AbstractParallelTask;
import org.thema.parallel.ExecutorService;
import org.thema.common.ProgressBar;
import org.thema.common.param.ReflectObject;
import org.thema.data.feature.Feature;
import org.thema.data.feature.FeatureCoverage;
import org.thema.drawshape.layer.GeometryLayer;
import org.thema.drawshape.style.SimpleStyle;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.msca.Cell;
import org.thema.msca.MSCell;
import org.thema.msca.SquareGrid;


/**
 *
 * @author Gilles Vuidel
 */

/**
 * Parallel task for computing box counting on vector data for only one scale.
 * Computes the number of boxes containing data.
 * 
 * This task can be used in threaded mode or MPI.
 * if keepBoxes option is activated, MPI cannot be used.
 * 
 */
class BoxCountingTask extends AbstractParallelTask<Integer, Integer> {
    private int total = 0;
    private SquareGrid grid;
    private FeatureCoverage<Feature> coverage;
    private boolean keepBoxes;
    private List<Geometry> boxes = Collections.synchronizedList(new ArrayList<Geometry>());

    /**
     * Creates a new task.
     * @param grid grid containing cells (ie. box)
     * @param coverage the vector data
     * @param keepBoxes keep boxes for displaying ?
     * @param monitor progression monitor
     */
    BoxCountingTask(SquareGrid grid, FeatureCoverage coverage, boolean keepBoxes, ProgressBar monitor) {
        super(monitor);
        this.grid = grid;
        this.coverage = coverage;
        this.keepBoxes = keepBoxes;
    }
    
    @Override
    public Integer execute(int start, int end) {
        int nb = 0;
        for(int y = start; y < end; y++) {
            for(int x = 0; x < grid.getWidth(); x++) {
                Polygon cellGeom = grid.getCellGeom(x, y);
                for(Feature f : coverage.getFeatures(cellGeom.getEnvelopeInternal())) {
                    if(f.getGeometry().intersects(cellGeom)) {
                        nb++;
                        if(keepBoxes) {
                            boxes.add(cellGeom);
                        }
                        break;
                    }
                }
            }
            incProgress(1);
        }

        return nb;
    }

    @Override
    public int getSplitRange() {
        return grid.getHeight();
    }

    @Override
    public Integer getResult() {
        return total;
    }

    @Override
    public void gather(Integer results) {
        total += results;
    }

    public List<Geometry> getBoxes() {
        return boxes;
    }
    
}

/**
 * Parallel task for computing box counting on vector data for only one scale.
 * Computes the number of boxes containing data.
 * 
 * This task can be used in threaded mode or MPI.
 * if keepBoxes option is activated, MPI cannot be used.
 * 
 * Faster than previous algorithm in most cases but uses more memory
 */
class BoxCountingTask2 extends AbstractParallelTask<Integer, Collection<Integer>> {
// Beaucoup plus rapide que la version précédente pour les petites résolutions (qui sont les plus lentes)
// La méthode est un peu plus lente que la version précédente pour les résolutions importantes (peu de cellules)
// mais comme ça reste très rapide dans les 2 cas on peut utiliser cette version dans tous les cas.
// Le seul problème de la méthode c'est qu'elle peut demander beaucoup de mémoire alors que la précédente ne demande rien 
// de plus que le coverage
    private SquareGrid grid;
    private FeatureCoverage<Feature> coverage;
    private Set<Integer> allIds = new HashSet<>();
    private boolean keepBoxes;
    private List<Geometry> boxes;
    
    /**
     * Creates a new task.
     * @param grid grid containing cells (ie. box)
     * @param coverage the vector data
     * @param keepBoxes keep boxes for displaying ?
     * @param monitor progression monitor
     */
    BoxCountingTask2(SquareGrid grid, FeatureCoverage coverage, boolean keepBoxes, ProgressBar monitor) {
        super(monitor);
        this.grid = grid;
        this.coverage = coverage;
        this.keepBoxes = keepBoxes;
        if(keepBoxes) {
            boxes = Collections.synchronizedList(new ArrayList<Geometry>());
        }
    }
    
    @Override
    public Collection<Integer> execute(int start, int end) {
        Set<Integer> ids = new HashSet<>();
        int i = 0;
        for(Feature f : coverage.getFeatures().subList(start, end)) {
            List<MSCell> cellsIn = grid.getCellIn(JTS.envToRect(f.getGeometry().getEnvelopeInternal()));
            for(Cell cell : cellsIn) {
                if(f.getGeometry().intersects(cell.getGeometry())) {
                    ids.add(cell.getId());
                }
            }

            i++;
            if((i % 50) == 0) {
                incProgress(50);
            }
        }
        incProgress(i%50);
        
        return ids;
    }

    @Override
    public int getSplitRange() {
        return coverage.getFeatures().size();
    }

    @Override
    public void finish() {
        if(keepBoxes) {
            for(Integer id : allIds) {
                boxes.add(grid.getCellGeom(id));
            }
        }
    }
    
    @Override
    public Integer getResult() {
        return allIds.size();
    }

    @Override
    public void gather(Collection<Integer> results) {
        allIds.addAll(results);
    }
    
    public List<Geometry> getBoxes() {
        return boxes;
    }
}

/**
 * Box counting method for vector data.
 * Box counting algorithm calculates the Minkowski–Bouligand dimension.
 * It can be optimized by gliding grid.
 * 
 * @author Gilles Vuidel
 */
public class BoxCountingMethod extends MonoVectorMethod {

    @ReflectObject.Name("gliding")
    private int d = 1;
    
    @ReflectObject.NoParam
    private boolean keepBoxes = false;

    /**
     * For parameter management only
     */
    public BoxCountingMethod() {
    }
    
    /**
     * For batch mode
     * @param d gliding grid optimization if d > 1
     */
    public BoxCountingMethod(int d) {
        if(d < 1) {
            d = 1;
        }
        this.d = d;
    }
    
    /**
     * Creates a new box counting method for vector data.
     * @param inputName the input data layer name 
     * @param sampling the scale sampling
     * @param cover the input vector data
     * @param d gliding grid optimization if d > 1
     * @param keepBoxes keep black boxes for displaying it ?
     */
    public BoxCountingMethod(String inputName, DefaultSampling sampling, FeatureCoverage cover, int d, boolean keepBoxes) {
        super(inputName, sampling, cover);
        if(d < 1) {
            d = 1;
        }
        this.d = d;
        this.keepBoxes = keepBoxes;
    }

    @Override
    public void execute(ProgressBar monitor, boolean parallel) {
        HashMap<Double, List<SquareGrid>> grids = new HashMap<>();
        SortedSet<Double> sizes = getSampling().getValues();
        curve = new TreeMap<>();
        
        Envelope env = new Envelope(getDataEnvelope());
        env.init(env.getMinX()-sizes.last()*1.001, env.getMaxX(), env.getMinY()-sizes.last()*1.001, env.getMaxY());
        for(double size : sizes) {
            List<SquareGrid> gridSize = new ArrayList<>();
            int nx = (int)Math.ceil(Math.ceil(env.getWidth() / (double)size) / 40000.0);
            int ny = (int)Math.ceil(Math.ceil(env.getHeight() / (double)size) / 40000.0);
            int w = (int)Math.ceil((env.getWidth() / (double)nx) / size);
            int h = (int)Math.ceil((env.getHeight() / (double)ny) / size);
            Coordinate start = new Coordinate(env.getMinX(), env.getMinY());
            for(int x = 0; x < nx; x++) {
                start.y = env.getMinY();
                SquareGrid grid = null;
                for(int y = 0; y < ny; y++) {
                    grid = new SquareGrid(start, size, w, h);
                    start.y = grid.getEnvelope().getMaxY();
                    gridSize.add(grid);
                }
                start.x = grid.getEnvelope().getMaxX();
            }
            grids.put(size, gridSize);
        }
        FeatureCoverage<Feature> coverage = getCoverage();
        monitor.setMaximum(sizes.size()*100);
        monitor.setProgress(0);
        int i = 0;
        for(double size : sizes) {
            int n = d == 1 ? 1 : (int)(d * Math.pow(Math.pow(getSampling().getCoef(), 0.3), i));
            double delta = size / n;
            monitor.setNote("Resolution : " + size);
            // if d > 1 : move the grid in the 2d space
            for(double dx = 0; dx < size; dx += delta) {
                for(double dy = 0; dy < size; dy += delta) {
                    List<Geometry> cells = new ArrayList<>();
                    long sum = 0;
                    for(SquareGrid grid : grids.get(size)) {
                        grid = grid.createTranslatedGrid(dx, dy);
                        // if there are more vector elements than cells grid -> use algo 1 else use algo 2
                        if(grid.getWidth()*grid.getHeight() < coverage.getFeatures().size()) {
//                            long t1 = System.currentTimeMillis();
                            BoxCountingTask task = new BoxCountingTask(grid, coverage,  keepBoxes,
                                    monitor.getSubProgress(100.0/(grids.get(size).size()*n*n)));
                            if(parallel) {
                                ExecutorService.execute(task);
                            } else {
                                ExecutorService.executeSequential(task);
                            }
//                            long t2 = System.currentTimeMillis();
//                            System.out.println("" + size + " - " + (t2-t1));
                            sum += task.getResult();
                            if(keepBoxes) {
                                cells.addAll(task.getBoxes());
                            }
            
                        } else {
//                            long t1 = System.currentTimeMillis();
                            BoxCountingTask2 task = new BoxCountingTask2(grid, coverage, keepBoxes, 
                                    monitor.getSubProgress(100.0/(grids.get(size).size()*n*n)));
                            if(parallel) {
                                ExecutorService.execute(task);
                            } else {
                                ExecutorService.executeSequential(task);
                            }
//                            long t2 = System.currentTimeMillis();
//                            System.out.println("" + size + " - " + (t2-t1));
                            sum += task.getResult();
                            if(keepBoxes) {
                                cells.addAll(task.getBoxes());
                            }
                        }

                    }
                    // add the result to the curve if it does not exist or update if is better (ie. smaller)
                    if(!curve.containsKey(size) || sum < curve.get(size)) {
                        curve.put(size, (double)sum);
                        if(keepBoxes) {
                            String name = String.format("%g", size);
                            if(getGroupLayer().getLayer(name) != null) { // si il existe déjà on l'enlève
                                getGroupLayer().removeLayer(getGroupLayer().getLayer(name));
                            }
                            GeometryLayer l = new GeometryLayer(name, new GeometryFactory().buildGeometry(cells), new SimpleStyle(Color.BLACK));
                            l.setVisible(false);
                            getGroupLayer().addLayerFirst(l);
                        }
                    }
                }
            }
            i++;
        }

    }

    @Override
    public int getDimSign() {
        return -1;
    }

    @Override
    public String getName() {
        return "Boxcounting";
    }
    
    @Override
    public String getParamString() {
        return super.getParamString() + String.format(Locale.US, "_glid%d", d);
    }
}
