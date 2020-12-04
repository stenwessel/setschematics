/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.data.drawing;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wmeulema
 */
public class GridDrawing extends Drawing<LineSegment> {
    
    public List<Double> x_coords, y_coords;
    
    
    @Override
    public List<LineSegment> structure() {
        List<LineSegment> result = new ArrayList();
        double min_x = x_coords.get(0);
        double min_y = y_coords.get(0);
        double max_x = x_coords.get(x_coords.size()-1);
        double max_y = y_coords.get(y_coords.size()-1);
        
        Vector up = Vector.up(max_y-min_y);
        for (double x : x_coords) {
            result.add(LineSegment.byStartAndOffset(new Vector(x, min_y), up));
        }
        
        Vector right = Vector.right(max_x-min_x);
        for (double y : y_coords) {
            result.add(LineSegment.byStartAndOffset(new Vector(min_x, y), right));
        }
        
        return result;
    }
    
    
    @Override
    public Vector center() {
        double min_x = x_coords.get(0);
        double min_y = y_coords.get(0);
        double max_x = x_coords.get(x_coords.size()-1);
        double max_y = y_coords.get(y_coords.size()-1);
        return new Vector((min_x+max_x)/2.0, (min_y+max_y)/2.0);
    }
}
