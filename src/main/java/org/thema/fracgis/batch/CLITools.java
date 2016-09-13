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
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.thema.data.GlobalDataStore;
import org.thema.common.JTS;
import org.thema.common.parallel.ParallelFExecutor;
import org.thema.data.IOImage;
import org.thema.common.swing.TaskMonitor;
import org.thema.data.feature.DefaultFeature;
import org.thema.data.feature.DefaultFeatureCoverage;
import org.thema.fracgis.tools.BinarizeDialog;
import org.thema.fracgis.estimation.Estimation;
import org.thema.fracgis.estimation.EstimationFactory;
import org.thema.fracgis.estimation.EstimationFactory.Type;
import org.thema.fracgis.sampling.DefaultSampling;
import org.thema.fracgis.sampling.RasterBoxSampling;
import org.thema.fracgis.sampling.Sampling.Sequence;
import org.thema.parallel.ParallelExecutor;
import org.thema.process.Rasterizer;


/**
 * Command line interface.
 * 
 * @author Gilles Vuidel
 */
public class CLITools {

    /**
     * Main entry for CLI execution.
     * This method is called from main.
     * @param argTab arguments from the command line
     * @throws IOException 
     */
    public static void execute(String [] argTab) throws IOException {
        List<String> args = new ArrayList<>(Arrays.asList(argTab));
        String arg0 = args.remove(0);
        if(arg0.equals("--help")) {
            System.out.println("Usage :\njava -jar fracgis.jar [-mpi | -proc n]\n" 
                    + "[--rasterize [-neg] res=val file_1.shp [... file_n.shp]]\n"
                    + "[--binarize min=val max=val file_1.tif [... file_n.tif]]\n"
                    + "[--boxcounting coef=val [min=val] [max=val] [seq=arith|geom] [gliding=val] [estim=log|direct] file_1.shp [... file_n.shp]]\n"
                    + "[--rboxcounting coef=val [max=val] [seq=arith|geom] [estim=log|direct] file_1.tif [... file_n.tif]]\n"
                    + "[--dilation coef=val min=val [max=val] [seq=arith|geom] [estim=log|direct] file_1.shp [... file_n.shp]]\n"
                    + "[--rdilation coef=val [max=val] [seq=arith|geom] [estim=log|direct] file_1.tif [... file_n.tif]]\n"
                    + "[--correlation coef=val [max=val] [seq=arith|geom] [estim=log|direct] file_1.tif [... file_n.tif]]");
            return;
        } 
        TaskMonitor.setHeadlessStream(new PrintStream(File.createTempFile("java", "monitor")));
        
        if(arg0.equals("-proc")) {
            int n = Integer.parseInt(args.remove(0));
            ParallelExecutor.setNbProc(n);
            ParallelFExecutor.setNbProc(n);
            arg0 = args.remove(0);
        }
        
        if(arg0.equals("--rasterize")) {
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
                if(negative) {
                    for(int i = 0; i < raster.getHeight(); i++) {
                        for(int j = 0; j < raster.getWidth(); j++) {
                            raster.setSample(j, i, 0, 1 - raster.getSample(j, i, 0));
                        }
                    }
                }
                GridCoverage2D gridCov = new GridCoverageFactory().create(f.getName(),
                    raster, new Envelope2D(GlobalDataStore.getCRS(f), rasterizer.getEnvelope()));

                new GeoTiffWriter(new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-4) + (negative?"_neg":"") + ".tif")).write(gridCov, null);
            }
        } else if(arg0.equals("--binarize")) {
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
        } else if(arg0.equals("--boxcounting")) {
            DefaultSampling samples = getSampling(args);
            int d = 1;
            if(args.get(0).startsWith("gliding=")) {
                d = Integer.parseInt(args.remove(0).split("=")[1]);
            }
            Type typeEstim = getEstim(args);
            String suffix = String.format(Locale.US, "coef%g_min%g_max%g_seq%s_glid%d_estim%s", 
                    samples.getCoef(), samples.getMinSize(), samples.getMaxSize(), samples.getSeq().toString().toLowerCase(),
                    d, typeEstim.toString().toLowerCase());
            try (BufferedWriter wres = new BufferedWriter(new FileWriter(new File("box_" + suffix + ".txt")))) {
                wres.write("File\tCoef\tMin\tMax\tGliding\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
                for(String arg : args) {
                    File f = new File(arg);
                    System.out.println(arg);
                    DefaultFeatureCoverage cov = new DefaultFeatureCoverage(DefaultFeature.loadFeatures(f, true));
                    BoxCountingMethod method = new BoxCountingMethod(f.getName(), samples, cov, d, false);
                    method.execute(new TaskMonitor.EmptyMonitor(), true);
                    Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                    try (FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_box_" + suffix + ".txt"))) {
                        estim.saveToText(w);
                    }
                    double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                    wres.write(f.getName() + "\t" + samples.getCoef() + "\t" + samples.getMinSize() + "\t" + samples.getRealMaxSize() + "\t" + d + "\t" +
                            estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" +
                            confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
                }
            }
        } else if(arg0.equals("--rboxcounting")) {
            DefaultSampling samples = getSampling(args);
            Type typeEstim = getEstim(args);
            String suffix = String.format(Locale.US, "coef%g_estim%s", samples.getCoef(), typeEstim.toString().toLowerCase());
            try (BufferedWriter wres = new BufferedWriter(new FileWriter(new File("rbox_" + suffix + ".txt")))) {
                wres.write("File\tCoef\tMinSize\tMaxSize\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
                for(String arg : args) {
                    File f = new File(arg);
                    System.out.println(arg);
                    GridCoverage2D cov = IOImage.loadTiff(f);
                    RasterBoxSampling boxSamples = new RasterBoxSampling(samples);
                    BoxCountingRasterMethod method = new BoxCountingRasterMethod(f.getName(), boxSamples, cov.getRenderedImage(),
                            JTS.rectToEnv(cov.getEnvelope2D()));
                    method.execute(new TaskMonitor.EmptyMonitor(), true);
                    Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                    try (FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_rbox_" + suffix + ".txt"))) {
                        estim.saveToText(w);
                    }
                    double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                    wres.write(f.getName() + "\t" + samples.getCoef() + "\t" + samples.getMinSize() + "\t" + samples.getRealMaxSize() + "\t" +
                            estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" +
                            confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
                }
            }
        } else if(arg0.equals("--dilation")) {
            DefaultSampling samples = getSampling(args);
            Type typeEstim = getEstim(args);
            String suffix = String.format(Locale.US, "coef%g_min%g_max%g_estim%s", samples.getCoef(), samples.getMinSize(), samples.getMaxSize(), typeEstim.toString().toLowerCase());
            try (BufferedWriter wres = new BufferedWriter(new FileWriter(new File("dil_" + suffix + ".txt")))) {
                wres.write("File\tCoef\tMin\tMax\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
                for(String arg : args) {
                    File f = new File(arg);
                    System.out.println(arg);
                    DefaultFeatureCoverage cov = new DefaultFeatureCoverage(DefaultFeature.loadFeatures(f, true));
                    DilationMethod method = new DilationMethod(f.getName(), samples, cov, false, false);
                    method.execute(new TaskMonitor.EmptyMonitor(), true);
                    Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                    try (FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_dil_" + suffix + ".txt"))) {
                        estim.saveToText(w);
                    }
                    double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                    wres.write(f.getName() + "\t" + samples.getCoef() + "\t" + samples.getMinSize() + "\t" + samples.getRealMaxSize() + "\t" +
                            estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" +
                            confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
                }
            }
        } else if(arg0.equals("--correlation")) {
            DefaultSampling samples = getSampling(args);
            Type typeEstim = getEstim(args);
            String suffix = String.format(Locale.US, "max%g_estim%s", samples.getMaxSize(), typeEstim.toString().toLowerCase());
            try (BufferedWriter wres = new BufferedWriter(new FileWriter(new File("corr_" + suffix + ".txt")))) {
                wres.write("File\tmax\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
                for(String arg : args) {
                    File f = new File(arg);
                    System.out.println(arg);
                    GridCoverage2D cov = IOImage.loadTiff(f);
                    CorrelationMethod method = new CorrelationMethod(f.getName(), samples, cov.getRenderedImage(),
                            JTS.rectToEnv(cov.getEnvelope2D()));
                    method.execute(new TaskMonitor.EmptyMonitor(), true);
                    Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                    try (FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_corr_" + suffix + ".txt"))) {
                        estim.saveToText(w);
                    }
                    double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                    wres.write(f.getName() + "\t" + samples.getRealMaxSize() + "\t" +
                            estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" +
                            confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
                }
            }
        } else if(arg0.equals("--rdilation")) {
            DefaultSampling samples = getSampling(args);
            Type typeEstim = getEstim(args);
            String suffix = String.format(Locale.US, "coef%g_max%g_estim%s", samples.getCoef(), samples.getMaxSize(), typeEstim.toString().toLowerCase());
            try (BufferedWriter wres = new BufferedWriter(new FileWriter(new File("rdil_" + suffix + ".txt")))) {
                wres.write("File\tcoef\tmaxsize\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
                for(String arg : args) {
                    File f = new File(arg);
                    System.out.println(arg);
                    GridCoverage2D cov = IOImage.loadTiff(f);
                    DilationRasterMethod method = new DilationRasterMethod(f.getName(), samples, cov.getRenderedImage(),
                            JTS.rectToEnv(cov.getEnvelope2D()));
                    method.execute(new TaskMonitor.EmptyMonitor(), true);
                    Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                    try (FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+"_rdil_" + suffix + ".txt"))) {
                        estim.saveToText(w);
                    }
                    double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                    wres.write(f.getName() + "\t" + samples.getCoef() + "\t" + samples.getRealMaxSize() + "\t" +
                            estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" +
                            confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
                }
            }
        } else {
            System.err.println("Unknown command " + arg0 + "\nTry --help" );
        }
    }
    
    
    private static DefaultSampling getSampling(List<String> args) {
        double coef = 0;
        if(args.get(0).startsWith("coef=")) {
            coef = Double.parseDouble(args.remove(0).split("=")[1]);
        }
        double min = 0;
        if(args.get(0).startsWith("min=")) {
            min = Double.parseDouble(args.remove(0).split("=")[1]);
        }
        double max = 0;
        if(args.get(0).startsWith("max=")) {
            max = Double.parseDouble(args.remove(0).split("=")[1]);
        }
        Sequence seq = Sequence.GEOM;
        if(args.get(0).equals("seq=arith")) {
            seq = Sequence.ARITH;
        }
        
        return new DefaultSampling(min, max, coef, seq);
    }
    
    private static Type getEstim(List<String> args) {
        Type typeEstim = Type.LOG;
        if(args.get(0).startsWith("estim=")) {
            if(args.remove(0).equals("estim=direct")) {
                typeEstim = Type.DIRECT;
            }
        }
        return typeEstim;
    }
}
