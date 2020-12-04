/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.supportgeneration;

import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Vertex;
import concentricsetschema.data.support.Supportedge;
import nl.tue.geometrycore.algorithms.EdgeWeightInterface;
import nl.tue.geometrycore.algorithms.delaunay.DelaunayTriangulation;
import nl.tue.geometrycore.algorithms.dsp.DijkstrasShortestPath;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.DoubleUtil;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ShortestPathGraph extends SupportGenerator {

    private enum WeightMode {
        EUCLIDEAN,
        L_1,
        RADIAL_L1,
        RADIAL_L2,
    }

    private WeightMode weightmode = WeightMode.EUCLIDEAN;

    private double t = 4;
    private boolean useDT = true;

    @Override
    public String toString() {
        return "SPG";
    }

    @Override
    public void createInterface(SideTab tab) {
        super.createInterface(tab);

        tab.addLabel("t-value:");
        tab.addDoubleSpinner(t, 1, Double.POSITIVE_INFINITY, 1, (e, v) -> t = v);
        tab.addCheckbox("Use DT base", useDT, (e, b) -> useDT = b);
        tab.addLabel("Distance:");
        tab.addComboBox(WeightMode.values(), weightmode, (e, v) -> {
            weightmode = v;
        });
    }

    @Override
    public boolean run(Drawing drawing, boolean useOriginalLocation) {
        super.run(drawing, useOriginalLocation);

        EdgeWeightInterface<SPGEdge> ewi;
        switch (weightmode) {
            default:
            case EUCLIDEAN: {
                ewi = (SPGEdge edge) -> {
                    return Math.pow(edge.getStart().distanceTo(edge.getEnd()), t);
                };
                break;
            }
            case L_1: {
                ewi = (SPGEdge edge) -> {
                    return Math.pow(Math.abs(edge.getStart().getX() - edge.getEnd().getX())
                            + Math.abs(edge.getStart().getY() - edge.getEnd().getY()), t);
                };
                break;
            }
            case RADIAL_L1: {
                Vector center = drawing.center();
                if (center == null) {
                    System.err.println("Cannot use " + weightmode + " with this structure in SPG Support");
                    return false;
                }
                ewi = (SPGEdge edge) -> {
                    Vector dStart = Vector.subtract(edge.getStart(), center);
                    double distStart = dStart.length();
                    dStart.scale(1.0 / distStart);

                    Vector dEnd = Vector.subtract(edge.getEnd(), center);
                    double distEnd = dEnd.length();
                    dEnd.scale(1.0 / distEnd);

                    double angle = Math.abs(dStart.computeSignedAngleTo(dEnd, false, false));

                    double l1_dist = Math.abs(distEnd - distStart) + angle * (distStart + distEnd) / 2.0;

                    return Math.pow(l1_dist, t);
                };
                break;
            }
            case RADIAL_L2: {
                Vector center = drawing.center();
                if (center == null) {
                    System.err.println("Cannot use " + weightmode + " with this structure in SPG Support");
                    return false;
                }
                ewi = (SPGEdge edge) -> {
                    Vector dStart = Vector.subtract(edge.getStart(), center);
                    double distStart = dStart.length();
                    dStart.scale(1.0 / distStart);

                    Vector dEnd = Vector.subtract(edge.getEnd(), center);
                    double distEnd = dEnd.length();
                    dEnd.scale(1.0 / distEnd);

                    double angle = distEnd < DoubleUtil.EPS || distStart < DoubleUtil.EPS
                            ? 0
                            : Math.abs(dStart.computeSignedAngleTo(dEnd, false, false));

                    double l2_dist = Math.sqrt(Math.pow(distEnd - distStart, 2) + Math.pow(angle * (distStart + distEnd) / 2.0, 2));

                    return Math.pow(l2_dist, t);
                };
                break;
            }
        }

        for (Hyperedge he : drawing.hypergraph.hyperedges) {
            addSPG(drawing, he, ewi);
        }

        return true;
    }

    private void addSPG(Drawing drawing, Hyperedge he, EdgeWeightInterface<SPGEdge> ewi) {

        SPGGraph spg = new SPGGraph();

        for (Vertex v : he.vertices) {
            spg.addVertex(location(v)).vertex = v;
        }

        if (useDT || (t >= 2 && weightmode == WeightMode.EUCLIDEAN)) {
            DelaunayTriangulation<SPGGraph, LineSegment, SPGVertex, SPGEdge> dt = new DelaunayTriangulation<>(spg, (LineSegment ls) -> {
                return ls.clone();
            });
            dt.run();
        } else {
            for (int i = 0; i < he.vertices.size(); i++) {
                for (int j = i + 1; j < he.vertices.size(); j++) {
                    spg.addEdge(spg.getVertices().get(i), spg.getVertices().get(j));
                }
            }
        }

        DijkstrasShortestPath<SPGGraph, LineSegment, SPGVertex, SPGEdge> dijkstra = new DijkstrasShortestPath<>(spg, ewi);

        for (SPGEdge e : spg.getEdges()) {
            if (dijkstra.computeShortestDetourLength(e) < Math.pow(e.toGeometry().length(), t)) {
                // skip it
            } else {
                // part of SPG
                Supportedge se = drawing.support.addEdge(
                        drawing.support.getVertices().get(e.getStart().vertex.graphIndex),
                        drawing.support.getVertices().get(e.getEnd().vertex.graphIndex)
                );
                se.hyperedges.add(he);
            }
        }
    }

    private class SPGVertex extends SimpleVertex<LineSegment, SPGVertex, SPGEdge> {

        Vertex vertex;

        public SPGVertex(double x, double y) {
            super(x, y);
        }

    }

    private class SPGEdge extends SimpleEdge<LineSegment, SPGVertex, SPGEdge> {

    }

    private class SPGGraph extends SimpleGraph<LineSegment, SPGVertex, SPGEdge> {

        @Override
        public SPGVertex createVertex(double x, double y) {
            return new SPGVertex(x, y);
        }

        @Override
        public SPGEdge createEdge() {
            return new SPGEdge();
        }

        public SPGEdge addEdge(SPGVertex from, SPGVertex to) {
            return addEdge(from, to, new LineSegment(from.clone(), to.clone()));
        }

    }

}
