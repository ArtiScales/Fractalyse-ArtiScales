/*
 * Copyright (C) 2018 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thema.fracgis.sampling;

import org.thema.fracgis.sampling.Sampling.Sequence;

/**
 *
 * @author Gilles Vuidel
 */
public class SamplingPanel extends javax.swing.JPanel {

    /**
     * Creates new form SamplingPanel
     */
    public SamplingPanel() {
        initComponents();
    }
    
    public void setSizes(double min, double max) {
        if(min > 0) {
            minTextField.setText(""+round(min, 3));
        }
        if(max > 0) {
            maxTextField.setText(""+round(max, 3));
        }
        if(arithRadioButton.isSelected()) {
            double coef = (max-min)/100;
            coefSpinner.setValue(round(coef, 2));
        }
    }
    
    public void setSampling(DefaultSampling sampling) {
        if(sampling.getMinSize() > 0) {
            minTextField.setText(""+sampling.getMinSize());
        }
        if(sampling.getMaxSize() > 0) {
            maxTextField.setText(""+sampling.getMaxSize());
        }
        coefSpinner.setValue(sampling.getClass());
        geomRadioButton.setSelected(sampling.getSeq() == Sequence.GEOM);
        arithRadioButton.setSelected(sampling.getSeq() == Sequence.ARITH);
    }
    
    public DefaultSampling getSampling() {
        double min = Double.parseDouble(minTextField.getText());
        double max = Double.parseDouble(maxTextField.getText());
        double coef = (Double)coefSpinner.getValue();
        Sequence seq = geomRadioButton.isSelected() ? Sequence.GEOM : Sequence.ARITH;
        if(min >= max) {
            throw new IllegalArgumentException("Max size must be greater than min size");
        }
        if(min <= 0 || max <= 0) {
            throw new IllegalArgumentException("Sizes must be greater than 0");
        }
        if(seq == Sequence.ARITH && coef <= 0) {
            throw new IllegalArgumentException("Coef must be greater than 0");
        }
        if(seq == Sequence.GEOM && coef <= 1) {
            throw new IllegalArgumentException("Coef must be greater than 1");
        }
        
        return new DefaultSampling(min, max, coef, seq);
    }

    public static double round(double val, int digit) {
        int exp = (int) Math.log10(val) - digit;
        if(val >= 1) {
            exp++;
        }
        double size = Math.pow(10, exp);
        return Math.round(val/size) * size;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        coefSpinner = new javax.swing.JSpinner();
        maxTextField = new javax.swing.JTextField();
        minTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        geomRadioButton = new javax.swing.JRadioButton();
        arithRadioButton = new javax.swing.JRadioButton();

        jLabel7.setText("Min size");

        jLabel8.setText("Max size");

        jLabel1.setText("Coef");

        coefSpinner.setModel(new javax.swing.SpinnerNumberModel(2.0d, 0.0d, null, 1.0d));

        minTextField.setText("1");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Sequence"));

        buttonGroup1.add(geomRadioButton);
        geomRadioButton.setSelected(true);
        geomRadioButton.setText("Geometric");

        buttonGroup1.add(arithRadioButton);
        arithRadioButton.setText("Arithmetic");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(geomRadioButton)
                    .addComponent(arithRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(geomRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(arithRadioButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(coefSpinner)
                    .addComponent(minTextField, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(minTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(maxTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(coefSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton arithRadioButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSpinner coefSpinner;
    private javax.swing.JRadioButton geomRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField maxTextField;
    private javax.swing.JTextField minTextField;
    // End of variables declaration//GEN-END:variables
}