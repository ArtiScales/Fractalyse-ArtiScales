/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DesserteDialog.java
 *
 * Created on 4 févr. 2010, 11:08:40
 */

package org.thema.fracgis.method.network;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;
import javax.swing.JOptionPane;
import org.thema.common.parallel.TaskMonitor;
import org.thema.drawshape.PanelMap;
import org.thema.drawshape.PanelMap.ShapeMouseListener;
import org.thema.drawshape.SelectableShape;
import org.thema.drawshape.ui.MapViewer;
import org.thema.fracgis.LayerModel;
import org.thema.fracgis.SpatialGraphLayer;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.estimation.EstimationFrame;
import org.thema.graph.SpatialGraph;


/**
 *
 * @author gvuidel
 */
public class LocalNetworkDialog extends javax.swing.JDialog implements ShapeMouseListener {

    MapViewer viewer;

    /** Creates new form DesserteDialog */
    public LocalNetworkDialog(java.awt.Frame parent, MapViewer viewer) {
        super(parent, false);
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(okButton);
        
        LayerModel model = new LayerModel(viewer.getLayers(), SpatialGraphLayer.class);
        if(model.getSize() == 0) {
            JOptionPane.showMessageDialog(parent, "No network layer.");
            throw new IllegalArgumentException("No network layer.");
        }
        netComboBox.setModel(model);

        this.viewer = viewer;
        viewer.addMouseListener(this);
        viewer.setCursorMode(PanelMap.INPUT_CURSOR_MODE);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        netComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        xTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        yTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        resSpinner = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Local network");
        setAlwaysOnTop(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        netComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                netComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Network layer");

        jLabel3.setText("Starting point");

        xTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        jLabel4.setText("X");

        jLabel5.setText("Y");

        yTextField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        jLabel1.setText("Resolution");

        resSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), null, null, Double.valueOf(1.0d)));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(32, 32, 32)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel5)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cancelButton))
                            .add(xTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(yTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(resSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(netComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(netComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(resSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(xTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(yTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 27, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        final double cx = Double.parseDouble(xTextField.getText());
        final double cy = Double.parseDouble(yTextField.getText());
        final SpatialGraphLayer layer = (SpatialGraphLayer) netComboBox.getSelectedItem();
        final SpatialGraph graph = layer.getSpatialGraph();
        
        setVisible(false);
        dispose();
        viewer.removeShapeMouseListener(this);

        new Thread(new Runnable() {
            public void run() {
                LocalNetworkMethod method = new LocalNetworkMethod(layer.getName(), graph, new GeometryFactory()
                .createPoint(new Coordinate(cx, cy)), (Double)resSpinner.getValue());
                method.execute(new TaskMonitor(LocalNetworkDialog.this, "Local network", "", 0, 100), true);

                new EstimationFrame(null, new EstimationFactory(method)).setVisible(true);
            }
        }).start();
        

    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose(); 
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void netComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_netComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_netComboBoxActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        viewer.removeShapeMouseListener(this);
    }//GEN-LAST:event_formWindowClosed

    public void mouseClicked(Point2D p, List<SelectableShape> shapes, MouseEvent sourceEvent, int cursorMode) {
        xTextField.setText(""+p.getX());
        yTextField.setText(""+p.getY());
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JComboBox netComboBox;
    private javax.swing.JButton okButton;
    private javax.swing.JSpinner resSpinner;
    private javax.swing.JTextField xTextField;
    private javax.swing.JTextField yTextField;
    // End of variables declaration//GEN-END:variables


}
