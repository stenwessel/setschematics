/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.centers;

import concentricsetschema.data.hypergraph.Hypergraph;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 *
 * @author wmeulema
 */
public class BoundingBoxCenter extends CenterPlacement {

    @Override
    public Vector compute(Hypergraph H) {
        Rectangle R = Rectangle.byBoundingBox(H.vertices);
        return R.center();
    }

    @Override
    public String toString() {
        return "BoundingBox";
    }
    
}
