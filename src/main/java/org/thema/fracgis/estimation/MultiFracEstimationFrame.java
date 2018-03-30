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


package org.thema.fracgis.estimation;

import org.thema.fracgis.method.MultiFracMethod;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.SpinnerListModel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.functors.StringValueTransformer;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.common.Util;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.fracgis.method.raster.multi.MultiFracWaveletMethod;
import org.thema.fracgis.method.raster.multi.WTMMMethod;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Frame for multifractal dimension estimation.
 * 
 * @author Gilles Vuidel
 */
public class MultiFracEstimationFrame extends javax.swing.JFrame implements ChartMouseListener {

    private MultiFracMethod method;
    
    private TreeMap<Double, TreeMap<Double, Double>> M;
    private TreeMap<Double, LogEstimation> estims;
    private TreeMap<Double, Double> Tq;
    private TreeMap<Double, Double> Dq;
    private TreeMap<Double, Double> alpha;
    private TreeMap<Double, Double> f;
    
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private XYPlot regPlot;
    
    /** 
     * Creates new form MultiFracEstimationFrame
     * @param frm the parent frame
     * @param method the multifractal method
     */
    public MultiFracEstimationFrame(JFrame frm, MultiFracMethod method) {
        initComponents();
        setLocationRelativeTo(frm);
        this.method = method;
        
        setTitle(method.getDetailName());
        
        TreeSet<Double> qSet = getqSet();
        M = method.getCurves(qSet);
        
        NavigableSet<Double> x = M.firstEntry().getValue().navigableKeySet();
        SpinnerListModel model = new SpinnerListModel(x.toArray());
        model.setValue(x.first());
        leftSpinner.setModel(model);
        model = new SpinnerListModel(x.toArray());
        model.setValue(x.last());
        rightSpinner.setModel(model);
        qComboBox.setModel(new DefaultComboBoxModel(qSet.toArray()));
        updateEstim();
    }
    
    private void updateEstim() {
        TreeSet<Double> qSet = getqSet();
        estims = new TreeMap<>();
        Tq = new TreeMap<>();
        Dq = new TreeMap<>();
        alpha = new TreeMap<>();
        f = new TreeMap<>();
        for(Double q : qSet) {
            LogEstimation estim = new LogEstimation(method.getSimpleMethod(q));
            estim.setRange((Double)leftSpinner.getValue(), (Double)rightSpinner.getValue());
            estims.put(q, estim);
            Tq.put(q, (isWTMMMethod() ? -1 : 1) * estim.getDimension() + (isLWTMethod() ? 2 : 0));
            if(Math.abs(q-1) > 0.00001) {
                if(isWTMMMethod()) {
                    Dq.put(q, (Tq.get(q)+2*q) / (q-1));
                } else {
                    Dq.put(q, Tq.get(q) / (1-q));
                }
            } else if(q == 1) {
                Dq.put(q, -Tq.get(q));
                Tq.put(q, 0.0);
            }
            if(qSet.first() != q) {
                double q1 = qSet.lower(q);
                alpha.put(q, -(Tq.get(q)-Tq.get(q1))/(q-q1));
                f.put(q, Tq.get(q) + q*alpha.get(q));
            }
        }
        
        updatePlot();
        if(!isLWTMethod()) {
            double d0 = Double.NaN;
            if(qSet.contains(0.0)) {
                d0 = Dq.get(0.0);
            }
            infoTextArea.setText(String.format("Dmin : %g\nD0 : %g\nDmax : %g", Collections.min(Dq.values()), d0, Collections.max(Dq.values())));
        }
        // TODO pas bien 
//        ((AbstractMethod)method).getGroupLayer().setRange((Double)leftSpinner.getValue(), (Double)rightSpinner.getValue());
    }

    private boolean isLWTMethod() {
        return method instanceof MultiFracWaveletMethod;
    }
    
    private boolean isWTMMMethod() {
        return method instanceof WTMMMethod;
    }
    
    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        if(!rightToggleButton.isSelected() && !leftToggleButton.isSelected()) {
            return;
        }

        regPlot.clearDomainMarkers();
        
        Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
        double x = regPlot.getDomainAxis().java2DToValue(p.getX(),
                chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea(),
                regPlot.getDomainAxisEdge());
        if(rightToggleButton.isSelected()) {
            rightSpinner.setValue(M.firstEntry().getValue().lowerKey(x));
            rightToggleButton.setSelected(false);
        } else {
            leftSpinner.setValue(M.firstEntry().getValue().higherKey(x));
            leftToggleButton.setSelected(false);
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        if(!rightToggleButton.isSelected() && !leftToggleButton.isSelected()) {
            return;
        }
        Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
        double x = regPlot.getDomainAxis().java2DToValue(p.getX(),
                chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea(),
                regPlot.getDomainAxisEdge());
        regPlot.clearDomainMarkers();
        regPlot.addDomainMarker(new ValueMarker(x, Color.BLACK, new BasicStroke(1)));
    }

    private TreeSet<Double> getqSet() {
        TreeSet<Double> qSet = new TreeSet<>();
        double q = (Double)qMinSpinner.getValue();
        while(q <= (Double)qMaxSpinner.getValue()) {
            qSet.add(q);
            q += (Double)qStepSpinner.getValue();
        }
        return qSet;
    }
    
    private void updatePlot() {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        XYSeriesCollection dataset = new XYSeriesCollection();
        switch(curveComboBox.getSelectedItem().toString()) {
            case "Mq":
                for(Double q : M.keySet()) {
                    XYSeries serie = new XYSeries("q"+q);
                    for(Double x : M.get(q).keySet()) {
                        if(M.get(q).get(x) > 0) {
                            serie.add(x, M.get(q).get(x));
                        }
                    }
                    dataset.addSeries(serie);
                }
                regPlot = new XYPlot(dataset, new LogarithmicAxis("x"), new LogarithmicAxis("y"), renderer);
                break;
            case "Tq":
                XYSeries serie = new XYSeries("Tq");
                for(Double q : Tq.keySet()) {
                    serie.add(q, Tq.get(q));
                }
                dataset.addSeries(serie);
                regPlot = new XYPlot(dataset, new NumberAxis("q"), new NumberAxis("t"), renderer);
                break;
            case "Dq":
                serie = new XYSeries("Dq");
                for(Double q : Dq.keySet()) {
                    serie.add(q, Dq.get(q));
                }
                dataset.addSeries(serie);
                regPlot = new XYPlot(dataset, new NumberAxis("q"), new NumberAxis("D"), renderer);
                break;
            default:
                serie = new XYSeries("f(alpha)");
                for(Double q : alpha.keySet()) {
                    serie.add(alpha.get(q), f.get(q));
                }
                dataset.addSeries(serie);
                regPlot = new XYPlot(dataset, new NumberAxis("alpha"), new NumberAxis("f(alpha)"), renderer);
        } 
      
        ((NumberAxis)regPlot.getDomainAxis()).setAutoRangeIncludesZero(false);
        ((NumberAxis)regPlot.getRangeAxis()).setAutoRangeIncludesZero(false);
        ((XYLineAndShapeRenderer)regPlot.getRenderer()).setDefaultShapesVisible(showPointCheckBox.isSelected());
        chart = new JFreeChart("", null, regPlot, false);

        chartPanel = new ChartPanel(chart);
        
        chartPanel.addChartMouseListener(this);
        splitPane.setRightComponent(chartPanel);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        infoTextArea = new javax.swing.JTextArea();
        exportButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        leftToggleButton = new javax.swing.JToggleButton();
        leftSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        rightToggleButton = new javax.swing.JToggleButton();
        jLabel6 = new javax.swing.JLabel();
        rightSpinner = new javax.swing.JSpinner();
        showPointCheckBox = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        qMinSpinner = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        qMaxSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        qStepSpinner = new javax.swing.JSpinner();
        qUpdateButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        curveComboBox = new javax.swing.JComboBox();
        qComboBox = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        viewqEstimButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Estimation");

        splitPane.setDividerLocation(300);

        infoTextArea.setEditable(false);
        infoTextArea.setColumns(10);
        infoTextArea.setRows(5);
        jScrollPane1.setViewportView(infoTextArea);

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Bounds"));

        leftToggleButton.setText("Select");
        leftToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftToggleButtonActionPerformed(evt);
            }
        });

        leftSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));
        leftSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                leftSpinnerStateChanged(evt);
            }
        });

        jLabel7.setText("Right");

        rightToggleButton.setText("Select");
        rightToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightToggleButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Left");

        rightSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1.0d)));
        rightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rightSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rightSpinner)
                    .addComponent(leftSpinner))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(leftToggleButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightToggleButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(leftSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(leftToggleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(rightSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightToggleButton))
                .addContainerGap())
        );

        showPointCheckBox.setText("Show points");
        showPointCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPointCheckBoxActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("q"));

        jLabel1.setText("Min");

        qMinSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(-5.0d), null, null, Double.valueOf(1.0d)));

        jLabel2.setText("Max");

        qMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(5.0d), null, null, Double.valueOf(1.0d)));

        jLabel3.setText("Step");

        qStepSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.5d), Double.valueOf(0.0d), null, Double.valueOf(0.1d)));

        qUpdateButton.setText("Update");
        qUpdateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qUpdateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(qMinSpinner)
                    .addComponent(qStepSpinner))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(qMaxSpinner))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(qUpdateButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(qMinSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(qMaxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(qStepSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(qUpdateButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText("Curves");

        curveComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mq", "Tq", "Dq", "f(alpha)" }));
        curveComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                curveComboBoxActionPerformed(evt);
            }
        });

        jLabel8.setText("q");

        viewqEstimButton.setText("View estim");
        viewqEstimButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewqEstimButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(exportButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(showPointCheckBox))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(curveComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(qComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(viewqEstimButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(curveComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(qComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(viewqEstimButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(showPointCheckBox)))
        );

        splitPane.setLeftComponent(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 893, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        File file = Util.getFileSave(".txt|.svg");
        if(file == null) {
            return;
        } 
        
        if(file.getName().endsWith(".txt")) { //TXT
            try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
                w.write("M\n");
                w.write(IterableUtils.toString(M.firstEntry().getValue().keySet(), StringValueTransformer.stringValueTransformer(), 
                        "\t", "q\\size\t", "\n"));
                for(double q : M.keySet()) {
                    w.write(IterableUtils.toString(M.get(q).values(), StringValueTransformer.stringValueTransformer(), 
                            "\t", q+"\t", "\n"));
                }
                
                w.write("\nRegressions");
                for(double q : estims.keySet()) {
                    w.write("\nq=" + q + "\n");
                    LogEstimation estim = estims.get(q);
                    estim.saveToText(w);
                }
                
                w.write("\nTq\n");
                writeCurve(Tq, w, "q", "Tau");
                
                w.write("Dq\n");
                writeCurve(Dq, w, "q", "D");
                
                w.write("Alpha(q)\n");
                writeCurve(alpha, w, "q", "alpha");
                
                w.write("f(q)\n");
                writeCurve(f, w, "q", "f");
                
            } catch (IOException ex) {
                Logger.getLogger(EstimationFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument(null, "svg", null);

            // Create an instance of the SVG Generator
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

            // draw the chart in the SVG generator
            chart.draw(svgGenerator, new Rectangle2D.Float(0, 0, 600, 400));

            // Write svg file
            try (OutputStream outputStream = new FileOutputStream(file)) {
                Writer out = new OutputStreamWriter(outputStream, "UTF-8");
                svgGenerator.stream(out, true /* use css */);
                outputStream.flush();
            } catch (IOException ex) {
                Logger.getLogger(RasterLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
}//GEN-LAST:event_exportButtonActionPerformed

    private void leftSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_leftSpinnerStateChanged
        updateEstim();
    }//GEN-LAST:event_leftSpinnerStateChanged

    private void rightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rightSpinnerStateChanged
        updateEstim();
    }//GEN-LAST:event_rightSpinnerStateChanged

    private void leftToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftToggleButtonActionPerformed
        regPlot.clearDomainMarkers();
    }//GEN-LAST:event_leftToggleButtonActionPerformed

    private void rightToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightToggleButtonActionPerformed
        regPlot.clearDomainMarkers();
    }//GEN-LAST:event_rightToggleButtonActionPerformed

    private void showPointCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPointCheckBoxActionPerformed
        updatePlot();
    }//GEN-LAST:event_showPointCheckBoxActionPerformed

    private void curveComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_curveComboBoxActionPerformed
        updatePlot();
        leftToggleButton.setEnabled("Mq".equals(curveComboBox.getSelectedItem()));
        rightToggleButton.setEnabled("Mq".equals(curveComboBox.getSelectedItem()));
    }//GEN-LAST:event_curveComboBoxActionPerformed

    private void qUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qUpdateButtonActionPerformed
        TreeSet<Double> qSet = getqSet();
        M = method.getCurves(qSet);
        updateEstim();
        qComboBox.setModel(new DefaultComboBoxModel(qSet.toArray()));
    }//GEN-LAST:event_qUpdateButtonActionPerformed

    private void viewqEstimButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewqEstimButtonActionPerformed
        new EstimationFrame(this, new EstimationFactory(method.getSimpleMethod((Double)qComboBox.getSelectedItem()))).setVisible(true);
    }//GEN-LAST:event_viewqEstimButtonActionPerformed


    private void writeCurve(SortedMap<Double, Double> curve, Writer w, String xLabel, String yLabel) throws IOException {
        w.write(xLabel + "\t" + yLabel + "\n");
        for(Double x : curve.keySet()) {
            w.write(x + "\t" + curve.get(x) + "\n");
        }
        w.write("\n");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox curveComboBox;
    private javax.swing.JButton exportButton;
    private javax.swing.JTextArea infoTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner leftSpinner;
    private javax.swing.JToggleButton leftToggleButton;
    private javax.swing.JComboBox qComboBox;
    private javax.swing.JSpinner qMaxSpinner;
    private javax.swing.JSpinner qMinSpinner;
    private javax.swing.JSpinner qStepSpinner;
    private javax.swing.JButton qUpdateButton;
    private javax.swing.JSpinner rightSpinner;
    private javax.swing.JToggleButton rightToggleButton;
    private javax.swing.JCheckBox showPointCheckBox;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JButton viewqEstimButton;
    // End of variables declaration//GEN-END:variables


}
