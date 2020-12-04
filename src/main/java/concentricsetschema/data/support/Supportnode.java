/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.data.support;

import concentricsetschema.data.hypergraph.Vertex;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.graphs.simple.SimpleVertex;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Supportnode extends SimpleVertex<LineSegment, Supportnode, Supportedge>  {
    
    public Vertex vertex;
    
    public Supportnode(Vector position) {
        super(position);
    }
    
}
