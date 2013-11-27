/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.thema.fracgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.thema.drawshape.layer.GroupLayer;
import org.thema.drawshape.layer.Layer;

/**
 *
 * @author gvuidel
 */
public class LayerModel<T extends Layer> extends AbstractListModel implements ComboBoxModel {

    List<T> layers;
    T selElem;

    public LayerModel(GroupLayer gl, Class<T> cls) {
        layers = Collections.checkedList(new ArrayList<T>(), cls);
        init(gl);
        if(!layers.isEmpty())
            setSelectedItem(layers.get(0));
    }

    private void init(GroupLayer gl) {
        for(Layer l : gl.getLayers()) {
            try {
                if(l.getName().endsWith("#"))
                    continue;
                layers.add((T)l);
            } catch(Exception ex) {

            }
            if(l instanceof GroupLayer)
                init((GroupLayer) l);
        }
    }

    public int getSize() {
        return layers.size();
    }

    public T getElementAt(int index) {
        return layers.get(index);
    }

    public void setSelectedItem(Object anItem) {
        selElem = (T) anItem;
    }

    public T getSelectedItem() {
        return selElem;
    }

    public List<T> getLayers() {
        return layers;
    }

}
