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


package org.thema.fracgis.method.raster.multi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.MathArrays;

/**
 * Wavelet Leader and Bootstrap based MultiFractal analysis.
 * Code is based on Matlab WLBMF toolbox written by Herwig Wendt
 *
 * @author Gilles Vuidel
 */
public class WLBMF {
    
    public static class Struct {
        RealMatrix allx, ally, allxy;
        private int i1, j1, i2, j2;
        int W;

        public Struct(RealMatrix allx, RealMatrix ally, RealMatrix allxy) {
            this.allx = allx;
            this.ally = ally;
            this.allxy = allxy;
            
            i1 = Collections.min(Arrays.asList(getMinRowNaN(allx), getMinRowNaN(ally), getMinRowNaN(allxy)));
            j1 = Collections.min(Arrays.asList(getMinColNaN(allx), getMinColNaN(ally), getMinColNaN(allxy)));
            i2 = Collections.max(Arrays.asList(getMaxRowNaN(allx), getMaxRowNaN(ally), getMaxRowNaN(allxy)));
            j2 = Collections.max(Arrays.asList(getMaxColNaN(allx), getMaxColNaN(ally), getMaxColNaN(allxy)));
            W = 3 * (i2-i1+1) * (j2-j1+1);
            
        }
        
        public RealMatrix getX() {
            return allx.getSubMatrix(i1, i2, j1, j2);
        }
        public RealMatrix getY() {
            return ally.getSubMatrix(i1, i2, j1, j2);
        }
        public RealMatrix getXY() {
            return allxy.getSubMatrix(i1, i2, j1, j2);
        }
        
        public double[] getVectorMax() {
            RealMatrix x = getX();
            RealMatrix y = getY();
            RealMatrix xy = getXY();
            double [] max = new double[x.getRowDimension()*x.getColumnDimension()];
            int ind = 0;
            for(int i = 0; i < x.getRowDimension(); i++) {
                for(int j = 0; j < x.getColumnDimension(); j++) {
                    max[ind++] = Math.max(x.getEntry(i, j), Math.max(y.getEntry(i, j), xy.getEntry(i, j)));
                }
            }
            return max;
        }

        public boolean isEmpty() {
            return i1 == -1 || j1 == -1 || i2 == -1 || j2 == -1;
        }
    }
    
    
    int njtemp1, njtemp2;
    int fp; // index of first good value
    int lp1, lp2; // index of last good value
    
    
    private double [] rlistcoefdaub(int nwt) {
        if(nwt == 3) {
            return new double [] {
                0.332670552950,
                0.806891509311,
                0.459877502118,
                -0.135011020010,
                -0.085441273882,
                0.035226291882
            };
        }
        throw new IllegalArgumentException(nwt + " not managed");
    }
    
    public List<Struct> DxLx2d(double [][] data, int nwt , double gamint) {
        double Norm = 1;
        int symm = 0;
        int n = data.length;

        double [] h = rlistcoefdaub(nwt) ;   // filter
        int nl = h.length;           // length of filter, store to manage edge effect later
        double [] gg1 = new double[h.length];// wavelet filter
        double [] hh1 = new double[h.length];// scaling filter
        for(int i = 0; i < h.length; i++) {
            gg1[i] = -1 * Math.pow(-1, i+1) * h[i];
            hh1[h.length-1-i] = h[i];
        }

        // parameter for the centering of the wavelet
        int x0 = 2; 
        int x0Appro = 2*nwt; 
        
        //--- Predict the max # of octaves available given Nwt, and take the min with
        int nbvoies = (int)Math.floor(Math.log(data.length) / Math.log(2));
//        nbvoies = (int) Math.min(Math.floor(Math.log(n/(nl+3.0)) / Math.log(2)), nbvoies); //   safer, casadestime having problems
        
        double [][] LL = data;
        int sidata1 = data.length;
        int sidata2 = data[0].length;
        
        List<Struct> coefs = new ArrayList<>();
        List<Struct> leaders = new ArrayList<>();
        List<Struct> leaders_sans_voisin = new ArrayList<>();
        
        for(int l = 1; l <= nbvoies; l++) {         // Loop Scales
            njtemp1 = LL.length;
            njtemp2 = LL[0].length;
            //-- border effect
            fp = nl; // index of first good value
            lp1 = njtemp1; // index of last good value
            lp2 = njtemp2; // index of last good value
            
            //-- OH convolution and subsampling
            double [][] OH = conv2(LL, gg1); 
            OH = subSampleH(OH, x0);
            //-- HH convolution and subsampling
            double [][] HH = conv2Trans(OH, gg1);
            HH = subSampleV(HH, x0);
            //-- LH convolution and subsampling
            double [][] LH = conv2Trans(OH, hh1);
            LH = subSampleV(LH, x0Appro);
            OH = null;
            //-- OL convolution and subsampling
            double [][] OL=conv2(LL, hh1);
            OL = subSampleH(OL, x0Appro);
            //-- HL convolution and subsampling
            double [][] HL = conv2Trans(OL, gg1);
            HL = subSampleV(HL, x0);
            //-- LL convolution and subsampling
            LL = conv2Trans(OL, hh1);
            LL = subSampleV(LL, x0Appro);
            OL = null;

            //-- passage Norme L1
            final double coef = Math.pow(2, l/Norm);
            DefaultRealMatrixChangingVisitor normL1 = new DefaultRealMatrixChangingVisitor() {
                @Override
                public double visit(int row, int column, double value) {
                    value = Math.abs(value) / coef;
                    return Double.isInfinite(value) ? Double.NaN : value;
                }
            };
            RealMatrix ALH = MatrixUtils.createRealMatrix(LH);
            ALH.walkInOptimizedOrder(normL1);
            RealMatrix AHL = MatrixUtils.createRealMatrix(HL);
            AHL.walkInOptimizedOrder(normL1);
            RealMatrix AHH = MatrixUtils.createRealMatrix(HH);
            AHH.walkInOptimizedOrder(normL1);
            
            //-- max before fractional integration    
//            double supcoefnointx = max(ALH);
//            double supcoefnointy = max(AHL);
//            double supcoefnointxy = max(AHH);
//            double supcoefnoint = Collections.max(Arrays.asList(supcoefnointx, supcoefnointy, supcoefnointxy));

            //-- fractional integration by gamma
            double gamma = Math.pow(2, gamint*l);
            ALH = ALH.scalarMultiply(gamma);
            AHL = AHL.scalarMultiply(gamma);
            AHH = AHH.scalarMultiply(gamma);

//            double supcoefx = max(ALH);
//            double supcoefy = max(AHL);
//            double supcoefxy = max(AHH);
//            double supcoef = Collections.max(Arrays.asList(supcoefx, supcoefy, supcoefxy));

            coefs.add(new Struct(ALH, AHL, AHH));
            

            //-- get position of coefs
//            lesx=1:2^j:sidata(2);
//            lesy=1:2^j:sidata(1);

//            coef(j).xpos=lesx(jj1:jj2);
//            coef(j).ypos=lesy(ii1:ii2);

            
            if(l == 1) {
                //-- compute and store leaders sans voisin
                leaders_sans_voisin.add(new Struct(ALH, AHL, AHH));
            } else {
                int nc1 = (int) Math.floor(leaders_sans_voisin.get(l-2).allx.getRowDimension() / 2);
                int nc2 = (int) Math.floor(leaders_sans_voisin.get(l-2).allx.getColumnDimension() / 2);
                //-- get max at smaller scales
                leaders_sans_voisin.add(new Struct(
                    max(ALH.getSubMatrix(0, nc1-1, 0, nc2-1), leaders_sans_voisin.get(l-2).allx),
                    max(AHL.getSubMatrix(0, nc1-1, 0, nc2-1), leaders_sans_voisin.get(l-2).ally),
                    max(AHH.getSubMatrix(0, nc1-1, 0, nc2-1), leaders_sans_voisin.get(l-2).allxy)
                ));
            }


            //-- on prend le max sur les 8 voisins i.e. 9 coeffs
            
            int six1 = leaders_sans_voisin.get(l-1).allx.getRowDimension();
            int six2 = leaders_sans_voisin.get(l-1).allx.getColumnDimension();
            leaders.add(new Struct(
                    maxVoisin(leaders_sans_voisin.get(l-1).allx),
                    maxVoisin(leaders_sans_voisin.get(l-1).ally),
                    maxVoisin(leaders_sans_voisin.get(l-1).allxy)
            ));
            
//
//
//            tempx=reshape(leaders(j).value.x,1,[]);
//            tempy=reshape(leaders(j).value.y,1,[]);
//            tempxy=reshape(leaders(j).value.xy,1,[]);
//            leaders(j).vector.x =tempx;
//            leaders(j).vector.y =tempy;
//            leaders(j).vector.xy=tempxy;
//            leaders(j).vector.all =  [tempx tempy tempxy] ;
//            leaders(j).vector.max = max(max(tempx, tempy), tempxy) ;   // maximum of x,y,xy at each point
//
//            leaders(j).gamma=gamint;
//
//
//          //// MINCOEF
//            leaders(j).supcoef=coef(j).supcoef;
//            leaders(j).supcoefnoint=coef(j).supcoefnoint;
//            leaders(j).mincoef=min(leaders(j).vector.max);
//            leaders(j).mincoef_x=min(leaders(j).vector.x);
//            leaders(j).mincoef_y=min(leaders(j).vector.y);
//            leaders(j).mincoef_xy=min(leaders(j).vector.xy);
//            leaders(j).supcoefL=max(leaders(j).vector.max);
//            leaders(j).supcoefL_x=max(leaders(j).vector.x); leaders(j).supcoefL_y=max(leaders(j).vector.y); leaders(j).supcoefL_xy=max(leaders(j).vector.xy);
//            coef(j).mincoef=leaders(j).mincoef; coef(j).mincoef_x=leaders(j).mincoef_x;  coef(j).mincoef_y=leaders(j).mincoef_y;  coef(j).mincoef_xy=leaders(j).mincoef_xy;
//            coef(j).supcoefL=leaders(j).supcoefL; coef(j).supcoefL_x=leaders(j).supcoefL_x;  coef(j).supcoefL_y=leaders(j).supcoefL_y;  coef(j).supcoefL_xy=leaders(j).supcoefL_xy;
//
//
//            nj.L(j) = prod(size(leaders(j).value.x));
//
//            ////
//            coef(j).vector.x=reshape(coef(j).x,1,[]);
//            coef(j).vector.y=reshape(coef(j).y,1,[]);
//            coef(j).vector.xy=reshape(coef(j).xy,1,[]);
//            coef(j).vector.all=[coef(j).vector.x coef(j).vector.y coef(j).vector.xy];

        }

//        //in case of last scale empty
//        if isempty(leaders(end).value.x)
//            coef=coef(1:end-1);
//            leaders=leaders(1:end-1);
//            nj.W=nj.W(1:end-1);
//            nj.L=nj.L(1:end-1);
//        end
        
        for(Iterator<Struct> it = leaders.iterator(); it.hasNext(); ) {
            Struct s = it.next();
            if(s.isEmpty()) {
                it.remove();
            }
        }
        
        return leaders;
    }
    
    private static int getMinRowNaN(RealMatrix m) {
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < m.getColumnDimension(); i++) {
            min = Math.min(min, getMinNaN(m.getColumn(i)));
        }
        return min;
    }
    
    private static int getMaxRowNaN(RealMatrix m) {
        int max = Integer.MIN_VALUE;
        for(int i = 0; i < m.getColumnDimension(); i++) {
            max = Math.max(max, getMaxNaN(m.getColumn(i)));
        }
        return max;
    }
    
    private static int getMinColNaN(RealMatrix m) {
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < m.getRowDimension(); i++) {
            min = Math.min(min, getMinNaN(m.getRow(i)));
        }
        return min;
    }
    
    private static int getMaxColNaN(RealMatrix m) {
        int max = Integer.MIN_VALUE;
        for(int i = 0; i < m.getRowDimension(); i++) {
            max = Math.max(max, getMaxNaN(m.getRow(i)));
        }
        return max;
    }
    
    private static int getMinNaN(double [] v) {
        for(int i = 0; i < v.length; i++) {
            if(!Double.isNaN(v[i])) {
                return i;
            }
        }
        return v.length;
    }
    
    private static int getMaxNaN(double [] v) {
        for(int i = v.length-1; i >= 0; i--) {
            if(!Double.isNaN(v[i])) {
                return i;
            }
        }
        return -1;
    }
    
    private RealMatrix maxVoisin(RealMatrix m) {
        final RealMatrix m2 = MatrixUtils.createRealMatrix(m.getRowDimension()+2, m.getColumnDimension()+2);
        m2.setSubMatrix(m.getData(), 1, 1);
        RealMatrix max = MatrixUtils.createRealMatrix(m.getData());
        max.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                row++;
                column++;
                return Collections.max(Arrays.asList(
                        m2.getEntry(row, column),
                        m2.getEntry(row+1, column),
                        m2.getEntry(row, column+1),
                        m2.getEntry(row+1, column+1),
                        m2.getEntry(row-1, column),
                        m2.getEntry(row, column-1),
                        m2.getEntry(row-1, column-1),
                        m2.getEntry(row+1, column-1),
                        m2.getEntry(row-1, column+1)
                        ));
            }
            
        });
        return max;
    }
    
    private RealMatrix max(RealMatrix l2, final RealMatrix l1) {
        RealMatrix max = MatrixUtils.createRealMatrix(l2.getData());
        max.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                return Collections.max(Arrays.asList(
                        l1.getEntry(row*2, column*2),
                        l1.getEntry(row*2+1, column*2),
                        l1.getEntry(row*2, column*2+1),
                        l1.getEntry(row*2+1, column*2+1),
                        value
                    ));
            }
            
        });
        return max;
    }
    
    private double max(RealMatrix m) {
        double max = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < m.getRowDimension(); i++) {
            for(int j = 0; j < m.getColumnDimension(); j++) {
                double val = m.getEntry(i, j);
                if(!Double.isNaN(val) && val > max) {
                    max = val;
                }
            }
        }
        return max;
    }

    private double[][] subSampleH(double[][] mat, int x0) {
        for(int i = 0; i < mat.length; i++) {
            for(int j = 0; j < mat[0].length; j++) {
                if(Double.isNaN(mat[i][j])) {
                    mat[i][j] = Double.POSITIVE_INFINITY;
                }
                if(j < fp-1 || j >= lp2) {
                    mat[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
        int nCol = (int) Math.ceil(njtemp2 / 2.0);
        double [][] mat2 = new double[mat.length][nCol];
        for(int i = 0; i < mat.length; i++) {
            int ind = 0;
            for(int j = 1; j <= njtemp2; j+=2) {
                int k = j + x0 - 1;
                mat2[i][ind++] = mat[i][k-1];
            }
        }
        return mat2;
    }
    
    private double[][] subSampleV(double[][] mat, int x0) {
        for(int i = 0; i < mat.length; i++) {
            for(int j = 0; j < mat[0].length; j++) {
                if(Double.isNaN(mat[i][j])) {
                    mat[i][j] = Double.POSITIVE_INFINITY;
                }
                if(i < fp-1 || i >= lp1) {
                    mat[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }
        int nRow = (int) Math.ceil(njtemp1 / 2.0);
        double [][] mat2 = new double[nRow][mat[0].length];
        int ind = 0;
        for(int i = 1; i <= njtemp1; i+=2) {
            int k = i + x0 - 1;
            for(int j = 0; j < mat[0].length; j++) {
                mat2[ind][j] = mat[k-1][j];
            }
            ind++;
        }

        return mat2;
    }
    
    public double[][] conv2(double[][] mat, double[] v) {
        double [][] conv = new double[mat.length][];
        for(int i = 0; i < mat.length; i++) {
            conv[i] = MathArrays.convolve(mat[i], v);
        }
        return conv;
    }

    public double[][] conv2Trans(double[][] mat, double[] v) {
        RealMatrix m = MatrixUtils.createRealMatrix(mat);
        RealMatrix conv = MatrixUtils.createRealMatrix(m.getRowDimension() + v.length - 1, m.getColumnDimension());
        for(int i = 0; i < m.getColumnDimension(); i++) {
            conv.setColumn(i, MathArrays.convolve(m.getColumn(i), v));
        }
        return conv.getData();
    }
}
