/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.edgerouting;

import concentricsetschema.data.drawing.Drawing;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class EdgeRouter {

    // static to share between all routers
    protected static double linewidth = 1.6;
    protected static double linespace = 0.4;

    public boolean run(Drawing drawing) {
        drawing.linewidth = linewidth;
        drawing.linespace = linespace;
        drawing.hyperedges = new GeometryGroup[drawing.hypergraph.hyperedges.size()];
        for (int i = 0; i < drawing.hypergraph.hyperedges.size(); i++) {
            drawing.hyperedges[i] = new GeometryGroup();
        }
        return false;
    }

    @Override
    public abstract String toString();

    public void createInterface(SideTab tab) {

        tab.addLabel("Line width:");
        tab.addDoubleSpinner(linewidth, 0, Double.MAX_VALUE, 0.1, (e, v) -> {
            linewidth = v;
        });

        tab.addLabel("Line spacing:");
        tab.addDoubleSpinner(linespace, 0, Double.MAX_VALUE, 0.1, (e, v) -> {
            linespace = v;
        });
    }
}
