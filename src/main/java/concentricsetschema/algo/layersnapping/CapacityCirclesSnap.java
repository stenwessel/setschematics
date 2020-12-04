/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.layersnapping;

import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.hypergraph.Vertex;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class CapacityCirclesSnap extends SnapAlgorithm {

    private boolean allowCenter = true;
    private int firstCapacity = 8;
    private double wedge = 10;

    @Override
    public boolean run(Drawing drawing) {
        if (!(drawing instanceof ConcentricDrawing)) {
            System.err.println("Cannot run " + toString() + " on nonConcentric drawing");
            return false;
        }
         
        super.run(drawing);
        
        Hypergraph graph = drawing.hypergraph;
        List<Circle> circles = ((ConcentricDrawing) drawing).circles;
        Vector center = circles.get(0).getCenter();

        List<Vertex> vertices = new ArrayList(graph.vertices);
        vertices.sort((u, v) -> {
            return Double.compare(u.distanceTo(center), v.distanceTo(center));
        });

        int i = 0;
        if (allowCenter) {
            drawing.vertices[vertices.get(i).graphIndex] = center.clone();
            i++;
        }
        int circindex = 0;
        int capacity = (circindex + 1) * firstCapacity;
        int currentLayerStart = i;

        double angle = Math.toRadians(wedge);

        while (i < vertices.size() && circindex < circles.size()) {
            Vertex v = vertices.get(i);
            for (int j = currentLayerStart; j < i; j++) {
                double diffangle = Math.abs(Vector.subtract(vertices.get(j), center).computeSignedAngleTo(Vector.subtract(v, center)));
                if (diffangle < angle) {
                    circindex++;
                    capacity = (circindex + 1) * firstCapacity;
                    currentLayerStart = i;
                    break;
                }
            }
            if (circindex < circles.size()) {
                drawing.vertices[v.graphIndex] = snap(v, circles.get(circindex));
                i++;

                capacity--;
                if (capacity == 0) {
                    circindex++;
                    capacity = (circindex + 1) * firstCapacity;
                    currentLayerStart = i;
                }
            }
        }

        return i == vertices.size();
    }

    @Override
    public String toString() {
        return "Capacity-based circles";
    }

    @Override
    public void createInterface(SideTab tab) {
        super.createInterface(tab);
        
        tab.addCheckbox("Allow center snap", allowCenter, (e, b) -> {
            allowCenter = b;
        });

        tab.addLabel("First capacity:");
        tab.addIntegerSpinner(firstCapacity, 1, Integer.MAX_VALUE, 1, (e, v) -> {
            firstCapacity = v;
        });

        tab.addLabel("Wedge (degrees):");
        tab.addDoubleSpinner(wedge, -1, 180, 1, (e, v) -> {
            wedge = v;
        });
    }

}
