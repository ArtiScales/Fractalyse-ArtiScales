/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis.batch;

import org.thema.fracgis.method.vector.mono.DilationMethod;
import org.thema.fracgis.method.vector.mono.BoxCountingMethod;
import org.thema.fracgis.method.raster.mono.DilationRasterMethod;
import org.thema.fracgis.method.raster.mono.BoxCountingRasterMethod;
import org.thema.fracgis.method.raster.mono.CorrelationMethod;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.thema.GlobalDataStore;
import org.thema.common.JTS;
import org.thema.common.io.IOImage;
import org.thema.common.parallel.TaskMonitor;
import org.thema.drawshape.feature.DefaultFeature;
import org.thema.drawshape.feature.DefaultFeatureCoverage;
import org.thema.fracgis.tools.BinarizeDialog;
import org.thema.fracgis.estimation.Estimation;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.estimation.EstimationFactory.Type;
import org.thema.fracgis.method.*;
import org.thema.process.Rasterizer;


/**
 *
 * @author gib
 */
public class CLITools {

    public void execute(String [] argTab) throws Throwable {
        List<String> args = new ArrayList<String>(Arrays.asList(argTab));
        String arg0 = args.remove(0);
        if(arg0.equals("--help")) {
            System.err.println("Usage :\n"
                    + "[--rasterize [-neg] res=val file_1.shp [... file_n.shp]]\n"
                    + "[--binarize min=val max=val file_1.tif [... file_n.tif]]\n"
                    + "[--boxcounting coef=val [min=val] [max=val] [gliding=val] [estim=log|direct] file_1.shp [... file_n.shp]]\n"
                    + "[--rboxcounting coef=val [max=val] [estim=log|direct] file_1.tif [... file_n.tif]]\n"
                    + "[--dilation coef=val min=val [max=val] [estim=log|direct] file_1.shp [... file_n.shp]]\n"
                    + "[--rdilation nstep=val [estim=log|direct] file_1.tif [... file_n.tif]]\n"
                    + "[--correlation [max=val] [estim=log|direct] file_1.tif [... file_n.tif]]");
        }
        else if(arg0.equals("--rasterize")) {
            boolean negative = false;
            if(args.get(0).equals("-neg")) {
                negative = true;
                args.remove(0);
            }
            double res = Double.parseDouble(args.remove(0).split("=")[1]);
            for(String arg : args) {
                File f = new File(arg);
                System.out.println(arg);
                GlobalDataStore datastore = GlobalDataStore.createDataStore(f.getParentFile());
                DefaultFeatureCoverage cov = new DefaultFeatureCoverage(datastore.getFeatures(f));
                Rasterizer rasterizer = new Rasterizer(cov, res);
                WritableRaster raster = rasterizer.rasterize(new TaskMonitor.EmptyMonitor());
                if(negative)
                    for(int i = 0; i < raster.getHeight(); i++)
                        for(int j = 0; j < raster.getWidth(); j++)
                            raster.setSample(j, i, 0, 1 - raster.getSample(j, i, 0));
                
                GridCoverage2D gridCov = new GridCoverageFactory().create(f.getName(),
                    raster, new Envelope2D(datastore.getCRS(f), rasterizer.getEnvelope()));

                new GeoTiffWriter(new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-4) + (negative?"_neg":"") + ".tif")).write(gridCov, null);
            }
        }
        else if(arg0.equals("--binarize")) {
            double min = Double.parseDouble(args.remove(0).split("=")[1]);
            double max = Double.parseDouble(args.remove(0).split("=")[1]);
            for(String arg : args) {
                File f = new File(arg);
                System.out.println(arg);
                GridCoverage2D grid = IOImage.loadTiff(f);
                RenderedImage img = grid.getRenderedImage();
                BufferedImage binImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                WritableRaster res = binImg.getRaster();
                BinarizeDialog.binarize(img, res, min, max);
                GridCoverage2D binGrid = new GridCoverageFactory().create(f.getName(),
                    binImg, grid.getEnvelope2D());

                new GeoTiffWriter(new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-4) + "_bin"+min+"-"+max+".tif")).write(binGrid, null);
            }
        }
        else if(arg0.equals("--boxcounting")) {
            double coef = Double.parseDouble(args.remove(0).split("=")[1]);
            double min = 0;
            if(args.get(0).startsWith("min="))
                min = Double.parseDouble(args.remove(0).split("=")[1]);
            double max = 0;
            if(args.get(0).startsWith("max="))
                max = Double.parseDouble(args.remove(0).split("=")[1]);
            int d = 1;
            if(args.get(0).startsWith("gliding="))
                d = Integer.parseInt(args.remove(0).split("=")[1]);
            Type typeEstim = Type.LOG;
            if(args.get(0).startsWith("estim=")) {
                if(args.remove(0).equals("estim=direct"))
                    typeEstim = Type.DIRECT;
            }
            String suffix = String.format(Locale.US, "coef%g_min%g_max%g_glid%d_estim%s", coef, min, max, d, typeEstim.toString().toLowerCase());
            BufferedWriter wres = new BufferedWriter(new FileWriter(new File("box_" + suffix + ".txt")));
            wres.write("File\tCoef\tMin\tMax\tGliding\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
            for(String arg : args) {
                File f = new File(arg);
                System.out.println(arg);
                BoxCountingMethod method = new BoxCountingMethod(f.getName(), new DefaultFeatureCoverage(DefaultFeature.loadFeatures(f, true)), 
                        min, max, coef, d);
                method.execute(new TaskMonitor.EmptyMonitor(), true);
                FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_box_" + suffix + ".txt"));
                Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                estim.saveToText(w);
                w.close();
                double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                wres.write(f.getName() + "\t" + coef + "\t" + method.getMin() + "\t" + method.getMax() + "\t" + d + "\t" +
                        estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" + 
                        confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
            }
            wres.close();
        }
        else if(arg0.equals("--rboxcounting")) {
            double coef = Double.parseDouble(args.remove(0).split("=")[1]);
            double max = 0;
            if(args.get(0).startsWith("max="))
                max = Double.parseDouble(args.remove(0).split("=")[1]);
            Type typeEstim = Type.LOG;
            if(args.get(0).startsWith("estim=")) {
                if(args.remove(0).equals("estim=direct"))
                    typeEstim = Type.DIRECT;
            }
            String suffix = String.format(Locale.US, "coef%g_estim%s", coef, typeEstim.toString().toLowerCase());
            BufferedWriter wres = new BufferedWriter(new FileWriter(new File("rbox_" + suffix + ".txt")));
            wres.write("File\tCoef\tMinSize\tMaxSize\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
            for(String arg : args) {
                File f = new File(arg);
                System.out.println(arg);
                GridCoverage2D cov = IOImage.loadTiff(f);
                BoxCountingRasterMethod method = new BoxCountingRasterMethod(f.getName(), cov.getRenderedImage(), 
                        JTS.rectToEnv(cov.getEnvelope2D()), coef, max);
                method.execute(new TaskMonitor.EmptyMonitor(), true);
                FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_rdil_" + suffix + ".txt"));
                Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                estim.saveToText(w);
                w.close();
                double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                wres.write(f.getName() + "\t" + coef + "\t" + method.getMinSize() + "\t" + method.getMaxSize() + "\t" +
                        estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" + 
                        confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
            }
            wres.close();
        } 
        else if(arg0.equals("--dilation")) {
            double coef = Double.parseDouble(args.remove(0).split("=")[1]);
            double min = Double.parseDouble(args.remove(0).split("=")[1]);
            double max = 0;
            if(args.get(0).startsWith("max="))
                max = Double.parseDouble(args.remove(0).split("=")[1]);
            Type typeEstim = Type.LOG;
            if(args.get(0).startsWith("estim=")) {
                if(args.remove(0).equals("estim=direct"))
                    typeEstim = Type.DIRECT;
            }
            String suffix = String.format(Locale.US, "coef%g_min%g_max%g_estim%s", coef, min, max, typeEstim.toString().toLowerCase());
            BufferedWriter wres = new BufferedWriter(new FileWriter(new File("dil_" + suffix + ".txt")));
            wres.write("File\tCoef\tMin\tMax\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
            for(String arg : args) {
                File f = new File(arg);
                System.out.println(arg);
                DilationMethod method = new DilationMethod(f.getName(), new DefaultFeatureCoverage(DefaultFeature.loadFeatures(f, true)), 
                        min, max, coef);
                method.execute(new TaskMonitor.EmptyMonitor(), true);
                FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_dil_" + suffix + ".txt"));
                Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                estim.saveToText(w);
                w.close();
                double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                wres.write(f.getName() + "\t" + coef + "\t" + min + "\t" + method.getMax() + "\t" +
                        estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" + 
                        confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
            }
            wres.close();
        }
        else if(arg0.equals("--correlation")) {
            double max = 0;
            if(args.get(0).startsWith("max="))
                max = Double.parseDouble(args.remove(0).split("=")[1]);
            Type typeEstim = Type.DIRECT;
            if(args.get(0).startsWith("estim=")) {
                if(args.remove(0).equals("estim=log"))
                    typeEstim = Type.LOG;
            }
            String suffix = String.format(Locale.US, "max%g_estim%s", max, typeEstim.toString().toLowerCase());
            BufferedWriter wres = new BufferedWriter(new FileWriter(new File("corr_" + suffix + ".txt")));
            wres.write("File\tmax\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
            for(String arg : args) {
                File f = new File(arg);
                System.out.println(arg);
                GridCoverage2D cov = IOImage.loadTiff(f);
                CorrelationMethod method = new CorrelationMethod(f.getName(), cov.getRenderedImage(),
                        JTS.rectToEnv(cov.getEnvelope2D()), max);
                method.execute(new TaskMonitor.EmptyMonitor(), true);
                FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_corr_" + suffix + ".txt"));
                Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                estim.saveToText(w);
                w.close();
                double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                wres.write(f.getName() + "\t" + method.getMaxSize() + "\t" +
                        estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" + 
                        confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
            }
            wres.close();
        } 
        else if(arg0.equals("--rdilation")) {
            int nStep = Integer.parseInt(args.remove(0).split("=")[1]);
            Type typeEstim = Type.LOG;
            if(args.get(0).startsWith("estim=")) {
                if(args.remove(0).equals("estim=direct"))
                    typeEstim = Type.DIRECT;
            }
            String suffix = String.format(Locale.US, "nstep%d_estim%s", nStep, typeEstim.toString().toLowerCase());
            BufferedWriter wres = new BufferedWriter(new FileWriter(new File("rdil_" + suffix + ".txt")));
            wres.write("File\tnStep\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
            for(String arg : args) {
                File f = new File(arg);
                System.out.println(arg);
                GridCoverage2D cov = IOImage.loadTiff(f);
                DilationRasterMethod method = new DilationRasterMethod(f.getName(), cov.getRenderedImage(), 
                        JTS.rectToEnv(cov.getEnvelope2D()), nStep);
                method.execute(new TaskMonitor.EmptyMonitor(), true);
                FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_rdil_" + suffix + ".txt"));
                Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                estim.saveToText(w);
                w.close();
                double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                wres.write(f.getName() + "\t" + nStep + "\t" +
                        estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" + 
                        confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
            }
            wres.close();
        } else
            System.err.println("Unknown command " + arg0 + "\nTry --help" );
    }
}
