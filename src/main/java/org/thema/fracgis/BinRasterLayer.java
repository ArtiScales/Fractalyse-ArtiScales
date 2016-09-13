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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.thema.drawshape.image.ImageShape;
import org.thema.drawshape.layer.RasterLayer;
import org.thema.drawshape.style.RasterStyle;
import org.thema.drawshape.style.table.UniqueColorTable;

/**
 * Layer for binary raster.
 * 
 * @author Gilles Vuidel
 */
public class BinRasterLayer extends RasterLayer {

    /**
     * Creates new BinRasterLayer.
     * 
     * @param name name of the layer
     * @param shape the binary image
     * @param crs the coordinate reference system
     */
    public BinRasterLayer(String name, ImageShape shape, CoordinateReferenceSystem crs) {
        super(name, shape);
        setRemovable(true);
        setStyle(new RasterStyle(new UniqueColorTable(Arrays.asList(0.0, 1.0),
                        Arrays.asList(Color.WHITE, Color.BLACK)), false));
        setCRS(crs);
    }

    @Override
    public JPopupMenu getContextMenu() {
        JPopupMenu menu = super.getContextMenu();
        menu.add(new AbstractAction("Negative") {
            @Override
            public void actionPerformed(ActionEvent e) {
                WritableRaster r = (WritableRaster) getImageShape().getImage().getData();
                for(int i = 0; i < r.getHeight(); i++) {
                    for(int j = 0; j < r.getWidth(); j++) {
                        r.setSample(j, i, 0, 1 - r.getSample(j, i, 0));
                    }
                }
                getImageShape().setImage(new BufferedImage(getImageShape().getImage().getColorModel(), r, false, null));
            }
        });
        return menu;
    }

}
