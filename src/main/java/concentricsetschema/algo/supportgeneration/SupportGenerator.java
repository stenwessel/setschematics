/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.supportgeneration;

import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Vertex;
import concentricsetschema.data.support.Supportgraph;
import concentricsetschema.data.support.Supportnode;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class SupportGenerator {
    
    public boolean recomputeAfterSnapping = false;
    
    private boolean useOriginalLocation = true;
    private Drawing drawing;
    
    public boolean run(Drawing drawing, boolean useOriginalLocation) {     
        this.useOriginalLocation = useOriginalLocation;
        this.drawing = drawing;
        drawing.support = new Supportgraph();
        for (Vertex v : drawing.hypergraph.vertices) {
            Supportnode node = drawing.support.addVertex(location(v));
            node.vertex = v;
        }
        return false;
    }
    
    protected Vector location(Vertex v) {
        return useOriginalLocation ? v : drawing.vertices[v.graphIndex];
    }
    
    public void createInterface(SideTab tab) {
        tab.addCheckbox("Recompute after snap", recomputeAfterSnapping, (e, b) -> recomputeAfterSnapping = b);
    }
    
    @Override
    public abstract String toString();
    
}
