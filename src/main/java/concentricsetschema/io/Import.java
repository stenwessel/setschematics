/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.io;

import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.hypergraph.Vertex;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.GeometryType;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.PolyLine;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Import {

    private final static JFileChooser chooser;
    private final static Color[] colors = {
        ExtendedColors.darkBlue,
        ExtendedColors.darkPurple,
        ExtendedColors.darkGreen,
        ExtendedColors.darkRed,
        ExtendedColors.darkOrange,
        ExtendedColors.lightBlue,
        ExtendedColors.lightPurple,
        ExtendedColors.lightGreen,
        ExtendedColors.lightRed,
        ExtendedColors.lightOrange
    };

    static {
        chooser = new JFileChooser("./data");
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("TSV files", "tsv"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("IPE files", "ipe"));
        chooser.setAcceptAllFileFilterUsed(true);
    }

    public static Hypergraph chooseFile() {
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                if (file.getName().endsWith("tsv")) {
                    return openTSVfile(file);
                } else if (file.getName().endsWith("ipe")) {
                    return openIPEfile(file);
                } else {
                    System.err.println("Unexpected file extension: " + file.getName());
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;

    }

    public static Hypergraph openTSVfile(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }

        Hypergraph H = new Hypergraph();

        Map<String, Hyperedge> map = new HashMap();

        BufferedReader read = null;
        String line = null;
        try {
            read = new BufferedReader(new FileReader(chooser.getSelectedFile()));

            line = read.readLine();

            if (line.startsWith("SETS")) {
                //
                line = read.readLine();
                while (!line.equals("VERTICES")) {
                    // each line: "i set r g b" -- i is assumed to simply count up from 0
                    String[] split = line.split("\t");
                    Hyperedge he = new Hyperedge();
                    he.graphIndex = H.hyperedges.size();
                    he.name = split[1];
                    he.color = new Color(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]));
                    H.hyperedges.add(he);

                    line = read.readLine();
                }
                line = read.readLine();
                while (line != null) {
                    // each line: "vtx x y <sets indices>"
                    String[] split = line.split("\t");
                    Vertex v = new Vertex(Double.parseDouble(split[1]), -Double.parseDouble(split[2]));
                    v.graphIndex = H.vertices.size();
                    H.vertices.add(v);
                    v.name = split[0];
                    for (int i = 3; i < split.length; i++) {
                        Hyperedge he = H.hyperedges.get(Integer.parseInt(split[i]));
                        he.vertices.add(v);
                        v.hyperedges.add(he);
                    }

                    line = read.readLine();
                }
            } else {
                // each line: "vtx x y <set names>"
                while (line != null) {
                    String[] split = line.split("\t");
                    if (split.length >= 3) {
                        Vertex v = new Vertex(Double.parseDouble(split[1]), -Double.parseDouble(split[2]));
                        v.graphIndex = H.vertices.size();
                        H.vertices.add(v);
                        v.name = split[0];

                        for (int i = 3; i < split.length; i++) {
                            Hyperedge e = map.get(split[i]);
                            if (e == null) {
                                e = new Hyperedge();
                                e.graphIndex = H.hyperedges.size();
                                e.name = split[i];
                                e.color = colors[H.hyperedges.size() % colors.length];
                                H.hyperedges.add(e);
                                map.put(split[i], e);
                            }
                            e.vertices.add(v);
                            v.hyperedges.add(e);
                        }
                    }
                    line = read.readLine();
                }
            }
        } catch (Exception ex) {
            System.err.println("Last line: " + line);
            ex.printStackTrace();
            H = null;
        } finally {
            if (read != null) {
                read.close();
            }
        }

        return H;
    }

    public static Hypergraph openIPEfile(File file) throws IOException {
        if (!file.exists()) {
            return null;
        }

        Hypergraph H = new Hypergraph();

        IPEReader read = IPEReader.fileReader(file);
        List<ReadItem> items = read.read();
        read.close();

        for (ReadItem ri : items) {
            if (ri.getGeometry().getGeometryType() == GeometryType.VECTOR && ri.getLayer().equals("Nodes")) {
                Vector loc = (Vector) ri.getGeometry();
                Vertex v = new Vertex(loc.getX(), loc.getY());
                v.graphIndex = H.vertices.size();
                H.vertices.add(v);
                v.name = "v" + H.vertices.size();
            }
        }

        for (ReadItem ri : items) {
            if (!ri.getLayer().equals("Nodes") && !ri.getLayer().equals("Background")) {
                List<Vector> vtcs = new ArrayList();
                breakdown(ri.getGeometry(), vtcs);
                Set<Vertex> vertices = new HashSet();
                for (Vector v : vtcs) {
                    boolean added = false;
                    for (Vertex vt : H.vertices) {
                        if (v.isApproximately(vt, 0.01)) {
                            vertices.add(vt);
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        System.err.println("Couldn't find a vertex for " + v);
                    }
                }
                Hyperedge he = new Hyperedge();
                he.graphIndex = H.hyperedges.size();
                H.hyperedges.add(he);
                he.color = colors[H.hyperedges.size() % colors.length];
                he.name = ri.getLayer();
                for (Vertex v : vertices) {
                    he.vertices.add(v);
                    v.hyperedges.add(he);
                }
                if (vertices.isEmpty()) {
                    System.err.println("Hyperedge " + he.name + " has no vertices");
                }
            }
        }

        for (Vertex v : H.vertices) {
            if (v.hyperedges.isEmpty()) {
                System.err.println("Vertex " + v.name + " not part of any hyperedge");
            }
        }

        return H;
    }

    private static void breakdown(BaseGeometry g, List<Vector> vtcs) {
        switch (g.getGeometryType()) {
            case VECTOR:
                vtcs.add((Vector) g);
                break;
            case LINESEGMENT:
                LineSegment ls = (LineSegment) g;
                vtcs.add(ls.getStart());
                vtcs.add(ls.getEnd());
                break;
            case POLYLINE:
                PolyLine pl = (PolyLine) g;
                vtcs.addAll(pl.vertices());
                break;
            case POLYGON:
                Polygon p = (Polygon) g;
                vtcs.addAll(p.vertices());
                break;
            case GEOMETRYGROUP:
                for (BaseGeometry prt : ((GeometryGroup<?>) g).getParts()) {
                    breakdown(prt, vtcs);
                }
                break;
            case GEOMETRYSTRING:
                for (BaseGeometry prt : ((GeometryString<?>) g).edges()) {
                    breakdown(prt, vtcs);
                }
                break;

        }
    }
}
