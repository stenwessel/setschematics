/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.data.support;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.graphs.simple.SimpleGraph;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Supportgraph extends SimpleGraph<LineSegment, Supportnode, Supportedge> {

    @Override
    public Supportnode createVertex(double x, double y) {
        return new Supportnode(new Vector(x,y));
    }

    @Override
    public Supportedge createEdge() {
        return new Supportedge();
    }

    public Supportedge addEdge(Supportnode from, Supportnode to) {
        return addEdge(from, to, new LineSegment(from.clone(), to.clone()));
    }
    
    
}
