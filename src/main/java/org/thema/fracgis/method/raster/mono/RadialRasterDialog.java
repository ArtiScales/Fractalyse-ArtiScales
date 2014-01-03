/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BoxCountingDialog.java
 *
 * Created on 2 févr. 2010, 10:57:31
 */

package org.thema.fracgis.method.raster.mono;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import org.thema.common.JTS;
import org.thema.drawshape.PanelMap;
import org.thema.drawshape.PointShape;
import org.thema.drawshape.SelectableShape;
import org.thema.drawshape.style.PointStyle;
import org.thema.drawshape.ui.MapViewer;
import org.thema.fracgis.BinRasterLayer;
import org.thema.fracgis.LayerModel;

/**
 *
 * @author gvuidel
 */
public class RadialRasterDialog extends javax.swing.JDialog implements PanelMap.ShapeMouseListener {

    public boolean isOk = false;
    public double maxSize;
    public Coordinate centre;
    public BinRasterLayer layer;
    
    private MapViewer mapViewer;
    private PointShape centreShape;

    /** Creates new form BoxCountingDialog */
    public RadialRasterDialog(java.awt.Frame parent, LayerModel<BinRasterLayer> model, MapViewer mapViewer) {
        super(parent, false);
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(okButton);
        if(model.getSize() == 0) {
            JOptionPane.showMessageDialog(parent, "No raster layer.");
            throw new IllegalArgumentException("No raster layer.");
        }
        layerComboBox.setModel(model);
        layerComboBoxActionPerformed(null);
        
        mapViewer.addMouseListener(this);
        mapViewer.setCursorMode(PanelMap.INPUT_CURSOR_MODE);
        this.mapViewer = mapViewer;
        
        centreShape = new PointShape(centre.x, centre.y);
        centreShape.setStyle(new PointStyle(Color.BLACK, Color.RED));
        mapViewer.getMap().addShape(centreShape);
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
        layerComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        maxTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        centreTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Box counting");
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

        layerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layerComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Layer");

        jLabel3.setText("Max size");

        jLabel6.setText("Centre");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 117, Short.MAX_VALUE)
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(jLabel6)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layerComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(centreTextField)
                            .add(maxTextField))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {cancelButton, okButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(layerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(centreTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(maxTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        layer = (BinRasterLayer) layerComboBox.getSelectedItem();
        String[] coords = centreTextField.getText().split(",");
        centre = new Coordinate(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
        maxSize = Double.parseDouble(maxTextField.getText());
        
        isOk = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void layerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layerComboBoxActionPerformed
        layer = (BinRasterLayer) layerComboBox.getSelectedItem();
        centre = RadialRasterMethod.getDefaultCentre(JTS.rectToEnv(layer.getBounds()));
        centreTextField.setText(centre.x + "," + centre.y);
        maxTextField.setText(String.format(Locale.US, "%g", RadialRasterMethod.getDefaultMaxSize(JTS.rectToEnv(layer.getBounds()), centre)));
        if(centreShape != null)
            centreShape.setPoint2D(new Point2D.Double(centre.x, centre.y));
    }//GEN-LAST:event_layerComboBoxActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        mapViewer.removeShapeMouseListener(this);
        mapViewer.getMap().removeShapes(Arrays.asList(centreShape));
    }//GEN-LAST:event_formWindowClosed

    public void mouseClicked(Point2D p, List<SelectableShape> shapes, MouseEvent sourceEvent, int cursorMode) {
        layer = (BinRasterLayer) layerComboBox.getSelectedItem();
        centreTextField.setText(p.getX() + "," + p.getY());
        maxTextField.setText(String.format(Locale.US, "%g", RadialRasterMethod.getDefaultMaxSize(JTS.rectToEnv(layer.getBounds()), new Coordinate(p.getX(), p.getY()))));
        centreShape.setPoint2D(p);
        mapViewer.getMap().fullRepaint();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField centreTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox layerComboBox;
    private javax.swing.JTextField maxTextField;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables



}