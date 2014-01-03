/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BoxCountingDialog.java
 *
 * Created on 2 févr. 2010, 10:57:31
 */

package org.thema.fracgis.method.vector;

import com.vividsolutions.jts.geom.Envelope;
import java.util.Collection;
import java.util.Locale;
import javax.swing.JOptionPane;
import org.thema.drawshape.feature.DefaultFeatureCoverage;
import org.thema.drawshape.feature.Feature;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.fracgis.LayerModel;
import org.thema.fracgis.method.vector.mono.BoxCountingMethod;

/**
 *
 * @author gvuidel
 */
public class BoxCountingDialog extends javax.swing.JDialog {

    public boolean isOk = false;
    public double minSize, maxSize, coef;
    public int d;
    public FeatureLayer layer;
    public boolean viewBoxes;

    /** Creates new form BoxCountingDialog */
    public BoxCountingDialog(java.awt.Frame parent, LayerModel<FeatureLayer> model) {
        super(parent, true);
        initComponents();
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(okButton);
        if(model.getSize() == 0) {
            JOptionPane.showMessageDialog(parent, "No vector layer.");
            throw new IllegalArgumentException("No vector layer.");
        }
        layerComboBox.setModel(model);
        layerComboBoxActionPerformed(null);
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
        minTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        maxTextField = new javax.swing.JTextField();
        coefSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        dSpinner = new javax.swing.JSpinner();
        viewBoxCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Box counting");

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

        jLabel2.setText("Min size");

        jLabel3.setText("Max size");

        coefSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(2.0d), Double.valueOf(1.0d), null, Double.valueOf(1.0d)));

        jLabel4.setText("Coef");

        jLabel5.setText("Gliding grid");

        dSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        viewBoxCheckBox.setText("View boxes");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(minTextField)
                            .add(maxTextField)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel4))
                        .add(35, 35, 35)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(coefSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 151, Short.MAX_VALUE))
                            .add(layerComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(viewBoxCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(0, 0, Short.MAX_VALUE)))
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
                    .add(jLabel2)
                    .add(minTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(maxTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(coefSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(dSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(viewBoxCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 24, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelButton)
                    .add(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        layer = (FeatureLayer) layerComboBox.getSelectedItem();
        minSize = Double.parseDouble(minTextField.getText());
        maxSize = Double.parseDouble(maxTextField.getText());
        coef = (Double)coefSpinner.getValue();
        d = (Integer)dSpinner.getValue();
        viewBoxes = viewBoxCheckBox.isSelected();
        
        isOk = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void layerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layerComboBoxActionPerformed
        FeatureLayer l = (FeatureLayer) layerComboBox.getSelectedItem();
        Collection<? extends Feature> features = l.getSelectedFeatures();
        if(features.isEmpty())
            features = l.getFeatures();
        DefaultFeatureCoverage cov = new DefaultFeatureCoverage(features);
        maxTextField.setText(String.format(Locale.US, "%g", BoxCountingMethod.getDefaultMax(cov)));
        double minArea = Double.MAX_VALUE;
        for(Feature f : features) {
            double area = f.getGeometry().getArea();
            if(area < minArea)
                minArea = area;
        }
        minTextField.setText(String.format(Locale.US, "%g", BoxCountingMethod.getDefaultMin(cov)));

    }//GEN-LAST:event_layerComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JSpinner coefSpinner;
    private javax.swing.JSpinner dSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JComboBox layerComboBox;
    private javax.swing.JTextField maxTextField;
    private javax.swing.JTextField minTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox viewBoxCheckBox;
    // End of variables declaration//GEN-END:variables

}
