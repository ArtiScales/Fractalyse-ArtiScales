/*
 * Copyright (C) 2017 Laboratoire ThéMA - UMR 6049 - CNRS / Université de Franche-Comté
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
package org.thema.fracgis;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.geotools.feature.SchemaException;
import org.thema.common.collection.HashMapList;
import org.thema.data.feature.DefaultFeature;
import org.thema.drawshape.layer.DefaultGroupLayer;
import org.thema.drawshape.layer.FeatureLayer;
import org.thema.drawshape.layer.GroupLayer;

/**
 *
 * @author gvuidel
 */
public class ImportPRAO {
    
    public static final double X_OFFSET = 800000;
    public static final double Y_OFFSET = 300000;
    private static final Coordinate NULL_COORD = new Coordinate(0, 0);
    
    private int numLine = 0;
    private String line = "";
    private GeometryFactory factory;
    
    private List<DefaultFeature> postes, noeuds, charges, transfos, segments, troncons;
    private List<String> posteAttr, noeudAttr, chargeAttr, transfoAttr, segmentAttr, tronconAttr;
    
    public ImportPRAO(File textFile) throws IOException, SchemaException {
        factory = new GeometryFactory();
        postes = new ArrayList<>();
        noeuds = new ArrayList<>();
        charges = new ArrayList<>();
        transfos = new ArrayList<>();
        segments = new ArrayList<>();
        troncons = new ArrayList<>();
        
        posteAttr = new ArrayList<>();
        noeudAttr = new ArrayList<>();
        chargeAttr = new ArrayList<>();
        transfoAttr = new ArrayList<>();
        segmentAttr = new ArrayList<>();
        tronconAttr = new ArrayList<>();
        
        try (BufferedReader r = new BufferedReader(new FileReader(textFile))) {
            while(!line.startsWith("CREER"))  {
                line = r.readLine();
                numLine++;
            }
            while(line != null) {
                if(!line.startsWith("CREER")) {
                    throw new IllegalArgumentException("Unexpected line : " + line + "\nAt line " + numLine);
                }
                String[] elems = line.split("=");
                switch(elems[0]) {
                    case "CREER POSTE":
                        postes.add(createFeature(r, elems[1], posteAttr, false));
                        break;
                    case "CREER NOEUD":
                        noeuds.add(createFeature(r, elems[1], noeudAttr, false));
                        break;
                    case "CREER TRANSFO":
                        transfos.add(createFeature(r, elems[1], transfoAttr, false));
                        break;
                    case "CREER SEGMENT":
                        segments.add(createFeature(r, elems[1], segmentAttr, true));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown command : " + elems[0] + " At line " + numLine);
                }
            }
        }
        
        fillAttributes(postes);
        fillAttributes(noeuds);
        fillAttributes(charges);
        fillAttributes(transfos);
        fillAttributes(segments);
        fillAttributes(troncons);
        
        fillSegNullCoordinates();
        
        
        File d = textFile.getAbsoluteFile().getParentFile();
        
        
        DefaultFeature.saveFeatures(baryConnectSegments(), new File(d, "segments_connected.shp"));
        
        
        DefaultFeature.saveFeatures(postes, new File(d, "postes.shp"));
        DefaultFeature.saveFeatures(noeuds, new File(d, "noeuds.shp"));
        DefaultFeature.saveFeatures(charges, new File(d, "charges.shp"));
        DefaultFeature.saveFeatures(transfos, new File(d, "transfos.shp"));
        DefaultFeature.saveFeatures(segments, new File(d, "segments.shp"));
        DefaultFeature.saveFeatures(troncons, new File(d, "troncons.shp"));
        
        
    }
    
    private DefaultFeature createFeature(BufferedReader r, String id, List<String> attrs, boolean isLine) throws IOException {
        DefaultFeature f = new DefaultFeature(id, null, attrs, new ArrayList(Collections.nCopies(attrs.size(), null)));
        List<Double> ordinates = new ArrayList<>();
        while((line = r.readLine()) != null && line.startsWith("\t")) {
            numLine++;
            String [] elems = line.trim().split("=");
            if(elems[0].contains("[")) {
                ordinates.add(Double.parseDouble(elems[1]));
            } else {
                if(elems.length > 1) {
                    f.addAttribute(elems[0], elems[1]);
                } else {
                    f.addAttribute(elems[0], "");
                }
            }
        }
        numLine++;
        if(f.getAttribute("COX") != null) {
            double x = Double.parseDouble((String)f.getAttribute("COX"));
            double y = Double.parseDouble((String)f.getAttribute("COY"));
            f.setGeometry(factory.createPoint(correctCoord(x, y)));
        } else if(!ordinates.isEmpty()) {
            List<Coordinate> coordinates = new ArrayList<>();
            for(int i = 0; i < ordinates.size(); i+=2) {
                coordinates.add(new Coordinate(correctCoord(ordinates.get(i), ordinates.get(i+1))));
            }
            if(coordinates.size() == 1) {
                coordinates.add(coordinates.get(0));
            }
            f.setGeometry(factory.createLineString(coordinates.toArray(new Coordinate[0])));
        } else {
            if(isLine) {
                f.setGeometry(factory.createLineString(new Coordinate[]{new Coordinate(0, 0), new Coordinate(0, 0)}));
            } else {
                f.setGeometry(factory.createPoint(new Coordinate(0, 0)));
            }
        }
        while(line != null && !line.contains("=")) {
            String type = line.split(" ")[1];
            DefaultFeature f2;
            switch(type) {
                case "CHARGE":
                    f2 = createFeature(r, id, chargeAttr, false);
                    charges.add(f2);
                    break;
                case "TRONCON":
                    f2 = createFeature(r, id, tronconAttr, true);
                    troncons.add(f2);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown item : " + type + " At line " + numLine);    
            }
            f2.setGeometry(f.getGeometry());
        }
        
        return f;
    }
    
    private Coordinate correctCoord(double x, double y) {
        if(x < 63000 || x > 69000 || y < 20000 || y > 30000) {
            return new Coordinate(0, 0);
        } else {
            return new Coordinate(x + X_OFFSET, y + Y_OFFSET);
        }
    }

    private void fillAttributes(List<DefaultFeature> features) {
        for(DefaultFeature f : features) {
            for(int i = f.getAttributes().size(); i < f.getAttributeNames().size(); i++) {
                f.addAttribute(f.getAttributeNames().get(i), null);
            }
        }
    }
    
    private List<DefaultFeature> directConnectSegments() {
        HashMap<String, DefaultFeature> mapNodes = new HashMap<>();
        for(DefaultFeature f : noeuds) {
            mapNodes.put((String)f.getId(), f);
        }
        
        List<DefaultFeature> conSeg = new ArrayList<>();
                
        for(DefaultFeature seg : segments) {
            DefaultFeature n1 = mapNodes.get(seg.getAttribute("N-1"));
            DefaultFeature n2 = mapNodes.get(seg.getAttribute("N-2"));
            
            List<Coordinate> coords = new ArrayList<>(Arrays.asList(seg.getGeometry().getCoordinates()));
            if(n1 != null) {
                if(!n1.getGeometry().getCoordinate().equals(NULL_COORD)) {
                    coords.add(0, n1.getGeometry().getCoordinate());
                }
            }
            if(n2 != null) {
                if(!n2.getGeometry().getCoordinate().equals(NULL_COORD)) {
                    coords.add(n2.getGeometry().getCoordinate());
                }
            }
            
            DefaultFeature f = new DefaultFeature(seg);
            f.setGeometry(factory.createLineString(coords.toArray(new Coordinate[0])));
            conSeg.add(f);
        }
        
        return conSeg;
    }
    
    private void fillSegNullCoordinates() {
        
        HashMapList<String, DefaultFeature> mapNodeSegs = new HashMapList<>();                
        for(DefaultFeature seg : segments) {
            mapNodeSegs.putValue((String)seg.getAttribute("N-1"), seg);
            mapNodeSegs.putValue((String)seg.getAttribute("N-2"), seg);
        }
        for(int i = 0; i < 100; i++) {      
            for(DefaultFeature seg : segments) {
                Coordinate[] coords = seg.getGeometry().getCoordinates();
                if(coords[0].equals(NULL_COORD)) {
                    for(DefaultFeature s : mapNodeSegs.get((String)seg.getAttribute("N-1"))) {
                        if(s == seg) {
                            continue;
                        }
                        Coordinate[] co = s.getGeometry().getCoordinates();
                        if(seg.getAttribute("N-1").equals(s.getAttribute("N-1"))) {
                            coords[0] = co[0].equals(NULL_COORD) ? coords[0] : co[0];
                        } else {
                            coords[0] = co[co.length-1].equals(NULL_COORD) ? coords[0] : co[co.length-1];
                        }
                    }
                }
                if(coords[coords.length-1].equals(NULL_COORD)) {
                    for(DefaultFeature s : mapNodeSegs.get((String)seg.getAttribute("N-2"))) {
                        if(s == seg) {
                            continue;
                        }
                        Coordinate[] co = s.getGeometry().getCoordinates();
                        if(seg.getAttribute("N-2").equals(s.getAttribute("N-1"))) {
                            coords[coords.length-1] = co[0];
                        } else {
                            coords[coords.length-1] = co[co.length-1];
                        }
                    }
                }
                seg.setGeometry(factory.createLineString(coords));
            }
            
            for(DefaultFeature seg : segments) {
                Coordinate[] coords = seg.getGeometry().getCoordinates();
                if(coords[0].equals(NULL_COORD)) {
                    for(DefaultFeature s : mapNodeSegs.get((String)seg.getAttribute("N-2"))) {
                        if(s == seg) {
                            continue;
                        }
                        Coordinate[] co = s.getGeometry().getCoordinates();
                        if(seg.getAttribute("N-2").equals(s.getAttribute("N-1"))) {
                            coords[0] = co[0].equals(NULL_COORD) ? coords[0] : co[0];
                        } else {
                            coords[0] = co[co.length-1].equals(NULL_COORD) ? coords[0] : co[co.length-1];
                        }
                    }
                }
                if(coords[coords.length-1].equals(NULL_COORD)) {
                    for(DefaultFeature s : mapNodeSegs.get((String)seg.getAttribute("N-1"))) {
                        if(s == seg) {
                            continue;
                        }
                        Coordinate[] co = s.getGeometry().getCoordinates();
                        if(seg.getAttribute("N-1").equals(s.getAttribute("N-1"))) {
                            coords[coords.length-1] = co[0];
                        } else {
                            coords[coords.length-1] = co[co.length-1];
                        }
                    }
                }
                seg.setGeometry(factory.createLineString(coords));
            }
        }
    }
    
    private void fillNullCoordinates() {
        HashMap<String, DefaultFeature> mapNodes = new HashMap<>();
        for(DefaultFeature f : noeuds) {
            mapNodes.put((String)f.getId(), f);
        }
        for(int i = 0; i < 100; i++) {       
            for(DefaultFeature seg : segments) {
                DefaultFeature n1 = mapNodes.get(seg.getAttribute("N-1"));
                DefaultFeature n2 = mapNodes.get(seg.getAttribute("N-2"));
                LineString segGeom = (LineString)seg.getGeometry();
                Coordinate[] coords = segGeom.getCoordinates();
                if(n1 != null && n1.getGeometry().getCoordinate().equals(NULL_COORD)) {
                    if(!coords[0].equals(NULL_COORD)) {
                        n1.setGeometry(factory.createPoint(coords[0]));
                    } else {
                        n1.setGeometry(factory.createPoint(coords[coords.length-1]));
                    }
                }
                if(n2 != null && n2.getGeometry().getCoordinate().equals(NULL_COORD)) {
                    if(!coords[coords.length-1].equals(NULL_COORD)) {
                        n2.setGeometry(factory.createPoint(coords[coords.length-1]));
                    } else {
                        n2.setGeometry(factory.createPoint(coords[0]));
                    }
                }
            }   
            for(DefaultFeature seg : segments) {
                DefaultFeature n1 = mapNodes.get(seg.getAttribute("N-1"));
                DefaultFeature n2 = mapNodes.get(seg.getAttribute("N-2"));
                LineString segGeom = (LineString)seg.getGeometry();
                Coordinate[] coords = segGeom.getCoordinates();
                if(n1 != null && !n1.getGeometry().getCoordinate().equals(NULL_COORD) && coords[0].equals(NULL_COORD)) {
                    coords[0] = n1.getGeometry().getCoordinate();
                }
                if(n2 != null && !n2.getGeometry().getCoordinate().equals(NULL_COORD) && coords[coords.length-1].equals(NULL_COORD)) {
                    coords[coords.length-1] = n2.getGeometry().getCoordinate();
                }
                seg.setGeometry(factory.createLineString(coords));
            }   
            for(DefaultFeature seg : segments) {
                DefaultFeature n1 = mapNodes.get(seg.getAttribute("N-1"));
                DefaultFeature n2 = mapNodes.get(seg.getAttribute("N-2"));
                LineString segGeom = (LineString)seg.getGeometry();
                Coordinate[] coords = segGeom.getCoordinates();
                if(n1 != null && !n1.getGeometry().getCoordinate().equals(NULL_COORD) && coords[coords.length-1].equals(NULL_COORD)) {
                    coords[coords.length-1] = n1.getGeometry().getCoordinate();
                }
                if(n2 != null && !n2.getGeometry().getCoordinate().equals(NULL_COORD) && coords[0].equals(NULL_COORD)) {
                    coords[0] = n2.getGeometry().getCoordinate();
                }
                seg.setGeometry(factory.createLineString(coords));
            }   
        }
    }
    
    private List<DefaultFeature> baryConnectSegments() {
        HashMap<String, DefaultFeature> mapNodes = new HashMap<>();
        for(DefaultFeature f : noeuds) {
            mapNodes.put((String)f.getId(), f);
        }
        
        for(DefaultFeature f : transfos) {
            mapNodes.put((String)f.getId(), f);
        }
        
        HashMapList<String, Coordinate> mapCoordNodes = new HashMapList<>();                
        for(DefaultFeature seg : segments) {
            DefaultFeature n1 = mapNodes.get(seg.getAttribute("N-1"));
            DefaultFeature n2 = mapNodes.get(seg.getAttribute("N-2"));
            LineString segGeom = (LineString)seg.getGeometry();
            if(n1 != null) {
                if(!segGeom.getStartPoint().getCoordinate().equals(NULL_COORD)) {
                    mapCoordNodes.putValue((String) n1.getId(), segGeom.getStartPoint().getCoordinate());
                } else
                    n1 = null;
            }
            if(n2 != null) {
                if(!segGeom.getEndPoint().getCoordinate().equals(NULL_COORD)) {
                    mapCoordNodes.putValue((String) n2.getId(), segGeom.getEndPoint().getCoordinate());
                } else
                    n2 = null;
            }
        }
        
        HashMap<String, Coordinate> mapBaryNodes = new HashMap<>();
        for(String nodeId : mapCoordNodes.keySet()) {
            double x = 0, y = 0;
            int nb = 0;
            for(Coordinate c : mapCoordNodes.get(nodeId)) {
                x += c.x;
                y += c.y;
                nb++;
            }
            mapBaryNodes.put(nodeId, new Coordinate(x/nb, y/nb));
        }
        
        List<DefaultFeature> conSeg = new ArrayList<>();   
        for(DefaultFeature seg : segments) {
            Coordinate c1 = mapBaryNodes.get(seg.getAttribute("N-1"));
            Coordinate c2 = mapBaryNodes.get(seg.getAttribute("N-2"));
            List<Coordinate> coords = new ArrayList<>(Arrays.asList(seg.getGeometry().getCoordinates()));
            if(c1 != null) {
                coords.add(0, c1);
            }
            if(c2 != null) {
                coords.add(c2);
            }
            
            DefaultFeature f = new DefaultFeature(seg);
            f.setGeometry(factory.createLineString(coords.toArray(new Coordinate[0])));
            conSeg.add(f);
        }
        
        for(DefaultFeature node : noeuds) {
            if(node.getGeometry().getCoordinate().equals(NULL_COORD)) {
                if(mapBaryNodes.containsKey(node.getId())) {
                    node.setGeometry(factory.createPoint(mapBaryNodes.get(node.getId())));
                }
            }
        }
        
        
        for(DefaultFeature node : transfos) {
            if(node.getGeometry().getCoordinate().equals(NULL_COORD)) {
                if(mapBaryNodes.containsKey(node.getId())) {
                    node.setGeometry(factory.createPoint(mapBaryNodes.get(node.getId())));
                }
            }
        }
        
//        for(DefaultFeature node : charges) {
//            if(node.getGeometry().getCoordinate().equals(NULL_COORD)) {
//                if(mapBaryNodes.containsKey(node.getId())) {
//                    node.setGeometry(factory.createPoint(mapBaryNodes.get(node.getId())));
//                }
//            }
//        }
        
        return conSeg;
    }
    
    public GroupLayer getLayers() {
        DefaultGroupLayer gl = new DefaultGroupLayer("PRAO import");
        gl.setRemovable(true);
        gl.addLayerLast(new FeatureLayer("Postes", postes));
        gl.addLayerLast(new FeatureLayer("Noeuds", noeuds));
        gl.addLayerLast(new FeatureLayer("Charges", charges));
        gl.addLayerLast(new FeatureLayer("Transfos", transfos));
        gl.addLayerLast(new FeatureLayer("Segments", segments));
        gl.addLayerLast(new FeatureLayer("Tronçons", troncons));
        
        return gl;
    }
    
}
