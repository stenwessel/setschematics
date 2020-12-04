/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.edgerouting;

import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.support.Supportedge;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class StraightRouter extends EdgeRouter {

    @Override
    public boolean run(Drawing drawing) {
        super.run(drawing);

        Vector center = drawing.center();

        for (Supportedge e : drawing.support.getEdges()) {

            Vector locstart = drawing.vertices[e.getStart().getGraphIndex()];
            Vector locend = drawing.vertices[e.getEnd().getGraphIndex()];

            boolean swapped = false;
            if (locstart.distanceTo(center) > locend.distanceTo(center)) {
                Vector tmp = locstart;
                locstart = locend;
                locend = tmp;
                swapped = true;
            }

            Vector armstart = Vector.subtract(locstart, center);
            Vector armend = Vector.subtract(locend, center);

            boolean ccw;
            if (e.ccwdirected) {
                ccw = !swapped;
            } else {
                double angle = armstart.computeSignedAngleTo(armend);
                ccw = angle < 0;
            }

            double offset = -(linewidth + linespace) * (e.hyperedges.size() - 1) / 2.0;

            for (Hyperedge he : e.hyperedges) {

                Vector realstart = locstart.clone();
                Vector realend = locend.clone();
                LineSegment ls = new LineSegment(realstart, realend);
                Vector dir = ls.getDirection();
                if (ccw) {
                    dir.rotate90DegreesClockwise();
                } else {
                    dir.rotate90DegreesCounterclockwise();
                }
                dir.scale(offset);
                ls.translate(dir);

                drawing.hyperedges[he.graphIndex].getParts().add(ls);

                offset += linewidth + linespace;
            }
        }
        
        
        
        for (Hyperedge he : drawing.hypergraph.hyperedges) {
            if (he.vertices.size() == 1) {
                Vector v = drawing.vertices[he.vertices.get(0).graphIndex];
                
                Vector arm = Vector.subtract(v,center);
                arm.rotate90DegreesClockwise();
                arm.normalize();
                arm.scale((linespace+linewidth) * drawing.hypergraph.hyperedges.size());
                Vector start = Vector.add(v, arm);
                Vector end = Vector.subtract(v, arm);
                
                drawing.hyperedges[he.graphIndex].getParts().add(new LineSegment(start,end));
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "Straight";
    }

}
