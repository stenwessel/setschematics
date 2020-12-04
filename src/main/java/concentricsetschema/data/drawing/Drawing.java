/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.data.drawing;

import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.support.Supportgraph;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;

import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public abstract class Drawing<T extends BaseGeometry> {
    
    public String label;
    public Hypergraph hypergraph;
    public Supportgraph support;
    public Vector[] vertices;
    public GeometryGroup[] hyperedges;
    public double linewidth, linespace;
    
    public abstract List<T> structure();
    
    public abstract Vector center();
}
