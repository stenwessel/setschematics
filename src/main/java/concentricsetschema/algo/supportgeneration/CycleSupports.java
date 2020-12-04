/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.supportgeneration;

import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.hypergraph.Vertex;
import concentricsetschema.data.support.Supportedge;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.LexicographicOrder;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class CycleSupports extends SupportGenerator {

    private boolean skipLargest = false;

    @Override
    public boolean run(Drawing drawing, boolean useOriginalLocation) {
        super.run(drawing, useOriginalLocation);

        Hypergraph H = drawing.hypergraph;

        Vector center = drawing.center();

        for (Hyperedge e : H.hyperedges) {
            if (e.vertices.size() < 1) {
                continue;
            }

            if (!(drawing instanceof ConcentricDrawing)) {
                e.vertices.sort(new LexicographicOrder());
            } else {
                final Vector ref = Vector.right();
                e.vertices.sort((Vertex o1, Vertex o2) -> {
                    double a1 = Vector.subtract(location(o1), center).computeClockwiseAngleTo(ref, true, false);
                    double a2 = Vector.subtract(location(o2), center).computeClockwiseAngleTo(ref, true, false);
                    return Double.compare(a1, a2);
                });
            }

            int skip = 0;

            int n = e.vertices.size();
            int first = 0;
            if (center == null) {
                skip = 1;
            } else if (skipLargest) {
                double mx = -1;
                for (int i = 0; i < n; i++) {
                    double a = Vector.subtract(location(e.vertices.get(i)), center)
                            .computeCounterClockwiseAngleTo(Vector.subtract(location(e.vertices.get((i + 1) % n)), center));
                    if (a > mx) {
                        mx = a;
                        first = i + 1;
                    }
                }
                skip = 1;
            }

            for (int i = 0; i < n - skip; i++) {
                Supportedge se = drawing.support.addEdge(
                        drawing.support.getVertices().get(e.vertices.get((first + i) % n).graphIndex),
                        drawing.support.getVertices().get(e.vertices.get((first + i + 1) % n).graphIndex));
                se.ccwdirected = true;
                se.hyperedges.add(e);
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "Cycles";
    }

    @Override
    public void createInterface(SideTab tab) {
        super.createInterface(tab);

        tab.addCheckbox("Skip largest", skipLargest, (e, b) -> {
            skipLargest = b;
        });
    }

}
