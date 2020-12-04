/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.supportgeneration;

import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.support.Supportedge;
import concentricsetschema.data.support.Supportgraph;
import concentricsetschema.data.support.Supportnode;
import nl.tue.geometrycore.algorithms.EdgeWeightInterface;
import nl.tue.geometrycore.algorithms.mst.DirectedTreeNode;
import nl.tue.geometrycore.algorithms.mst.MinimumSpanningTree;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class MSTSupports extends SupportGenerator {

    private enum WeightMode {
        EUCLIDEAN,
        RADIAL
    }

    private WeightMode weightmode = WeightMode.RADIAL;

    @Override
    public boolean run(Drawing drawing, boolean useOriginalLocation) {
        super.run(drawing, useOriginalLocation);

        Hypergraph H = drawing.hypergraph;

        for (int i = 0; i < drawing.support.getVertices().size(); i++) {
            Supportnode v = drawing.support.getVertices().get(i);
            for (int j = 0; j < i; j++) {
                Supportnode u = drawing.support.getVertices().get(j);
                drawing.support.addEdge(u, v);
            }
        }

        int cnt = H.hyperedges.size();
        while (cnt > 0) {
            cnt--;
            for (Hyperedge he : H.hyperedges) {

                EdgeWeightInterface<Supportedge> ewi;
                switch (weightmode) {
                    default:
                    case EUCLIDEAN: {
                        ewi = (Supportedge edge) -> {
                            if (!edge.getStart().vertex.hyperedges.contains(he) || !edge.getEnd().vertex.hyperedges.contains(he)) {
                                return Double.POSITIVE_INFINITY;
                            } else if (edge.hyperedges.isEmpty()) {
                                return edge.getStart().distanceTo(edge.getEnd());
                            } else {
                                return 0;
                            }
                        };
                        break;
                    }
                    case RADIAL: {
                        Vector center = drawing.center();
                        if (center == null) {
                            System.err.println("Cannot use " + weightmode + " with this structure in MST Support");
                            return false;
                        }
                        ewi = (Supportedge edge) -> {
                            if (!edge.getStart().vertex.hyperedges.contains(he) || !edge.getEnd().vertex.hyperedges.contains(he)) {
                                return Double.POSITIVE_INFINITY;
                            } else if (edge.hyperedges.isEmpty()) {
                                Vector dStart = Vector.subtract(edge.getStart(), center);
                                double distStart = dStart.length();
                                dStart.scale(1.0 / distStart);

                                Vector dEnd = Vector.subtract(edge.getEnd(), center);
                                double distEnd = dEnd.length();
                                dEnd.scale(1.0 / distEnd);

                                double angle = distEnd < DoubleUtil.EPS || distStart < DoubleUtil.EPS
                                        ? 0
                                        : Math.abs(dStart.computeSignedAngleTo(dEnd, false, false));

                                return Math.abs(distEnd - distStart) + angle * (distStart + distEnd) / 2.0;

                            } else {
                                return 0;
                            }
                        };
                        break;
                    }
                }

                for (Supportedge se : drawing.support.getEdges()) {
                    se.hyperedges.remove(he);
                }
                MinimumSpanningTree<Supportgraph, LineSegment, Supportnode, Supportedge> mst
                        = new MinimumSpanningTree(drawing.support, ewi);
                DirectedTreeNode<Supportgraph, LineSegment, Supportnode, Supportedge> result
                        = mst.computeMinimumSpanningTree(drawing.support.getVertices().get(he.vertices.get(0).graphIndex));

                for (Supportedge se : result.getAllEdgesInSubtree()) {
                    se.hyperedges.add(he);
                }
            }
        }

        List<Supportedge> toremove = new ArrayList();
        for (Supportedge se : drawing.support.getEdges()) {
            if (se.hyperedges.isEmpty()) {
                toremove.add(se);
            }
        }
        for (Supportedge se : toremove) {
            drawing.support.removeEdge(se);
        }

        return true;
    }

    @Override
    public String toString() {
        return "MST Iteration";
    }

    @Override
    public void createInterface(SideTab tab) {
        super.createInterface(tab);

        tab.addLabel("Distance:");
        tab.addComboBox(WeightMode.values(), weightmode, (e, v) -> {
            weightmode = v;
        });
    }
}
