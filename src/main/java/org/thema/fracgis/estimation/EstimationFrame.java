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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.SpinnerListModel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.thema.common.Util;
import org.thema.drawshape.layer.RasterLayer;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Frame for unifractal dimension estimation.
 * 
 * @author Gilles Vuidel
 */
public class EstimationFrame extends javax.swing.JFrame implements ChartMouseListener {

    private JFreeChart chart;
    private ChartPanel chartPanel;
    private EstimationFactory estimFactory;
    private Estimation estim;
    private XYPlot regPlot;
    private XYPlot scalingPlot;
    private XYPlot otherPlot;
    private HashMap<String, TreeMap<Double, Double>> otherCurves = new HashMap<>();
    
    /** 
     * Creates new form EstimationFrame.
     * @param frm the parent frame
     * @param estimFac the estimation factory
     */
    public EstimationFrame(JFrame frm, EstimationFactory estimFac) {
        initComponents();
        setLocationRelativeTo(frm);
        this.estimFactory = estimFac;
        estim = estimFactory.getDefaultEstimation();
        
        DefaultComboBoxModel model = new DefaultComboBoxModel(EstimationFactory.Type.values());
        model.setSelectedItem(estim.getType());
        typeComboBox.setModel(model);
        
        setTitle(estim.getMethod().getDetailName());
        
        setEstimation(estim);
    }

    private void setEstimation(Estimation estim) {
        this.estim = estim;
        regPlot = estim.getPlot();
        lineCheckBoxActionPerformed(null);
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(regPlot.getDomainAxis());
        plot.add(regPlot);
        chart = new JFreeChart(plot);
        chartPanel = new ChartPanel(chart);
        chartPanel.addChartMouseListener(this);
        splitPane.setRightComponent(chartPanel);

        modelComboBox.setModel(new DefaultComboBoxModel(estim.getModels().toArray()));
        Range r = estim.getRange();
        SpinnerListModel model = new SpinnerListModel(estim.getCurve().keySet().toArray());
        model.setValue(r.getLowerBound());
        leftSpinner.setModel(model);
        model = new SpinnerListModel(estim.getCurve().keySet().toArray());
        model.setValue(r.getUpperBound());
        rightSpinner.setModel(model);
        
        estim.getMethod().getGroupLayer().setRange(r.getLowerBound(), r.getUpperBound());
        
        infoTextArea.setText(estim.getResultInfo());
    }
    
    
    public void addOtherCurve(String name, TreeMap<Double, Double> clusters) {
        otherCurves.put(name, clusters);
        curveComboBox.addItem(name);
    }
    
    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        if(!rightToggleButton.isSelected() && !leftToggleButton.isSelected()) {
            return;
        }

        regPlot.clearDomainMarkers();
        
        Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
        double x = regPlot.getDomainAxis().java2DToValue(p.getX(),
                chartPanel.getChartRenderingInfo().getPlotInfo().getSubplotInfo(0).getDataArea(),
                regPlot.getDomainAxisEdge());
        if(rightToggleButton.isSelected()) {
            rightSpinner.setValue(estim.getCurve().lowerKey(x));
            rightToggleButton.setSelected(false);
        } else {
            leftSpinner.setValue(estim.getCurve().higherKey(x));
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
                chartPanel.getChartRenderingInfo().getPlotInfo().getSubplotInfo(0).getDataArea(),
                regPlot.getDomainAxisEdge());
        regPlot.clearDomainMarkers();
        regPlot.addDomainMarker(new ValueMarker(x, Color.BLACK, new BasicStroke(1)));
    }

    private void updateEstim(double xmin, double xmax) {
        estim.setRange(xmin, xmax);

        updatePlot();

        Range r = estim.getRange();
        rightSpinner.setValue(r.getUpperBound());
        leftSpinner.setValue(r.getLowerBound());
        estim.getMethod().getGroupLayer().setRange(r.getLowerBound(), r.getUpperBound());
    }

    private void updatePlot() {
        regPlot = estim.getPlot();
        lineCheckBoxActionPerformed(null);
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(regPlot.getDomainAxis());
        plot.add(regPlot);
        if(scalingCheckBox.isSelected()) {
            plot.add(scalingPlot);
        }
        if(curveComboBox.getSelectedIndex() > 0) {
            plot.add(otherPlot);
        }
        chart = new JFreeChart(plot);
        chartPanel.setChart(chart);

        infoTextArea.setText(estim.getResultInfo());
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
        jLabel4 = new javax.swing.JLabel();
        modelComboBox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        scalingCheckBox = new javax.swing.JCheckBox();
        smoothSpinner = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        leftToggleButton = new javax.swing.JToggleButton();
        leftSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        rightToggleButton = new javax.swing.JToggleButton();
        jLabel6 = new javax.swing.JLabel();
        rightSpinner = new javax.swing.JSpinner();
        typeComboBox = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        curveComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        lineCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Estimation");

        splitPane.setDividerLocation(300);

        infoTextArea.setColumns(10);
        infoTextArea.setEditable(false);
        infoTextArea.setRows(5);
        jScrollPane1.setViewportView(infoTextArea);

        exportButton.setText("Export");
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Model");

        modelComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelComboBoxActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Scaling behaviour"));

        jLabel5.setText("Smooth");

        scalingCheckBox.setText("Show");
        scalingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scalingCheckBoxActionPerformed(evt);
            }
        });

        smoothSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 1.0d, 0.01d));
        smoothSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                smoothSpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scalingCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(smoothSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(scalingCheckBox)
                .addComponent(jLabel5)
                .addComponent(smoothSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

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

        typeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        jLabel8.setText("Type");

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Other curve"));

        curveComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "(none)" }));
        curveComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                curveComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Show");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(curveComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(curveComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel1))
        );

        lineCheckBox.setText("Show line");
        lineCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineCheckBoxActionPerformed(evt);
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
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(typeComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(modelComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(exportButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lineCheckBox)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(modelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exportButton)
                    .addComponent(lineCheckBox)))
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
                estim.saveToText(w);
                double smooth = (Double)smoothSpinner.getValue();
                if(smooth > 0.0) {
                    w.write("\nX\tSmoothed SB " + smooth + "\n");
                    double[][] sb = estim.getSmoothedScalingBehaviour(smooth);
                    for(int i = 0; i < sb[0].length; i++) {
                        w.write(sb[0][i] + "\t" + sb[1][i] + "\n");
                    }
                }
                for(String key : otherCurves.keySet()) {
                    TreeMap<Double, Double> curve = otherCurves.get(key);
                    w.write("\nX\t" + key + "\n");
                    for(Double x : curve.keySet()) {
                        w.write(x + "\t" + curve.get(x) + "\n");
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(EstimationFrame.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else { // SVG
        
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
            } catch (IOException ex) {
                Logger.getLogger(RasterLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
}//GEN-LAST:event_exportButtonActionPerformed

    private void modelComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelComboBoxActionPerformed
        estim.setModel(modelComboBox.getSelectedIndex());
        updatePlot();
    }//GEN-LAST:event_modelComboBoxActionPerformed

    private void scalingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scalingCheckBoxActionPerformed
        if(scalingCheckBox.isSelected()) {
           smoothSpinnerStateChanged(null);
           ((CombinedDomainXYPlot)chart.getXYPlot()).add(scalingPlot);
        } else {
            ((CombinedDomainXYPlot)chart.getXYPlot()).remove(scalingPlot);
        }

    }//GEN-LAST:event_scalingCheckBoxActionPerformed

    private void smoothSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_smoothSpinnerStateChanged
        double [][] curve = estim.getScalingBehaviour();
        double bandwidth = (Double)smoothSpinner.getValue();
        List<Integer> pointInflex = null;
        if(bandwidth > 0) {
            try {
                curve = estim.getSmoothedScalingBehaviour(bandwidth);
                pointInflex = estim.getInflexPointIndices(bandwidth, 0);
            } catch (Exception ex) {
                Logger.getLogger(EstimationFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("Scaling behaviour", curve);
        if(pointInflex != null) {
            double [][] pt = new double[2][pointInflex.size()];
            for(int i = 0; i < pointInflex.size(); i++) {
                pt[0][i] = curve[0][pointInflex.get(i)];
                pt[1][i] = curve[1][pointInflex.get(i)];
            }
            dataset.addSeries("Inflex points", pt);
        }
        
        if(scalingPlot == null) {
            NumberAxis axis = new NumberAxis();
            axis.setAutoRangeIncludesZero(false);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
            renderer.setSeriesLinesVisible(1, false);
            renderer.setSeriesShapesVisible(1, true);            
            scalingPlot = new XYPlot(dataset,
                    null, axis, renderer);
        } else {
            scalingPlot.getRangeAxis().setAutoRange(false);
            scalingPlot.setDataset(dataset);
        }
    }//GEN-LAST:event_smoothSpinnerStateChanged

    private void leftSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_leftSpinnerStateChanged
        updateEstim((Double)leftSpinner.getValue(), (Double)rightSpinner.getValue());
    }//GEN-LAST:event_leftSpinnerStateChanged

    private void rightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rightSpinnerStateChanged
        updateEstim((Double)leftSpinner.getValue(), (Double)rightSpinner.getValue());
    }//GEN-LAST:event_rightSpinnerStateChanged

    private void leftToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftToggleButtonActionPerformed
        regPlot.clearDomainMarkers();
    }//GEN-LAST:event_leftToggleButtonActionPerformed

    private void rightToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightToggleButtonActionPerformed
        regPlot.clearDomainMarkers();
    }//GEN-LAST:event_rightToggleButtonActionPerformed

    private void typeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboBoxActionPerformed
        setEstimation(estimFactory.getEstimation((EstimationFactory.Type)typeComboBox.getSelectedItem()));
    }//GEN-LAST:event_typeComboBoxActionPerformed

    private void curveComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_curveComboBoxActionPerformed
        if(curveComboBox.getSelectedIndex() == 0) {
            ((CombinedDomainXYPlot)chart.getXYPlot()).remove(otherPlot);
            return;
        }
        String name = curveComboBox.getSelectedItem().toString();
        TreeMap<Double, Double> curve = otherCurves.get(name);
        XYSeries serie = new XYSeries(name);
        for(Double key : curve.keySet()) {
            serie.add(key, curve.get(key));
        }

        NumberAxis axis = typeComboBox.getSelectedItem().equals(EstimationFactory.Type.DIRECT) ? new NumberAxis() : new LogarithmicAxis("");
        axis.setAutoRangeIncludesZero(false);
        otherPlot = new XYPlot(new XYSeriesCollection(serie),
                null, axis, new XYLineAndShapeRenderer(true, false));
        
        ((CombinedDomainXYPlot)chart.getXYPlot()).add(otherPlot);
    }//GEN-LAST:event_curveComboBoxActionPerformed

    private void lineCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineCheckBoxActionPerformed
        ((XYLineAndShapeRenderer)regPlot.getRenderer()).setSeriesLinesVisible(1, lineCheckBox.isSelected());
        ((XYLineAndShapeRenderer)regPlot.getRenderer()).setSeriesShapesVisible(1, !lineCheckBox.isSelected());
    }//GEN-LAST:event_lineCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox curveComboBox;
    private javax.swing.JButton exportButton;
    private javax.swing.JTextArea infoTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner leftSpinner;
    private javax.swing.JToggleButton leftToggleButton;
    private javax.swing.JCheckBox lineCheckBox;
    private javax.swing.JComboBox modelComboBox;
    private javax.swing.JSpinner rightSpinner;
    private javax.swing.JToggleButton rightToggleButton;
    private javax.swing.JCheckBox scalingCheckBox;
    private javax.swing.JSpinner smoothSpinner;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JComboBox typeComboBox;
    // End of variables declaration//GEN-END:variables


}
