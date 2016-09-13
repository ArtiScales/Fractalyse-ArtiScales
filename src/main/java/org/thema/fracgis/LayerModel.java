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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.thema.drawshape.layer.GroupLayer;
import org.thema.drawshape.layer.Layer;

/**
 * ListModel of layers of type T.
 * 
 * @author Gilles Vuidel
 */
public class LayerModel<T extends Layer> extends AbstractListModel implements ComboBoxModel {

    private List<T> layers;
    private T selElem;

    /**
     * Creates a new LayerModel including all layers from gl of type cls
     * @param gl the layers to include
     * @param cls the type filter
     */
    public LayerModel(GroupLayer gl, Class<T> cls) {
        layers = Collections.checkedList(new ArrayList<T>(), cls);
        init(gl);
        if(!layers.isEmpty()) {
            setSelectedItem(layers.get(0));
        }
    }

    private void init(GroupLayer gl) {
        for(Layer l : gl.getLayers()) {
            try {
                if(l.getName().endsWith("#")) {
                    continue;
                }
                layers.add((T)l);
            } catch(ClassCastException ex) {
                Logger.getLogger(LayerModel.class.getName()).fine("Layer " + l.getName() + " not added.");
            }
            if(l instanceof GroupLayer) {
                init((GroupLayer) l);
            }
        }
    }

    @Override
    public int getSize() {
        return layers.size();
    }

    @Override
    public T getElementAt(int index) {
        return layers.get(index);
    }

    @Override
    public final void setSelectedItem(Object anItem) {
        selElem = (T) anItem;
    }

    @Override
    public T getSelectedItem() {
        return selElem;
    }

    public List<T> getLayers() {
        return layers;
    }

}
