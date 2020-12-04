/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.layersnapping;

import concentricsetschema.data.drawing.Drawing;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class NoSnap extends SnapAlgorithm {

    @Override
    public boolean run(Drawing drawing) {
        super.run(drawing);
        for (int i = 0; i < drawing.vertices.length; i++) {
            drawing.vertices[i] = drawing.hypergraph.vertices.get(i).clone();
        }
        return true;
    }

    @Override
    public String toString() {
        return "No Snap";
    }

}
