/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.centers;

import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.hypergraph.Vertex;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @author wmeulema
 */
public class MeanCenter  extends CenterPlacement {

    @Override
    public Vector compute(Hypergraph H) {
        Vector mean = Vector.origin();
        for (Vertex v : H.vertices) {            
            mean.translate(Vector.multiply(1.0/H.vertices.size(),v));
        }
        return mean;
    }

    @Override
    public String toString() {
        return "Mean";
    }
    
    
}
