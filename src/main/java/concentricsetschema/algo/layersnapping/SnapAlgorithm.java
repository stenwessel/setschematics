/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.layersnapping;

import concentricsetschema.data.drawing.Drawing;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class SnapAlgorithm {

    public boolean run(Drawing drawing) {
        drawing.vertices = new Vector[drawing.hypergraph.vertices.size()];
        return false;
    }

    public void createInterface(SideTab tab) {
    }

    @Override
    public abstract String toString();

    public static Vector snap(Vector v, Circle c) {
        Vector pos = Vector.subtract(v, c.getCenter());
        pos.normalize();
        pos.scale(c.getRadius());
        pos.translate(c.getCenter());
        return pos;
    }
}
