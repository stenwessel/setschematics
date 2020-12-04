/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.centers;

import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.hypergraph.Vertex;
import nl.tue.geometrycore.geometry.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author wmeulema
 */
public class MedianCenter extends CenterPlacement {
    @Override
    public Vector compute(Hypergraph H) {
        List<Double> xs = new ArrayList();
        List<Double> ys = new ArrayList();
        
        for (Vertex v : H.vertices) {
            xs.add(v.getX());
            ys.add(v.getY());
        }
        Collections.sort(xs);
        Collections.sort(ys);
        
        int med = H.vertices.size()/2;
        
        return new Vector(xs.get(med), ys.get(med));
    }

    @Override
    public String toString() {
        return "Median";
    }
}
