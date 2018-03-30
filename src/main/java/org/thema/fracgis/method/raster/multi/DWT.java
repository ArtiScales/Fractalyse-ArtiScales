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

import jwave.transforms.wavelets.Wavelet;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathUtils;

/**
 *
 * @author Gilles Vuidel
 */
public class DWT {
    
    private double [] haarScaling = new double [] {0.5, 0.5};
    private double [] haarWavelet = new double [] {0.5, -0.5};
    private Wavelet wavelet;

    public DWT() {
    }
    
    public DWT(Wavelet wavelet) {
        this.wavelet = wavelet;
    }
    
    public void FWT(double[] data) {
        if(wavelet != null) {
            System.arraycopy(wavelet.forward(data, data.length), 0, data, 0, data.length);
            return;
        } 
        double[] temp = new double[data.length];

        int h = data.length >> 1;
        for (int i = 0; i < h; i++) {
            int k = (i << 1);
            temp[i] = data[k] * haarScaling[0] + data[k + 1] * haarScaling[1];
            temp[i + h] = data[k] * haarWavelet[0] + data[k + 1] * haarWavelet[1];
        }

        System.arraycopy(temp, 0, data, 0, data.length);
    }
    
    public void IWT(double[] data) {
        if(wavelet != null) {
            System.arraycopy(wavelet.reverse(data, data.length), 0, data, 0, data.length);
            return;
        } 
        
        double[] temp = new double[data.length];

        int h = data.length >> 1;
        for (int i = 0; i < h; i++) {
            int k = (i << 1);
            temp[k] = (data[i] * haarScaling[0] + data[i + h] * haarWavelet[0]) / haarWavelet[0];
            temp[k + 1] = (data[i] * haarScaling[1] + data[i + h] * haarWavelet[1]) / haarScaling[0];
        }

        System.arraycopy(temp, 0, data, 0, data.length);
    }
    
    public void FWT(double[][] data, int iterations) {
        for (int k = 0; k < iterations; k++) {
            FWT1(data, 0);
        }
    }
    
    public void FWT1(double[][] data, int k) {
        int rows = data.length;
        int cols = data[0].length;

        double[] row;
        double[] col;

        int lev = 1 << k;

        int levCols = cols / lev;
        int levRows = rows / lev;

        row = new double[levCols];
        for (int i = 0; i < levRows; i++) {
            System.arraycopy(data[i], 0, row, 0, row.length);
            FWT(row);
            System.arraycopy(row, 0, data[i], 0, row.length);
        }

        col = new double[levRows];
        for (int j = 0; j < levCols; j++) {
            for (int i = 0; i < col.length; i++) {
                col[i] = data[i][j];
            }
            FWT(col);
            for (int i = 0; i < col.length; i++) {
                data[i][j] = col[i];
            }
        }
    }
    
    public void IWT(double[][] data, int iterations) {
        for (int k = iterations - 1; k >= 0; k--) {
            IWT1(data, k);
        }
    }
    public void IWT1(double[][] data, int k) {
        int rows = data.length;
        int cols = data[0].length;

        double[] col;
        double[] row;

        int lev = 1 << k;

        int levCols = cols / lev;
        int levRows = rows / lev;

        col = new double[levRows];
        for (int j = 0; j < levCols; j++) {
            for (int i = 0; i < col.length; i++) {
                col[i] = data[i][j];
            }
            IWT(col);
            for (int i = 0; i < col.length; i++) {
                data[i][j] = col[i];
            }
        }

        row = new double[levCols];
        for (int i = 0; i < levRows; i++) {
            System.arraycopy(data[i], 0, row, 0, row.length);
            IWT(row);
            System.arraycopy(row, 0, data[i], 0, row.length);
        }

    }
}
