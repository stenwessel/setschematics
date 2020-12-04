/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.edgerouting;

import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.support.Supportedge;
import concentricsetschema.data.support.Supportnode;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class TangentRouting extends EdgeRouter {

    private int directions = 2;
    private double tan_length = 1;

    @Override
    public String toString() {
        return "Tangents";
    }

    @Override
    public boolean run(Drawing drawing) {
        if (!(drawing instanceof ConcentricDrawing)) {
            System.err.println("Cannot run " + toString() + " on nonConcentric drawing");
            return false;
        }

        super.run(drawing);

        Vector c = drawing.center();
        double dr = ((ConcentricDrawing) drawing).circles.get(1).getRadius()
                - ((ConcentricDrawing) drawing).circles.get(0).getRadius();

        for (Supportedge se : drawing.support.getEdges()) {

            Vector[] dirs = direction(drawing, se);

            Vector start = drawing.vertices[se.getStart().vertex.graphIndex].clone();
            Vector end = drawing.vertices[se.getEnd().vertex.graphIndex].clone();
//
//            Vector arm_start = Vector.subtract(start, c);
//            arm_start.normalize();
//            Vector arm_end = Vector.subtract(end, c);
//            arm_end.normalize();
//
//            Vector arm_mid = Vector.add(arm_start, arm_end);
//            Line ray = new Line(c, arm_mid);

//            Vector mid_1 = (Vector) ray.intersect(new Line(start, dirs[0])).get(0);
//            Vector mid_2 = (Vector) ray.intersect(new Line(end, dirs[1])).get(0);
           
dirs[0].scale(dr * tan_length);
            dirs[1].scale(dr * tan_length);
            Vector mid_1 = Vector.add(start, dirs[0]);
            Vector mid_2 = Vector.add(end, dirs[1]);

            BezierCurve bc = new BezierCurve(start, mid_1, mid_2, end);
            for (Hyperedge he : se.hyperedges) {
                drawing.hyperedges[he.graphIndex].getParts().add(bc);
            }

        }

        return true;
    }

    @Override
    public void createInterface(SideTab tab) {
        super.createInterface(tab);
        
        tab.addLabel("Directions");
        tab.addIntegerSpinner(directions, 2, Integer.MAX_VALUE, 2, (e, v) -> directions = v);
        tab.addLabel("Tangent length");
        tab.addDoubleSpinner(tan_length, 0, Double.MAX_VALUE, 0.25, (e, v) -> tan_length = v);
    }

    private Vector baseTangent(Drawing drawing, Supportnode node) {
        return baseTangent(drawing, node.vertex.graphIndex);
    }

    private Vector baseTangent(Drawing drawing, int vertex) {
        Vector c = drawing.center();
        Vector loc = drawing.vertices[vertex];
        Vector tan = Vector.subtract(loc, c);
        tan.rotate90DegreesCounterclockwise();
        tan.normalize();
        return tan;
    }

    private Vector[] direction(Drawing drawing, Supportedge edge) {
        return new Vector[]{direction(drawing, edge.getStart(), edge), direction(drawing, edge.getEnd(), edge)};
    }

    private Vector direction(Drawing drawing, Supportnode node, Supportedge edge) {
        double bucketsize = Math.PI * 2.0 / directions;

        Vector tan = baseTangent(drawing, node);
        tan.rotate(-bucketsize / 2.0);

        Vector dir = Vector.subtract(drawing.vertices[edge.getOtherVertex(node).vertex.graphIndex], drawing.vertices[node.vertex.graphIndex]);
        dir.normalize();

        double angle = tan.computeCounterClockwiseAngleTo(dir, false, false);
        int bucket = (int) (angle / bucketsize);

        tan.rotate(bucket * bucketsize + bucketsize / 2.0);
        return tan;
    }

}
