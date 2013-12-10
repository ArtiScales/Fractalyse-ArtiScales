/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author gvuidel
 */
public class BinRasterLayer extends RasterLayer {

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
            public void actionPerformed(ActionEvent e) {
                WritableRaster r = (WritableRaster) getImageShape().getImage().getData();
                for(int i = 0; i < r.getHeight(); i++)
                    for(int j = 0; j < r.getWidth(); j++)
                        r.setSample(j, i, 0, 1 - r.getSample(j, i, 0));

                getImageShape().setImage(new BufferedImage(getImageShape().getImage().getColorModel(), r, false, null));
            }
        });
        return menu;
    }




}
