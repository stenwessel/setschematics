/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.layersnapping;

import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.drawing.GridDrawing;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @author wmeulema
 */
public class GreedyGridSnap extends SnapAlgorithm {

    @Override
    public boolean run(Drawing drawing) {
        if (!(drawing instanceof GridDrawing)) {
            System.err.println("Cannot run " + toString() + " on nonGrid drawing");
            return false;
        }

        super.run(drawing);

        GridDrawing GD = (GridDrawing) drawing;
        
        // this is a really silly implementation just to test things with

        boolean[][] occupied = new boolean[GD.x_coords.size()][GD.y_coords.size()];
        for (int i = 0; i < GD.x_coords.size(); i++) {
            for (int j = 0; j < GD.y_coords.size(); j++) {
                occupied[i][j] = false;
            }
        }

        for (int vindex = 0; vindex < drawing.vertices.length; vindex++) {
            Vector loc = drawing.hypergraph.vertices.get(vindex);
            Vector closest = loc.clone();
            double dist = Double.POSITIVE_INFINITY;
            int ci = -1;
            int cj = -1;
            for (int i = 0; i < GD.x_coords.size(); i++) {
                for (int j = 0; j < GD.y_coords.size(); j++) {
                    if (!occupied[i][j]) {
                        Vector p = new Vector(GD.x_coords.get(i), GD.y_coords.get(j));
                        double d = p.distanceTo(loc);
                        if (d < dist) {
                            closest = p;
                            ci = i;
                            cj = j;
                            dist = d;
                        }
                    }
                }
            }
            
            if (ci >= 0) {
                occupied[ci][cj] = true; 
            }

            drawing.vertices[vindex] = closest;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Greedy Grid Snapping";
    }

}
