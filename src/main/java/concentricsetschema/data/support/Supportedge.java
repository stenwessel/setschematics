/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.data.support;

import concentricsetschema.data.hypergraph.Hyperedge;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.graphs.simple.SimpleEdge;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Supportedge extends SimpleEdge<LineSegment, Supportnode, Supportedge>  {

    public List<Hyperedge> hyperedges = new ArrayList();
    public boolean ccwdirected = false; // specific to CycleSupports...   
    
}
