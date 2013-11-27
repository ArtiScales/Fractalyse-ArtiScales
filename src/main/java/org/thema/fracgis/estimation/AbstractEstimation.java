/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.estimation;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.fracgis.method.Method;

/**
 *
 * @author gvuidel
 */
public abstract class AbstractEstimation implements Estimation {

    protected Method method;
    protected TreeMap<Double, Double> curve;
    protected Range range;

    public AbstractEstimation(Method method) {
        this.method = method;
        curve = method.getCurve();
        range = new Range(curve.firstKey(), curve.lastKey());
    }

    public XYPlot getPlot() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(getEstimationSerie());
        XYSeries serie = new XYSeries("Empirical");
        for(Double d : curve.keySet())
            serie.add(d, curve.get(d));
        dataset.addSeries(serie);

        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesShapesFilled(1, false);
        renderer.setSeriesShapesVisible(0, false);
        XYPlot plot = new XYPlot(dataset, new NumberAxis("X"), new NumberAxis("Y"),
                       renderer);
        return plot;
        
    }

    public XYSeries getScalingBehaviour() {
        XYSeries serie = new XYSeries("Scaling behaviour");
        Iterator<Double> it = curve.keySet().iterator();
        double precX = it.next();
        while(it.hasNext()) {
            double x = it.next();
            serie.add((x+precX)/2, (Math.log(curve.get(x)) - Math.log(curve.get(precX)))
                    / (Math.log(x) - Math.log(precX)));
            precX = x;
        }
        
        return serie;
    }

    public void setRange(double xmin, double xmax) {
        if(curve.floorKey(xmin) == null)
            xmin = curve.firstKey();
        if(curve.ceilingKey(xmax) == null)
            xmax = curve.lastKey();
        range = new Range(curve.floorKey(xmin), curve.ceilingKey(xmax));
        estimate();
    }

    public Range getRange() {
        return range;
    }

    public TreeMap<Double, Double> getCurve() {
        return curve;
    }

    public Method getMethod() {
        return method;
    }

    protected abstract XYSeries getEstimationSerie();

    protected abstract void estimate();

    protected NavigableMap<Double, Double> getRangeCurve() {
        return curve.subMap(range.getLowerBound(), true, range.getUpperBound(), true);
    }

    public void saveToText(Writer w) throws IOException {
        w.write("Input : " + method.getInputName() + "\n");
        w.write("Method : " + method.getName() + "\n");
        w.write("Parameters : " + method.getParamsName() + "\n\n");
        
        w.write(getResultInfo());
        w.write("\n\nX\tY\tScaling behaviour\tEstim\n");
        XYSeries scalingBehaviour = getScalingBehaviour();
        int i = 0;
        for(Double x : curve.keySet()) {
            double sc = Double.NaN;
            if(i < scalingBehaviour.getItemCount())
                sc = ((Number)scalingBehaviour.getY(i++)).doubleValue();
            if(getRangeCurve().containsKey(x)) 
                w.write(String.format("%g\t%g\t%g\t%g\n", x, curve.get(x), sc, getEstimValue(x)));
            else
                w.write(String.format("%g\t%g\t%g\t\n", x, curve.get(x), sc));
        }

    }


}
