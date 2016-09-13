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


package org.thema.fracgis;

import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.method.network.LocalNetworkDialog;
import org.thema.fracgis.method.network.DesserteDialog;
import org.thema.fracgis.method.vector.mono.DilationMethod;
import org.thema.fracgis.method.vector.mono.RadialMethod;
import org.thema.fracgis.method.vector.mono.DilationDialog;
import org.thema.fracgis.method.vector.mono.BoxCountingMethod;
import org.thema.fracgis.method.vector.BoxCountingDialog;
import org.thema.fracgis.method.vector.mono.RadialDialog;
import org.thema.fracgis.method.raster.RasterMethodDialog;
import org.thema.fracgis.method.raster.mono.DilationRasterMethod;
import org.thema.fracgis.method.raster.mono.RadialRasterDialog;
import org.thema.fracgis.method.raster.mono.BoxCountingRasterMethod;
import org.thema.fracgis.method.raster.mono.CorrelationMethod;
import org.thema.fracgis.method.raster.mono.RadialRasterMethod;
import org.thema.fracgis.tools.RasterizeDialog;
import org.thema.fracgis.tools.BinarizeDialog;
import org.thema.fracgis.batch.BatchVectorDialog;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.JOptionPane;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.thema.data.GlobalDataStore;
import org.thema.common.Config;
import org.thema.common.JTS;
import org.thema.common.JavaLoader;
import org.thema.common.RasterImage;
import org.thema.common.Util;
import org.thema.data.IOImage;
import org.thema.common.ProgressBar;
import org.thema.common.swing.PreferencesDialog;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.data.feature.Feature;
import org.thema.drawshape.image.CoverageShape;
import org.thema.drawshape.image.RasterShape;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.FeatureStyle;
import org.thema.drawshape.style.RasterStyle;
import org.thema.drawshape.style.table.ColorRamp;
import org.thema.fracgis.batch.*;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.estimation.EstimationFrame;
import org.thema.fracgis.method.*;
import org.thema.fracgis.method.raster.multi.MultiFracBoxCountingRasterMethod;
import org.thema.fracgis.method.vector.multi.MultiFracBoxCountingVectorMethod;
import org.thema.fracgis.estimation.MultiFracEstimationFrame;
import org.thema.fracgis.method.raster.multi.MultiFracWaveletMethod;
import org.thema.fracgis.sampling.MultiFracSampling;
import org.thema.fracgis.sampling.RasterBoxSampling;
import org.thema.fracgis.tools.RasterSelectionDialog;
import org.thema.fracgis.tools.VectorSelectionDialog;
import org.thema.graph.SpatialGraph;
import org.thema.process.Rasterizer;

/**
 * The main frame of the program and the main entry point.
 * 
 * @author Giles Vuidel
 */
public class MainFrame extends javax.swing.JFrame {

    private DefaultGroupLayer groupLayer;

    /** Creates new form MainFrame */
    public MainFrame() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/thema/fracgis/fractal.png")));
        initComponents();
        setLocationRelativeTo(null);
        setTitle("FracGIS - " + JavaLoader.getVersion(MainFrame.class));
        groupLayer = new DefaultGroupLayer("Layers", true);
        mapViewer.setRootLayer(groupLayer);
        mapViewer.disableInfoPanel();
        mapViewer.getMap().setMultipleSelection(true);
        Config.setProgressBar(mapViewer.getProgressBar());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mapViewer = new org.thema.drawshape.ui.MapViewer();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        loadVectorMenuItem = new javax.swing.JMenuItem();
        loadRasterMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        prefMenuItem = new javax.swing.JMenuItem();
        vectorMenu = new javax.swing.JMenu();
        boxCountingMenuItem = new javax.swing.JMenuItem();
        dilationMenuItem = new javax.swing.JMenuItem();
        radialMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        batchVectorMenuItem = new javax.swing.JMenuItem();
        multiFracVectorMenuItem1 = new javax.swing.JMenuItem();
        rasterMenu = new javax.swing.JMenu();
        boxCountingRasterMenuItem = new javax.swing.JMenuItem();
        dilRasterMenuItem = new javax.swing.JMenuItem();
        correlationMenuItem = new javax.swing.JMenuItem();
        radialRasterMenuItem = new javax.swing.JMenuItem();
        multiRadialRasterMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        multiFracBoxRasterMenuItem = new javax.swing.JMenuItem();
        waveletMenuItem = new javax.swing.JMenuItem();
        networkMenu = new javax.swing.JMenu();
        backBoneMenuItem = new javax.swing.JMenuItem();
        desserteMenuItem = new javax.swing.JMenuItem();
        localNetMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        rasterizeMenuItem = new javax.swing.JMenuItem();
        binarizeMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        selVectorMenuItem = new javax.swing.JMenuItem();
        selRasterMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FracGIS");

        fileMenu.setText("File");

        loadVectorMenuItem.setText("Load vector data...");
        loadVectorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadVectorMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadVectorMenuItem);

        loadRasterMenuItem.setText("Load raster data...");
        loadRasterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadRasterMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadRasterMenuItem);
        fileMenu.add(jSeparator1);

        prefMenuItem.setText("Preferences...");
        prefMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(prefMenuItem);

        jMenuBar1.add(fileMenu);

        vectorMenu.setText("Vector");

        boxCountingMenuItem.setText("Box counting");
        boxCountingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxCountingMenuItemActionPerformed(evt);
            }
        });
        vectorMenu.add(boxCountingMenuItem);

        dilationMenuItem.setText("Dilation");
        dilationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dilationMenuItemActionPerformed(evt);
            }
        });
        vectorMenu.add(dilationMenuItem);

        radialMenuItem.setText("Radial");
        radialMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radialMenuItemActionPerformed(evt);
            }
        });
        vectorMenu.add(radialMenuItem);
        vectorMenu.add(jSeparator2);

        batchVectorMenuItem.setText("Batch");
        batchVectorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchVectorMenuItemActionPerformed(evt);
            }
        });
        vectorMenu.add(batchVectorMenuItem);

        multiFracVectorMenuItem1.setText("Multi-fractal");
        multiFracVectorMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiFracVectorMenuItem1ActionPerformed(evt);
            }
        });
        vectorMenu.add(multiFracVectorMenuItem1);

        jMenuBar1.add(vectorMenu);

        rasterMenu.setText("Raster");

        boxCountingRasterMenuItem.setText("Box counting");
        boxCountingRasterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxCountingRasterMenuItemActionPerformed(evt);
            }
        });
        rasterMenu.add(boxCountingRasterMenuItem);

        dilRasterMenuItem.setText("Dilation");
        dilRasterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dilRasterMenuItemActionPerformed(evt);
            }
        });
        rasterMenu.add(dilRasterMenuItem);

        correlationMenuItem.setText("Correlation");
        correlationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                correlationMenuItemActionPerformed(evt);
            }
        });
        rasterMenu.add(correlationMenuItem);

        radialRasterMenuItem.setText("Radial");
        radialRasterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radialRasterMenuItemActionPerformed(evt);
            }
        });
        rasterMenu.add(radialRasterMenuItem);

        multiRadialRasterMenuItem.setText("Multi radial");
        multiRadialRasterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiRadialRasterMenuItemActionPerformed(evt);
            }
        });
        rasterMenu.add(multiRadialRasterMenuItem);

        jMenu2.setText("Multi-fractal");

        multiFracBoxRasterMenuItem.setText("Box");
        multiFracBoxRasterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiFracBoxRasterMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(multiFracBoxRasterMenuItem);

        waveletMenuItem.setText("Wavelet");
        waveletMenuItem.setEnabled(false);
        waveletMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                waveletMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(waveletMenuItem);

        rasterMenu.add(jMenu2);

        jMenuBar1.add(rasterMenu);

        networkMenu.setText("Network");

        backBoneMenuItem.setText("Backbone...");
        backBoneMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBoneMenuItemActionPerformed(evt);
            }
        });
        networkMenu.add(backBoneMenuItem);

        desserteMenuItem.setText("Desserte...");
        desserteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                desserteMenuItemActionPerformed(evt);
            }
        });
        networkMenu.add(desserteMenuItem);

        localNetMenuItem.setText("Radial...");
        localNetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localNetMenuItemActionPerformed(evt);
            }
        });
        networkMenu.add(localNetMenuItem);

        jMenuBar1.add(networkMenu);

        toolsMenu.setText("Tools");

        rasterizeMenuItem.setText("Rasterize...");
        rasterizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rasterizeMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(rasterizeMenuItem);

        binarizeMenuItem.setText("Binarize...");
        binarizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                binarizeMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(binarizeMenuItem);

        jMenu1.setText("Selection");

        selVectorMenuItem.setText("Vector...");
        selVectorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selVectorMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(selVectorMenuItem);

        selRasterMenuItem.setText("Raster...");
        selRasterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selRasterMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(selRasterMenuItem);

        toolsMenu.add(jMenu1);

        jMenuBar1.add(toolsMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 798, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadVectorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadVectorMenuItemActionPerformed
    final File f = Util.getFile(".shp", "Shapefile");
    if(f == null) {
        return;
    }

    new Thread(new Runnable() {
        @Override
        public void run() {
         try {
            String layer = f.getName().substring(0, f.getName().length()-4);
            List<DefaultFeature> features = GlobalDataStore.getFeatures(f, null, Config.getProgressBar());
            CoordinateReferenceSystem crs = GlobalDataStore.getCRS(f);
            Geometry g = features.get(0).getGeometry();
            boolean isLinear = g instanceof Lineal;
            if(isLinear) {
                DefaultGroupLayer gl = new DefaultGroupLayer(layer + "-Network");
                FeatureLayer fl = new FeatureLayer(layer, features, null, crs);
                gl.addLayerFirst(fl);
                SpatialGraph network = new SpatialGraph(features, new GeometryPrecisionReducer(new PrecisionModel(1000)));
                SpatialGraphLayer graphlayer = new SpatialGraphLayer(layer + "-Graph", network);
                graphlayer.setVisible(false);
                gl.addLayerFirst(graphlayer);
                groupLayer.addLayerFirst(gl);
                gl.setRemovable(true);
            } else {
                FeatureLayer fl = new FeatureLayer(layer, features, null, crs);
                fl.setRemovable(true);
                groupLayer.addLayerFirst(fl);
            }

            mapViewer.setTreeLayerVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(MainFrame.this, "Error while loading shapefile.\n" + ex.getLocalizedMessage());
        }
       }
    }).start();
       
}//GEN-LAST:event_loadVectorMenuItemActionPerformed

    private void backBoneMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBoneMenuItemActionPerformed
        BackboneDialog dlg = new BackboneDialog(this, mapViewer);
        dlg.setVisible(true);
    }//GEN-LAST:event_backBoneMenuItemActionPerformed

    private void boxCountingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxCountingMenuItemActionPerformed
        final BoxCountingDialog dlg = new BoxCountingDialog(this, new LayerModel(mapViewer.getLayers(), FeatureLayer.class),
            new DefaultSampling());
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                BoxCountingMethod boxCountingMethod = new BoxCountingMethod(dlg.layer.getName(), dlg.sampling, 
                        new DefaultFeatureCoverage(dlg.layer.getFeatures()), dlg.d, dlg.viewBoxes);
                launchMethod(boxCountingMethod);
            }
        }).start();      
    }//GEN-LAST:event_boxCountingMenuItemActionPerformed

    private void desserteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_desserteMenuItemActionPerformed
        DesserteDialog dlg = new DesserteDialog(this, mapViewer);
        dlg.setVisible(true);
    }//GEN-LAST:event_desserteMenuItemActionPerformed

    private void localNetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localNetMenuItemActionPerformed
        LocalNetworkDialog dlg = new LocalNetworkDialog(this, mapViewer);
        dlg.setVisible(true);
    }//GEN-LAST:event_localNetMenuItemActionPerformed

    private void correlationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_correlationMenuItemActionPerformed
        final RasterMethodDialog dlg = new RasterMethodDialog(this, "Correlation", 
                new LayerModel(mapViewer.getLayers(), BinRasterLayer.class), new DefaultSampling());
        dlg.setVisible(true);

        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() { 
                CorrelationMethod method = new CorrelationMethod(dlg.layer.getName(), dlg.sampling, 
                        dlg.layer.getImageShape().getImage(), JTS.rectToEnv(dlg.layer.getBounds()));
                launchMethod(method);
            }
        }).start();
    }//GEN-LAST:event_correlationMenuItemActionPerformed

    private void loadRasterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadRasterMenuItemActionPerformed
        File f = Util.getFile(".tif|.asc", "Image");
        if(f == null) {
            return;
        }

        RasterLayer fl;
        try {
            try {
                GridCoverage2D grid = IOImage.loadCoverage(f);
                fl = new RasterLayer(f.getName(), new CoverageShape(grid, new RasterStyle()),
                        grid.getCoordinateReferenceSystem2D());
            } catch(DataSourceException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.INFO, "Impossible to load GeoTiff. Try simple TIFF", ex);
                BufferedImage img = ImageIO.read(f);
                fl = new RasterLayer(f.getName(), new RasterShape(img, img.getRaster().getBounds()));
            }
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Error while loading raster image.\n" + ex.getLocalizedMessage());
            return;
        }

        if(isBinary(fl.getImageShape().getImage())) {
            fl = new BinRasterLayer(f.getName(), fl.getImageShape(),fl.getCRS());
        }
        fl.setRemovable(true);
        groupLayer.addLayerFirst(fl);

        mapViewer.setTreeLayerVisible(true);
    }//GEN-LAST:event_loadRasterMenuItemActionPerformed

    private void rasterizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rasterizeMenuItemActionPerformed
        final RasterizeDialog dlg = new RasterizeDialog(this, new LayerModel(mapViewer.getLayers(), FeatureLayer.class));
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Collection<? extends Feature> features =
                        dlg.layer.getSelectedFeatures().isEmpty() ? dlg.layer.getFeatures() : dlg.layer.getSelectedFeatures();

                ProgressBar monitor = Config.getProgressBar("Rasterize...", features.size());
                DefaultFeatureCoverage cov = new DefaultFeatureCoverage(features);
                Rasterizer rasterizer = new Rasterizer(cov, dlg.resolution, dlg.field);
                rasterizer.setPolygonalRasterization(dlg.polyMode);
                Raster raster = rasterizer.rasterize(monitor);
                monitor.close();
                if(raster == null) {
                    return;
                }
                RasterLayer l;
                if(dlg.field == null && dlg.polyMode != Rasterizer.PolyRasterMode.AREA) {
                    l = new BinRasterLayer(dlg.layer.getName() + "-raster_" + dlg.resolution,
                            new RasterShape(new RasterImage(raster), rasterizer.getEnvelope()), dlg.layer.getCRS());
                } else {
                    l = new RasterLayer(dlg.layer.getName() + "-raster_" + dlg.field + "_" + dlg.resolution, 
                        new RasterShape(new RasterImage(raster), rasterizer.getEnvelope()), dlg.layer.getCRS());
                    l.setRemovable(true);
                }
                ((DefaultGroupLayer) mapViewer.getLayers()).addLayerFirst(l);       
            }
        }).start();
    }//GEN-LAST:event_rasterizeMenuItemActionPerformed

    private void binarizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_binarizeMenuItemActionPerformed
        BinarizeDialog dlg = new BinarizeDialog(this, mapViewer);
        dlg.setVisible(true);
    }//GEN-LAST:event_binarizeMenuItemActionPerformed

    private void prefMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefMenuItemActionPerformed
        PreferencesDialog dlg = new PreferencesDialog(this, true);
        dlg.setProcPanelVisible(true);
        dlg.setVisible(true);
    }//GEN-LAST:event_prefMenuItemActionPerformed

    private void boxCountingRasterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxCountingRasterMenuItemActionPerformed
        final RasterMethodDialog dlg = new RasterMethodDialog(this, "Box counting", new LayerModel(mapViewer.getLayers(), BinRasterLayer.class), new RasterBoxSampling());
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                BoxCountingRasterMethod boxCountingMethod = new BoxCountingRasterMethod(dlg.layer.getName(),
                        new RasterBoxSampling(dlg.sampling),
                        dlg.layer.getImageShape().getImage(), JTS.rectToEnv(dlg.layer.getBounds()));
                launchMethod(boxCountingMethod);
            }
        }).start();
    }//GEN-LAST:event_boxCountingRasterMenuItemActionPerformed

    private void dilationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dilationMenuItemActionPerformed
        final DilationDialog dlg = new DilationDialog(this, new LayerModel(mapViewer.getLayers(), FeatureLayer.class));
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                DilationMethod dilMethod = new DilationMethod(dlg.layer.getName(), new DefaultSampling(dlg.startDist, dlg.maxDist, dlg.coef),
                        new DefaultFeatureCoverage(dlg.layer.getFeatures()), dlg.maxDist == -1, dlg.viewBuf);
                EstimationFrame estimFrame = launchMethod(dilMethod);
                estimFrame.addOtherCurve("Clusters", dilMethod.getClusters());
            }
        }).start();
    }//GEN-LAST:event_dilationMenuItemActionPerformed

    private void radialMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radialMenuItemActionPerformed
        final RadialDialog dlg = new RadialDialog(this, new LayerModel(mapViewer.getLayers(), FeatureLayer.class), mapViewer);
        dlg.setVisible(true);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                // on attend que la boite de dialogue soit fermée
                while(dlg.isDisplayable()) { 
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(!dlg.isOk) {
                    return;
                }
                RadialMethod radMethod = new RadialMethod(dlg.layer.getName(), dlg.sampling, new DefaultFeatureCoverage(dlg.layer.getFeatures()), 
                        dlg.centre, BufferParameters.CAP_ROUND);
                launchMethod(radMethod);
            }
        }).start();
    }//GEN-LAST:event_radialMenuItemActionPerformed

    private void dilRasterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dilRasterMenuItemActionPerformed
        final RasterMethodDialog dlg = new RasterMethodDialog(this, "Dilation", 
                new LayerModel(mapViewer.getLayers(), BinRasterLayer.class), new DefaultSampling());
        dlg.setVisible(true);

        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() { 
                DilationRasterMethod method = new DilationRasterMethod(dlg.layer.getName(), dlg.sampling, dlg.layer.getImageShape().getImage(), 
                        JTS.rectToEnv(dlg.layer.getBounds()));
                launchMethod(method);
            }
        }).start();
    }//GEN-LAST:event_dilRasterMenuItemActionPerformed

    private void batchVectorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchVectorMenuItemActionPerformed
        final BatchVectorDialog dlg = new BatchVectorDialog(this, new LayerModel(mapViewer.getLayers(), FeatureLayer.class));
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                BatchVectorMethod batchMethod;
                if(dlg.grid) {
                    batchMethod = new BatchVectorMethod(dlg.layer, dlg.method, dlg.resolution);
                } else {
                    batchMethod = new BatchVectorMethod(dlg.layer, dlg.method, dlg.zoneLayer, dlg.idZone);
                }
                batchMethod.execute(Config.getProgressBar("Batch " + dlg.method.getName()));
                FeatureLayer l = new FeatureLayer(dlg.layer.getName() + "_Batch" + dlg.method.getDetailName() + "#", batchMethod.getResults(),
                        new FeatureStyle("Dim", ColorRamp.RAMP_TEMP, 0, 2));
                l.setRemovable(true);
                ((DefaultGroupLayer)mapViewer.getLayers()).addLayerFirst(l);
            }
        }).start();
    }//GEN-LAST:event_batchVectorMenuItemActionPerformed

    private void radialRasterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radialRasterMenuItemActionPerformed
        final RadialRasterDialog dlg = new RadialRasterDialog(this, new LayerModel(mapViewer.getLayers(), BinRasterLayer.class), mapViewer);
        dlg.setVisible(true);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                // on attend que la boite de dialogue soit fermée
                while(dlg.isDisplayable()) { 
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(!dlg.isOk) {   
                    return;
                }
                RadialRasterMethod radMethod = new RadialRasterMethod(dlg.layer.getName(), dlg.sampling, dlg.layer.getImageShape().getImage(), 
                        JTS.rectToEnv(dlg.layer.getBounds()), dlg.centre);
                launchMethod(radMethod);
            }
        }).start();
    }//GEN-LAST:event_radialRasterMenuItemActionPerformed

    private void multiRadialRasterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiRadialRasterMenuItemActionPerformed
        final MultiRadialRasterDialog dlg = new MultiRadialRasterDialog(this, new LayerModel(mapViewer.getLayers(), BinRasterLayer.class));
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProgressBar monitor = Config.getProgressBar("Multi radial...");
                MultiRadialRaster radMethod = new MultiRadialRaster(dlg.layer.getImageShape().getImage(), 
                        dlg.layer.getBounds(), dlg.maxSize, dlg.autoThreshold, dlg.minThreshold, dlg.indModel, dlg.confidenceInterval);
                radMethod.execute(monitor);
                
                RasterStyle dimStyle = new RasterStyle(ColorRamp.RAMP_TEMP, 0, 2);
                DefaultGroupLayer gl = new DefaultGroupLayer(dlg.layer.getName() + "_MultiRadial_maxsize" + dlg.maxSize, true);
                gl.setRemovable(true);
                RasterLayer l = new RasterLayer("Dimension#", 
                        new RasterShape(radMethod.getRasterDim(), dlg.layer.getBounds(), dimStyle, true), dlg.layer.getCRS());
                gl.addLayerLast(l);
                RasterStyle r2Style = new RasterStyle(ColorRamp.RAMP_GREEN, 0, 1);
                l = new RasterLayer("R2#", 
                        new RasterShape(radMethod.getRasterR2(), dlg.layer.getBounds(), r2Style, true), dlg.layer.getCRS());
                l.setVisible(false);
                gl.addLayerLast(l);
                RasterStyle aStyle = new RasterStyle(ColorRamp.RAMP_RED);
                l = new RasterLayer("a#", 
                        new RasterShape(radMethod.getRasterA(), dlg.layer.getBounds(), aStyle, true), dlg.layer.getCRS());
                l.setVisible(false);
                gl.addLayerLast(l);
                if(dlg.autoThreshold) {
                    RasterStyle distStyle = new RasterStyle(ColorRamp.reverse(ColorRamp.RAMP_BROWN));
                    l = new RasterLayer("DistMax#", 
                            new RasterShape(radMethod.getRasterDistMax(), dlg.layer.getBounds(), distStyle, true), dlg.layer.getCRS());
                    l.setVisible(false);
                    gl.addLayerLast(l);
                }
                if(dlg.confidenceInterval) {
                    RasterStyle interStyle = new RasterStyle(ColorRamp.RAMP_RED);
                    l = new RasterLayer("Confidence interval#", 
                            new RasterShape(radMethod.getRasterDinter(), dlg.layer.getBounds(), interStyle, true), dlg.layer.getCRS());
                    l.setVisible(false);
                    gl.addLayerLast(l);
                    l = new RasterLayer("Confidence interval min#", 
                            new RasterShape(radMethod.getRasterDmin(), dlg.layer.getBounds(), dimStyle, true), dlg.layer.getCRS());
                    l.setVisible(false);
                    gl.addLayerLast(l);
                    l = new RasterLayer("Confidence interval max#", 
                            new RasterShape(radMethod.getRasterDmax(), dlg.layer.getBounds(), dimStyle, true), dlg.layer.getCRS());
                    l.setVisible(false);
                    gl.addLayerLast(l);
                }
                
                ((DefaultGroupLayer)mapViewer.getLayers()).addLayerFirst(gl);
                monitor.close();
            }
        }).start();
    }//GEN-LAST:event_multiRadialRasterMenuItemActionPerformed

    private void selVectorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selVectorMenuItemActionPerformed
        VectorSelectionDialog dlg = new VectorSelectionDialog(this, mapViewer);
        dlg.setVisible(true);
    }//GEN-LAST:event_selVectorMenuItemActionPerformed

    private void selRasterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selRasterMenuItemActionPerformed
        RasterSelectionDialog dlg = new RasterSelectionDialog(this, mapViewer);
        dlg.setVisible(true);
    }//GEN-LAST:event_selRasterMenuItemActionPerformed

    private void multiFracBoxRasterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiFracBoxRasterMenuItemActionPerformed
        final RasterMethodDialog dlg = new RasterMethodDialog(this, "Multi-fractal box", 
                new LayerModel(mapViewer.getLayers(), RasterLayer.class), new MultiFracSampling());
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {       
                MultiFracBoxCountingRasterMethod method = new MultiFracBoxCountingRasterMethod(dlg.layer.getName(), 
                        new RasterBoxSampling(dlg.sampling), 
                        dlg.layer.getImageShape().getImage(), JTS.rectToEnv(dlg.layer.getBounds()));
                launchMethod(method);
            }
        }).start();
    }//GEN-LAST:event_multiFracBoxRasterMenuItemActionPerformed

    private void multiFracVectorMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiFracVectorMenuItem1ActionPerformed
        final BoxCountingDialog dlg = new BoxCountingDialog(this, new LayerModel(mapViewer.getLayers(), FeatureLayer.class),
            new MultiFracSampling());
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                MultiFracBoxCountingVectorMethod method = new MultiFracBoxCountingVectorMethod(dlg.layer.getName(),  
                        dlg.sampling, new DefaultFeatureCoverage(dlg.layer.getFeatures()));
                launchMethod(method);
            }
        }).start();
    }//GEN-LAST:event_multiFracVectorMenuItem1ActionPerformed

    private void waveletMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_waveletMenuItemActionPerformed
        final RasterMethodDialog dlg = new RasterMethodDialog(this, "Multi-fractal wavelet",
                new LayerModel<>(mapViewer.getLayers(), RasterLayer.class), new DefaultSampling());
        dlg.setVisible(true);
        if(!dlg.isOk) {
            return;
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {       
                MultiFracWaveletMethod method = new MultiFracWaveletMethod(dlg.layer.getName(), 
                        dlg.sampling, 
                        dlg.layer.getImageShape().getImage(), JTS.rectToEnv(dlg.layer.getBounds()));
                launchMethod(method);
            }
        }).start();
    }//GEN-LAST:event_waveletMenuItemActionPerformed

    private boolean isBinary(RenderedImage img) {
        RandomIter iter = RandomIterFactory.create(img, null);
        
        for(int i = 0; i < img.getHeight(); i++) {
            for(int j = 0; j < img.getWidth(); j++) {
                if(iter.getSampleDouble(j, i, 0) != 0 && iter.getSampleDouble(j, i, 0) != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private EstimationFrame launchMethod(MonoMethod method) {
        ProgressBar monitor = Config.getProgressBar(method.getName() + "...");
        method.execute(monitor, true);
        ((DefaultGroupLayer)mapViewer.getLayers()).addLayerFirst(method.getGroupLayer());
        monitor.setIndeterminate(true);
        monitor.setNote("Estimation...");
        EstimationFrame frm = new EstimationFrame(MainFrame.this, new EstimationFactory(method));
        frm.setVisible(true);
        monitor.close();
        return frm;
    }
    
    private void launchMethod(MultiFracMethod method) {
        ProgressBar monitor = Config.getProgressBar(method.getName() + "...");
        method.execute(monitor, true);
        ((DefaultGroupLayer)mapViewer.getLayers()).addLayerFirst(method.getGroupLayer());
        monitor.setIndeterminate(true);
        monitor.setNote("Estimation...");
        MultiFracEstimationFrame frm = new MultiFracEstimationFrame(MainFrame.this, method);
        frm.setVisible(true);
        monitor.close();
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) throws IOException {

        if(args.length > 0 && !args[0].equals(JavaLoader.NOFORK)) {
            CLITools.execute(args);
        } else {
            Config.setNodeClass(MainFrame.class);
            PreferencesDialog.initLanguage();
            JavaLoader.launchGUI(MainFrame.class, args.length == 0, 1024);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem backBoneMenuItem;
    private javax.swing.JMenuItem batchVectorMenuItem;
    private javax.swing.JMenuItem binarizeMenuItem;
    private javax.swing.JMenuItem boxCountingMenuItem;
    private javax.swing.JMenuItem boxCountingRasterMenuItem;
    private javax.swing.JMenuItem correlationMenuItem;
    private javax.swing.JMenuItem desserteMenuItem;
    private javax.swing.JMenuItem dilRasterMenuItem;
    private javax.swing.JMenuItem dilationMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JMenuItem loadRasterMenuItem;
    private javax.swing.JMenuItem loadVectorMenuItem;
    private javax.swing.JMenuItem localNetMenuItem;
    private org.thema.drawshape.ui.MapViewer mapViewer;
    private javax.swing.JMenuItem multiFracBoxRasterMenuItem;
    private javax.swing.JMenuItem multiFracVectorMenuItem1;
    private javax.swing.JMenuItem multiRadialRasterMenuItem;
    private javax.swing.JMenu networkMenu;
    private javax.swing.JMenuItem prefMenuItem;
    private javax.swing.JMenuItem radialMenuItem;
    private javax.swing.JMenuItem radialRasterMenuItem;
    private javax.swing.JMenu rasterMenu;
    private javax.swing.JMenuItem rasterizeMenuItem;
    private javax.swing.JMenuItem selRasterMenuItem;
    private javax.swing.JMenuItem selVectorMenuItem;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu vectorMenu;
    private javax.swing.JMenuItem waveletMenuItem;
    // End of variables declaration//GEN-END:variables

}
