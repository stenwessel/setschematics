/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.data.drawing;

import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.LineSegment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wmeulema
 */
public class ConcentricDrawing extends Drawing<BaseGeometry> {
    
    public List<Circle> circles;
    public int angularResolution;

    @Override
    public List<BaseGeometry> structure() {
        ArrayList<BaseGeometry> structure = new ArrayList<>(circles);

        Vector center = center();
        double outerRadius = circles.get(circles.size() - 1).getRadius();
        Vector up = Vector.up(outerRadius);

        for (int i = 0; i < angularResolution; i++) {
            Vector direction = Vector.rotate(up, 2.0*Math.PI*i/angularResolution);
            LineSegment segment = LineSegment.byStartAndOffset(center, direction);
            structure.add(segment);
        }

        return structure;
    }

    @Override
    public Vector center() {
        return circles.get(0).getCenter();
    }
    
    
    
    
}
