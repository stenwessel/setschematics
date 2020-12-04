/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.data.hypergraph;

import nl.tue.geometrycore.geometry.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Vertex extends Vector {
    
    public String name;
    public List<Hyperedge> hyperedges = new ArrayList();
    public int graphIndex;
    
    public Vertex(double x, double y) {
        super(x, y);
    }
    
}
