/*
 * Copyright (C) 2016 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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
package org.thema.fracgis.method.raster.multi;

import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 *
 * @author Gilles Vuidel
 */
public class CWT {
    
    public static final int NVAR = 3;
    
    public WritableRaster conv, dx, dy, dxy, dxx, dyy, mod, arg, kapa, chain, goodchain;
    
    public void calcCWT(Raster img, int var) {
        int d = NVAR*var;
        WritableRaster r = img.createCompatibleWritableRaster(img.getWidth()+2*d, img.getHeight()+2*d);
//                Raster.createWritableRaster(new BandedSampleModel(
//                DataBuffer.TYPE_FLOAT, img.getWidth()+2*d, img.getHeight()+2*d, 1), null);
        for(int y = 0; y < img.getHeight(); y++) {
            for(int x = 0; x < img.getWidth(); x++) {
                r.setSample(x+d, y+d, 0, img.getSampleDouble(x, y, 0));
            }
        }
        
        conv = convolve2d(r, gauss(var));
        dx = convolve2d(r, dog1x(var));
        dy = convolve2d(r, dog1y(var));
        dxx = convolve2d(r, dog2x(var));
        dyy = convolve2d(r, dog2y(var));
        dxy = convolve2d(r, dog2xy(var));
        
        mod = dx.createCompatibleWritableRaster();
        for(int y = 0; y < r.getHeight(); y++) {
            for(int x = 0; x < r.getWidth(); x++) {
                double m = Math.hypot(dx.getSampleDouble(x, y, 0), dy.getSampleDouble(x, y, 0));
                mod.setSample(x, y, 0, m);
            }
        }
        
        arg = dx.createCompatibleWritableRaster();
        for(int y = 0; y < r.getHeight(); y++) {
            for(int x = 0; x < r.getWidth(); x++) {
                double a = Math.atan2(-dy.getSampleDouble(x, y, 0), dx.getSampleDouble(x, y, 0));
                arg.setSample(x, y, 0, a);
            }
        }
        
        kapa = dx.createCompatibleWritableRaster();
        for(int y = 0; y < r.getHeight(); y++) {
            for(int x = 0; x < r.getWidth(); x++) {
                double k = 2*dx.getSampleDouble(x, y, 0)*dx.getSampleDouble(x, y, 0)*dxx.getSampleDouble(x, y, 0)
                         + 2*dy.getSampleDouble(x, y, 0)*dy.getSampleDouble(x, y, 0)*dyy.getSampleDouble(x, y, 0)
                         + 4*dx.getSampleDouble(x, y, 0)*dy.getSampleDouble(x, y, 0)*dxy.getSampleDouble(x, y, 0);
                kapa.setSample(x, y, 0, k);
            }
        }
        
        double max = 0;
        int xmax = -1;
        int ymax = -1;
        chain = dx.createCompatibleWritableRaster();
        for(int y = 1; y < r.getHeight()-1; y++) {
            for(int x = 1; x < r.getWidth()-1; x++) {
                double k = kapa.getSampleDouble(x, y, 0);
                double kx1 = kapa.getSampleDouble(x-1, y, 0);
                double ky1 = kapa.getSampleDouble(x, y-1, 0);
                double kx2 = kapa.getSampleDouble(x+1, y, 0);
                double ky2 = kapa.getSampleDouble(x, y+1, 0);
                if(k*kx1 <= 0 || k*ky1 <= 0 || k*kx2 <= 0 || k*ky2 <= 0) {
                    double m = mod.getSampleDouble(x, y, 0);
                    double m4 = mod.getSampleDouble(x-1, y, 0) + mod.getSampleDouble(x, y-1, 0)
                            + mod.getSampleDouble(x+1, y, 0) + mod.getSampleDouble(x, y+1, 0);
                    if(m4 < 4*m) {
                        chain.setSample(x, y, 0, m);
                        if(m > max) {
                            max = m;
                            xmax = x;
                            ymax = y;
                        }
                    }
                }
            }
        }
        
        goodchain = dx.createCompatibleWritableRaster();
        int x = xmax;
        int y = ymax;
        while(true) {
            if(goodchain.getSampleDouble(x, y, 0) != 0) {
                break;
            }
            double m = mod.getSampleDouble(x, y, 0);
            if(m == 0) {
                System.err.println("err");
                //throw new RuntimeException();
            }
            
            goodchain.setSample(x, y, 0, var/img.getWidth());
            
            double a = arg.getSampleDouble(x, y, 0);
            
            if(a >= 0 && a < Math.PI/4) {
                double m1 = chain.getSampleDouble(x, y-1, 0);
                double m2 = chain.getSampleDouble(x-1, y-1, 0);
                y--;
                if(m1 < m2) {
                    x--;
                }
            } else if(a >= Math.PI/4 && a < Math.PI/2) {
                double m1 = chain.getSampleDouble(x-1, y-1, 0);
                double m2 = chain.getSampleDouble(x-1, y, 0);
                x--;
                if(m1 > m2) {
                    y--;
                }
            } else if(a >= Math.PI/2 && a < 3*Math.PI/4) {
                double m1 = chain.getSampleDouble(x-1, y+1, 0);
                double m2 = chain.getSampleDouble(x-1, y, 0);
                x--;
                if(m1 > m2) {
                    y++;
                }
            } else if(a >= 3*Math.PI/4 && a <= Math.PI) {
                double m1 = chain.getSampleDouble(x-1, y+1, 0);
                double m2 = chain.getSampleDouble(x, y+1, 0);
                y++;
                if(m1 > m2) {
                    x--;
                }
            } else if(a >= -Math.PI && a < -3*Math.PI/4) {
                double m1 = chain.getSampleDouble(x+1, y+1, 0);
                double m2 = chain.getSampleDouble(x, y+1, 0);
                y++;
                if(m1 > m2) {
                    x++;
                }
            } else if(a >= -3*Math.PI/4 && a < -Math.PI/2) {
                double m1 = chain.getSampleDouble(x+1, y+1, 0);
                double m2 = chain.getSampleDouble(x+1, y, 0);
                x++;
                if(m1 > m2) {
                    y++;
                }
            } else if(a >= -Math.PI/2 && a < -Math.PI/4) {
                double m1 = chain.getSampleDouble(x+1, y-1, 0);
                double m2 = chain.getSampleDouble(x+1, y, 0);
                x++;
                if(m1 > m2) {
                    y--;
                }
            } else if(a >= -Math.PI/4 && a < 0) {
                double m1 = chain.getSampleDouble(x+1, y-1, 0);
                double m2 = chain.getSampleDouble(x, y-1, 0);
                y--;
                if(m1 > m2) {
                    x++;
                }
            } else {
                throw new RuntimeException();
            }
//            if(chain.getSampleDouble(x, y, 0) == 0) {
//                System.err.println("err");
//                //throw new RuntimeException();
//            }
        }
    }
    
    private float[] gauss(int var) {
        int size = 2*NVAR*var + 1;
        float[] kernel = new float[size*size];
        double facteur = 1 / (var * Math.sqrt(2 * Math.PI));
        double var2 = 2*Math.pow(var, 2);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                int x = j - NVAR*var;
                int y = i - NVAR*var;
		kernel[i*size+j] = (float) (facteur * Math.exp(-(Math.pow(x, 2)+Math.pow(y, 2)) / var2));
            }
        }
        return kernel;
    }
    
    private float[] dog1x(int var) {
        int size = 2*NVAR*var + 1;
        float[] kernel = new float[size*size];
        double facteur = -1 / (Math.pow(var, 3) * Math.sqrt(2 * Math.PI));
        double var2 = 2*Math.pow(var, 2);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                int x = j - NVAR*var;
                int y = i - NVAR*var;
		kernel[i*size+j] = (float) (facteur * x * Math.exp(-(Math.pow(x, 2)+Math.pow(y, 2)) / var2));
            }
        }
        return kernel;
    }
    
    private float[] dog1y(int var) {
        int size = 2*NVAR*var + 1;
        float[] kernel = new float[size*size];
        double facteur = -1 / (Math.pow(var, 3) * Math.sqrt(2 * Math.PI));
        double var2 = 2*Math.pow(var, 2);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                int x = j - NVAR*var;
                int y = i - NVAR*var;
		kernel[i*size+j] = (float) (facteur * y * Math.exp(-(Math.pow(x, 2)+Math.pow(y, 2)) / var2));
            }
        }
        return kernel;
    }
    
    private float[] dog2x(int var) {
        int size = 2*NVAR*var + 1;
        float[] kernel = new float[size*size];
        double facteur = 1 / (Math.pow(var, 5) * Math.sqrt(2 * Math.PI));
        double var2 = Math.pow(var, 2);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                int x = j - NVAR*var;
                int y = i - NVAR*var;
		kernel[i*size+j] = (float) (facteur * (x*x - var2) * Math.exp(-(Math.pow(x, 2)+Math.pow(y, 2)) / (2*var2)));
            }
        }
        return kernel;
    }
    
    private float[] dog2y(int var) {
        int size = 2*NVAR*var + 1;
        float[] kernel = new float[size*size];
        double facteur = 1 / (Math.pow(var, 5) * Math.sqrt(2 * Math.PI));
        double var2 = Math.pow(var, 2);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                int x = j - NVAR*var;
                int y = i - NVAR*var;
		kernel[i*size+j] = (float) (facteur * (y*y - var2) * Math.exp(-(Math.pow(x, 2)+Math.pow(y, 2)) / (2*var2)));
            }
        }
        return kernel;
    }
    
    private float[] dog2xy(int var) {
        int size = 2*NVAR*var + 1;
        float[] kernel = new float[size*size];
        double facteur = 1 / (Math.pow(var, 5) * Math.sqrt(2 * Math.PI));
        double var2 = Math.pow(var, 2);
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                int x = j - NVAR*var;
                int y = i - NVAR*var;
		kernel[i*size+j] = (float) (facteur * x*y * Math.exp(-(Math.pow(x, 2)+Math.pow(y, 2)) / (2*var2)));
            }
        }
        return kernel;
    }
    
//    public static WritableRaster convolve2d(Raster r, float[] kernel) {
//        int size = (int) Math.sqrt(kernel.length);
//        Kernel kern = new Kernel(size, size, kernel);
//        ConvolveOp op = new ConvolveOp(kern);
//        WritableRaster dst = op.createCompatibleDestRaster(r);
//        op.filter(r, dst);
//
//        int d = size / 2;
//        return dst.createWritableChild(d, d, r.getWidth()-2*d, r.getHeight()-2*d, 0, 0, null);
//    }
    
    public static WritableRaster convolve2d(Raster r, float[] kernel) {
        int size = (int) Math.sqrt(kernel.length);
        int d = size / 2;
        WritableRaster dst = Raster.createWritableRaster(new BandedSampleModel(
                DataBuffer.TYPE_DOUBLE, r.getWidth(), r.getHeight(), 1), null);
        for(int y = 0; y < r.getHeight(); y++) {
            for(int x = 0; x < r.getWidth(); x++) {
                int i1 = -Math.min(0, y-d);
                int j1 = -Math.min(0, x-d);
                int i2 = d+Math.min(d, r.getHeight()-y);
                int j2 = d+Math.min(d, r.getWidth()-x);
                double sum = 0;
                for(int i = i1; i < i2; i++) {
                    for(int j = j1; j < j2; j++) {
                        sum += r.getSampleDouble(x+j-d, y+i-d, 0) * kernel[i*size+j];
                    }
                }
                dst.setSample(x, y, 0, sum);
            }
        }
        
        return dst;
    }
}
