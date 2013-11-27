/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.method.vector;

import com.vividsolutions.jts.geom.*;
import java.awt.Color;
import java.util.*;
import org.thema.common.JTS;
import org.thema.common.distribute.AbstractDistributeTask;
import org.thema.common.distribute.ExecutorService;
import org.thema.common.parallel.ProgressBar;
import org.thema.common.param.XMLParams;
import org.thema.drawshape.feature.Feature;
import org.thema.drawshape.feature.FeatureCoverage;
import org.thema.drawshape.layer.GeometryLayer;
import org.thema.drawshape.style.SimpleStyle;
import org.thema.msca.Cell;
import org.thema.msca.MSCell;
import org.thema.msca.SquareGrid;



// Beaucoup plus rapide que la version précédente pour les petites résolutions (qui sont les plus lents)
// La méthode est un peu plus lente que la version précédente pour les résolutions importantes (peu de cellules)
// mais comme ça reste très rapide dans les 2 cas on peut utiliser cette version dans tous les cas.
// Le seul problème de la méthode c'est qu'elle peut demander beaucoup de mémoire alors que la précédente ne demande rien 
// de plus que le coverage

/**
 *
 * @author Gilles Vuidel
 */

/**
 * Attention cells ne fonctionne pas en distribuer
 
 * @author gvuidel
 */
class BoxCountingTask extends AbstractDistributeTask<Integer, Integer> {
    int total = 0;
    SquareGrid grid;
    FeatureCoverage<Feature> coverage;
    boolean keepCells;
    public List<Geometry> cells = Collections.synchronizedList(new ArrayList<Geometry>());

    public BoxCountingTask(SquareGrid grid, FeatureCoverage coverage, boolean keepCells, ProgressBar monitor) {
        super(monitor);
        this.grid = grid;
        this.coverage = coverage;
        this.keepCells = keepCells;
    }
    
    @Override
    public Integer execute(int start, int end) {
        int nb = 0;
        for(int y = start; y < end; y++) {
            for(int x = 0; x < grid.getWidth(); x++) {
                Polygon cellGeom = grid.getCellGeom(x, y);
                for(Feature f : coverage.getFeatures(cellGeom.getEnvelopeInternal()))
                    if(f.getGeometry().intersects(cellGeom)) {
                        nb++;
                        if(keepCells)
                            cells.add(cellGeom);
                        break;
                    }
            }
            incProgress(1);
        }

        return nb;
    }

    public int getSplitRange() {
        return grid.getHeight();
    }

    public Integer getResult() {
        return total;
    }

    public void gather(Integer results) {
        total += results;
    }
}

// Beaucoup plus rapide que la version précédente pour les petites résolutions (qui sont les plus lents)
// La méthode est un peu plus lente que la version précédente pour les résolutions importantes (peu de cellules)
// mais comme ça reste très rapide dans les 2 cas on peut utiliser cette version dans tous les cas.
// Le seul problème de la méthode c'est qu'elle peut demander beaucoup de mémoire alors que la précédente ne demande rien 
// de plus que le coverage

class BoxCountingTask2 extends AbstractDistributeTask<Integer, Collection<Integer>> {
    SquareGrid grid;
    FeatureCoverage<Feature> coverage;
    Set<Integer> allIds = new HashSet<Integer>();
    boolean keepCells;
    public List<Geometry> cells = Collections.synchronizedList(new ArrayList<Geometry>());
    
    public BoxCountingTask2(SquareGrid grid, FeatureCoverage coverage, boolean keepCells, ProgressBar monitor) {
        super(monitor);
        this.grid = grid;
        this.coverage = coverage;
        this.keepCells = keepCells;
    }
    
    @Override
    public Collection<Integer> execute(int start, int end) {
        Set<Integer> ids = new HashSet<Integer>();
        int i = 0;
        for(Feature f : coverage.getFeatures().subList(start, end)) {
//            boolean inter = false;
            List<MSCell> cells = grid.getCellIn(JTS.envToRect(f.getGeometry().getEnvelopeInternal()));
            for(Cell cell : cells)
                if(f.getGeometry().intersects(cell.getGeometry())) {
                    ids.add(cell.getId());
//                    inter = true;
                }
//            if(!inter)
//                throw new RuntimeException("Feature out of the grid !!!!");
            i++;
            if((i % 50) == 0)
                incProgress(50);
        }
        incProgress(i%50);
        
        return ids;
    }

    public int getSplitRange() {
        return coverage.getFeatures().size();
    }

    @Override
    public void finish() {
        if(keepCells)
            for(Integer id : allIds)
                cells.add(grid.getCellGeom(id));
    }
    
    public Integer getResult() {
        return allIds.size();
    }

    public void gather(Collection<Integer> results) {
        allIds.addAll(results);
    }
}

public class BoxCountingMethod extends VectorMethod {

    double minSize = 0;
    double maxSize = 0;
    double coef = 2;
    @XMLParams.Name("gliding")
    int d = 1;
    
    @XMLParams.NoParam
    TreeSet<Double> sizes;
    @XMLParams.NoParam
    boolean keepCells = false;

    /**
     * For parameter management only
     */
    public BoxCountingMethod() {
        super();
    }
    
    public BoxCountingMethod(String inputName, FeatureCoverage cover, double min, double max, double coef, int d) {
        super(inputName, cover);
        
        this.minSize = min;
        this.maxSize = max;
        this.coef = coef;
        this.d = d;
        
        updateParams();
    }
    
    protected final void updateParams() {
        if(d == 0) d = 1;
        if(minSize == 0) minSize = getDefaultMin(coverage);
        if(maxSize == 0) maxSize = getDefaultMax(coverage);
        
        if(minSize > maxSize)
            throw new IllegalArgumentException("Min is greater than max !");
        
        sizes = new TreeSet<Double>();
        double val = minSize;
        while(val <= maxSize) {
            sizes.add(val);
            val *= coef;
        }
    }

    public void execute(ProgressBar monitor, boolean threaded) {
        HashMap<Double, List<SquareGrid>> grids = new HashMap<Double, List<SquareGrid>>();
        curve = new TreeMap<Double, Double>();
        Envelope env = new Envelope(coverage.getEnvelope());
        env.init(env.getMinX()-sizes.last()*1.01, env.getMaxX(), env.getMinY()-sizes.last()*1.01, env.getMaxY());
        for(double size : sizes) {
            List<SquareGrid> gridSize = new ArrayList<SquareGrid>();
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

        monitor.setMaximum(sizes.size()*100);
        monitor.setProgress(0);
        int i = 0;
        for(double size : sizes) {
            int n = d == 1 ? 1 : (int)(d * Math.pow(1.2, i));
            double delta = size / n;
            monitor.setNote("Resolution : " + size);
            for(double dx = 0; dx < size; dx += delta)
                for(double dy = 0; dy < size; dy += delta) {
                    List<Geometry> cells = new ArrayList<Geometry>();
                    long sum = 0;
                    for(SquareGrid grid : grids.get(size)) {
                        grid = grid.createTranslatedGrid(dx, dy);
                        if(grid.getWidth()*grid.getHeight() < coverage.getFeatures().size()) {
//                            long t1 = System.currentTimeMillis();
                            BoxCountingTask task = new BoxCountingTask(grid, coverage,  keepCells,
                                    monitor.getSubProgress(100.0/(grids.get(size).size()*n*n)));
                            if(threaded)
                                ExecutorService.execute(task);
                            else
                                ExecutorService.executeSequential(task);
//                            long t2 = System.currentTimeMillis();
//                            System.out.println("" + size + " - " + (t2-t1));
                            sum += task.getResult();
                            if(keepCells)
                                cells.addAll(task.cells);
            
                        } else {
//                            long t1 = System.currentTimeMillis();
                            BoxCountingTask2 task = new BoxCountingTask2(grid, coverage, keepCells, 
                                    monitor.getSubProgress(100.0/(grids.get(size).size()*n*n)));
                            if(threaded)
                                ExecutorService.execute(task);
                            else
                                ExecutorService.executeSequential(task);
//                            long t2 = System.currentTimeMillis();
//                            System.out.println("" + size + " - " + (t2-t1));
                            sum += task.getResult();
                            if(keepCells)
                                cells.addAll(task.cells);
                        }

                    }
                    if(!curve.containsKey(size) || sum < curve.get(size)) {
                        curve.put(size, (double)sum);
                        if(keepCells) {
                            String name = String.format("%g", size);
                            if(getGroupLayer().getLayer(name) != null) // si il existe déjà on l'enlève
                                getGroupLayer().removeLayer(getGroupLayer().getLayer(name));
                            GeometryLayer l = new GeometryLayer(name, new GeometryFactory().buildGeometry(cells), new SimpleStyle(Color.BLACK));
                            l.setVisible(false);
                            getGroupLayer().addLayer(l);
                        }
                    }
                }
            i++;
        }

    }

    public boolean isKeepCells() {
        return keepCells;
    }

    public void setKeepCells(boolean keepCells) {
        this.keepCells = keepCells;
    }

    @Override
    public int getDimSign() {
        return -1;
    }

    public double getMax() {
        return maxSize;
    }

    public double getMin() {
        return minSize;
    }

    @Override
    public String getName() {
        return "Boxcounting";
    }
    
    @Override
    public String getParamsName() {
        return String.format(Locale.US, "coef%g_min%g_max%g_glid%d", coef, minSize, maxSize, d);
    }
    
    public static double getDefaultMin(FeatureCoverage<Feature> cov) {
        double minArea = Double.MAX_VALUE;
        for(Feature f : cov.getFeatures()) {
            double area = f.getGeometry().getArea();
            if(area < minArea)
                minArea = area;
        }
        return Math.sqrt(minArea+1);
    }
    
    public static double getDefaultMax(FeatureCoverage cov) {
        Envelope env = cov.getEnvelope();
        return Math.min(env.getWidth(), env.getHeight())/2;
    }
}
