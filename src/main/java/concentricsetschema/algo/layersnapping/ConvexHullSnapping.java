/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.layersnapping;

import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Vertex;
import nl.tue.geometrycore.algorithms.hulls.ConvexHull;
import nl.tue.geometrycore.algorithms.hulls.ConvexHull.ListMaintenance;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class ConvexHullSnapping extends SnapAlgorithm {

    private boolean allowCenter = true;
    
    @Override
    public String toString() {
        return "ConvexHull";
    }

    @Override
    public void createInterface(SideTab tab) {
        super.createInterface(tab); 
        
        tab.addCheckbox("Allow center snap", allowCenter, (e, b) -> {
            allowCenter = b;
        });
    }

    @Override
    public boolean run(Drawing drawing) {
        if (!(drawing instanceof ConcentricDrawing)) {
            System.err.println("Cannot run " + toString() + " on nonConcentric drawing");
            return false;
        }
         
        super.run(drawing);
        
        ConcentricDrawing cdrawing = (ConcentricDrawing) drawing;

        ConvexHull<Vertex> ch = new ConvexHull();

        List<Vertex> vertices = new ArrayList(cdrawing.hypergraph.vertices);
        List<List<Vertex>> hulls = new ArrayList();
        boolean centerPossible = false;
        while (!vertices.isEmpty()) {
            if (vertices.size() <= 3) {
                hulls.add(vertices);
                centerPossible = vertices.size() == 1;
                break;
            } else {
                List<Vertex> hull = ch.computeHull(vertices, ListMaintenance.REMOVE_CH);
                hulls.add(hull);
            }
        }
        
        

        if (hulls.size() > cdrawing.circles.size() + (centerPossible && allowCenter ? 1 : 0)) {
            System.err.println("More hulls than circles; #hulls: " + hulls.size());
            return false;
        } else {
            int min;
            if (centerPossible && allowCenter) {
                Vertex v = hulls.get(hulls.size()-1).get(0);
                cdrawing.vertices[v.graphIndex] = cdrawing.circles.get(0).getCenter().clone();
                min = 1;
            } else {
                min = 0;
            }
            for (int i = 0; i < hulls.size() - min; i++) {
                for (Vertex v : hulls.get(i)) {
                    cdrawing.vertices[v.graphIndex] = snap(v, cdrawing.circles.get(hulls.size() - 1 - i - min));
                }
            }

            return true;
        }
    }
}
