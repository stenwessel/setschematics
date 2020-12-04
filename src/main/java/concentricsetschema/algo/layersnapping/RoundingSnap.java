/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.layersnapping;

import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Vertex;
import nl.tue.geometrycore.geometry.curved.Circle;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class RoundingSnap extends SnapAlgorithm {

    @Override
    public boolean run(Drawing drawing) {
        if (!(drawing instanceof ConcentricDrawing)) {
            System.err.println("Cannot run " + toString() + " on nonConcentric drawing");
            return false;
        }
        
        super.run(drawing);
        
        for (Vertex v : drawing.hypergraph.vertices) {
            Circle closest = null;
            double dist = Double.POSITIVE_INFINITY;
            for (Circle c : ((ConcentricDrawing) drawing).circles) {
                double d = c.distanceTo(v);
                if (d < dist) {
                    closest = c;
                    dist = d;
                }
            }
            drawing.vertices[v.graphIndex] = snap(v,closest);
        }

        return true;
    }

    @Override
    public String toString() {
        return "Rounding";
    }

}
