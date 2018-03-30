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
import org.thema.fracgis.method.raster.mono.CorrelationRasterMethod;
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
import org.thema.fracgis.method.MonoMethod;
import org.thema.fracgis.method.raster.mono.MonoRasterMethod;
import org.thema.fracgis.method.vector.mono.CorrelationMethod;
import org.thema.fracgis.method.vector.mono.MonoVectorMethod;
import org.thema.fracgis.sampling.DefaultSampling;
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
            System.out.println("Usage :\njava -jar fracgis.jar [-mpi | -proc n] COMMAND\n"
                    + "COMMAND:" 
                    + "\t--rasterize [neg] res=val file_1.shp [... file_n.shp]\n"
                    + "\t--binarize min=val max=val file_1.tif [... file_n.tif]\n"
                    + "\t--boxcounting [gliding=val] SAMPLING [estim=log|direct] file_1.shp [... file_n.shp]\n"
                    + "\t--rboxcounting SAMPLING [estim=log|direct] file_1.tif [... file_n.tif]\n"
                    + "\t--dilation SAMPLING [estim=log|direct] file_1.shp [... file_n.shp]\n"
                    + "\t--rdilation SAMPLING [estim=log|direct] file_1.tif [... file_n.tif]\n"
                    + "\t--correlation SAMPLING [estim=log|direct] file_1.shp [... file_n.shp]\n"
                    + "\t--rcorrelation SAMPLING [estim=log|direct] file_1.tif [... file_n.tif]\n"
                    + "SAMPLING:\n"
                    + "\t[coef=val] [min=val] [max=val] [seq=arith|geom]");
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
            if(args.get(0).equals("neg")) {
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

                IOImage.saveTiffCoverage(new File(f.getParentFile(), f.getName().substring(0, f.getName().length()-4) + (negative?"_neg":"") + ".tif"), gridCov);
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
            int d = 1;
            if(args.get(0).startsWith("gliding=")) {
                d = Integer.parseInt(args.remove(0).split("=")[1]);
            }
            String name = String.format(Locale.US, "box_glid%d", d);
            executeMonoMethod(new BoxCountingMethod(d), name, args);
        } else if(arg0.equals("--dilation")) {
            executeMonoMethod(new DilationMethod(), "dil", args);
        } else if(arg0.equals("--correlation")) {
            executeMonoMethod(new CorrelationMethod(), "cor", args);
        } else if(arg0.equals("--rboxcounting")) {
            executeMonoMethod(new BoxCountingRasterMethod(), "rbox", args);
        } else if(arg0.equals("--rcorrelation")) {
            executeMonoMethod(new CorrelationRasterMethod(), "rcor", args);
        } else if(arg0.equals("--rdilation")) {
            executeMonoMethod(new DilationRasterMethod(), "rdil", args);
        } else {
            throw new IllegalArgumentException("Unknown command " + arg0 + "\nTry --help" );
        }
    }
    
    private static void executeMonoMethod(MonoMethod method, String name, List<String> args) throws IOException {
        DefaultSampling sampling = getSampling(args);
        Type typeEstim = getEstim(args, sampling);
        String suffix = String.format(Locale.US, "_coef%g_min%g_max%g_seq%s_estim%s", 
                sampling.getCoef(), sampling.getMinSize(), sampling.getMaxSize(), sampling.getSeq().toString().toLowerCase(),
                typeEstim.toString().toLowerCase());
        try (BufferedWriter wres = new BufferedWriter(new FileWriter(new File(name + suffix + ".txt")))) {
            wres.write("File\tCoef\tMin\tMax\tModel\tDim.\tR2\tConfidence Interval min\tmax\tParams\n");
            for(String arg : args) {
                File f = new File(arg);
                System.out.println(arg);
                if(method instanceof MonoVectorMethod) {
                    DefaultFeatureCoverage cov = new DefaultFeatureCoverage(DefaultFeature.loadFeatures(f, true));
                    ((MonoVectorMethod)method).setInputData(f.getName(), cov);
                } else {
                    GridCoverage2D cov = IOImage.loadTiff(f);
                    ((MonoRasterMethod)method).setInputData(f.getName(), cov.getRenderedImage(), JTS.rectToEnv(cov.getEnvelope2D()));
                }
                method.setSampling(new DefaultSampling(sampling));
                method.execute(new TaskMonitor.EmptyMonitor(), true);
                Estimation estim = new EstimationFactory(method).getEstimation(typeEstim);
                try (FileWriter w = new FileWriter(new File(f.getParent(), f.getName().substring(0, f.getName().length()-4)+ name + suffix + ".txt"))) {
                    estim.saveToText(w);
                }
                DefaultSampling finalSamp = method.getSampling();
                double[] confidenceInterval = estim.getBootStrapConfidenceInterval();
                wres.write(f.getName() + "\t" + finalSamp.getCoef() + "\t" + finalSamp.getMinSize() + "\t" + finalSamp.getRealMaxSize() + "\t" +
                        estim.getModel() + "\t" + estim.getDimension() + "\t" + estim.getR2() + "\t" +
                        confidenceInterval[0] + "\t" + confidenceInterval[1] + "\t" + estim.getParamInfo() + "\n");
            }
        }
    }
    
    static DefaultSampling getSampling(List<String> args) {
        double coef = 2;
        double min = 0;
        double max = 0;
        Sequence seq = Sequence.GEOM;
        
        boolean found = true;
        while(!args.isEmpty() && found) {
            if(args.get(0).startsWith("coef=")) {
                coef = Double.parseDouble(args.remove(0).split("=")[1]);
            } else if(args.get(0).startsWith("min=")) {
                min = Double.parseDouble(args.remove(0).split("=")[1]);
            } else if(args.get(0).startsWith("max=")) {
                max = Double.parseDouble(args.remove(0).split("=")[1]);
            } else if(args.get(0).startsWith("seq=")) {
                String arg = args.remove(0);
                if(arg.equals("seq=arith")) {
                    seq = Sequence.ARITH;
                } else if(arg.equals("seq=geom")) {
                    seq = Sequence.GEOM;
                } else {
                    throw new IllegalArgumentException("Unkown sequence : " + arg);
                }
            } else {
                found = false;
            }
        }
        return new DefaultSampling(min, max, coef, seq);
    }
    
    static Type getEstim(List<String> args, DefaultSampling sampling) {
        Type typeEstim = sampling.getDefaultEstimType();
        if(!args.isEmpty() && args.get(0).startsWith("estim=")) {
            String arg = args.remove(0);
            if(arg.equals("estim=direct")) {
                typeEstim = Type.DIRECT;
            } else if(arg.equals("estim=log")) {
                typeEstim = Type.LOG;
            } else {
                throw new IllegalArgumentException("Unknown estimation method : " + arg);
            }
        }
        return typeEstim;
    }
}
