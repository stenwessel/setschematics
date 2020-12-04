/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.edgerouting;

import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.hypergraph.Vertex;
import concentricsetschema.data.support.Supportedge;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.HalfLine;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class LanesRouter extends EdgeRouter {

    private double singletonWedge = 5; // degrees

    @Override
    public boolean run(Drawing drawing) {
        if (!(drawing instanceof ConcentricDrawing)) {
            System.err.println("Cannot run " + toString() + " on nonConcentric drawing");
            return false;
        }

        super.run(drawing);

        Hypergraph graph = drawing.hypergraph;
        Vector center = drawing.center();
        double radstep = ((ConcentricDrawing) drawing).circles.get(1).getRadius()
                - ((ConcentricDrawing) drawing).circles.get(0).getRadius();

        List<Hyperedge>[] downs = new List[graph.vertices.size()];
        List<Hyperedge>[] ups = new List[graph.vertices.size()];
        List<Hyperedge>[] sameLayerDirs = ups;
        for (Vertex v : graph.vertices) {
            int vi = v.graphIndex;
            downs[vi] = new ArrayList();
            ups[vi] = new ArrayList();
        }

        for (Supportedge se : drawing.support.getEdges()) {
            int start_index = se.getStart().getGraphIndex();
            int end_index = se.getEnd().getGraphIndex();
            double dA = drawing.vertices[start_index].distanceTo(center);
            double dB = drawing.vertices[end_index].distanceTo(center);
            if (DoubleUtil.close(dA, dB)) {
                for (Hyperedge he : se.hyperedges) {
                    if (!sameLayerDirs[start_index].contains(he)) {
                        sameLayerDirs[start_index].add(he);
                    }
                    if (!ups[end_index].contains(he)) {
                        ups[end_index].add(he);
                    }
                }
            } else if (dA > dB) {
                for (Hyperedge he : se.hyperedges) {
                    if (!downs[start_index].contains(he)) {
                        downs[start_index].add(he);
                    }
                    if (!ups[end_index].contains(he)) {
                        ups[end_index].add(he);
                    }
                }
            } else {
                for (Hyperedge he : se.hyperedges) {
                    if (!ups[start_index].contains(he)) {
                        ups[start_index].add(he);
                    }
                    if (!downs[end_index].contains(he)) {
                        downs[end_index].add(he);
                    }
                }
            }
        }

        for (List<Hyperedge> hes : downs) {
            hes.sort((Hyperedge o1, Hyperedge o2) -> {
                return Integer.compare(o1.graphIndex, o2.graphIndex);
            });
        }
        for (List<Hyperedge> hes : ups) {
            hes.sort((Hyperedge o1, Hyperedge o2) -> {
                return Integer.compare(o1.graphIndex, o2.graphIndex);
            });
        }
        for (Supportedge se : drawing.support.getEdges()) {

            int start_index = se.getStart().getGraphIndex();
            int end_index = se.getEnd().getGraphIndex();

            double dA = drawing.vertices[start_index].distanceTo(center);
            double dB = drawing.vertices[end_index].distanceTo(center);
            boolean samelayer = DoubleUtil.close(dA, dB);

            Vector locstart = drawing.vertices[start_index];
            Vector locend = drawing.vertices[end_index];

            boolean swapped = false;
            if (!samelayer && dA > dB) {
                Vector tmp = locstart;
                locstart = locend;
                locend = tmp;
                swapped = true;
            }

            Vector armstart = Vector.subtract(locstart, center);
            Vector armend = Vector.subtract(locend, center);

            boolean ccw;
            double angle;
            if (se.ccwdirected) {
                ccw = !swapped;
                angle = ccw ? armstart.computeCounterClockwiseAngleTo(armend) : armstart.computeClockwiseAngleTo(armend);
            } else {
                angle = armstart.computeSignedAngleTo(armend);
                ccw = angle < 0;
                if (!ccw) {
                    angle = Math.abs(angle);
                }
            }
            double slen = armstart.length();
            double elen = armend.length();

            for (Hyperedge he : se.hyperedges) {
                List<BaseGeometry> geoms = new ArrayList();

                Vertex start, end;
                if (!swapped) {
                    // no swap was done
                    start = se.getStart().vertex;
                    end = se.getEnd().vertex;
                } else {
                    // swap was done
                    end = se.getStart().vertex;
                    start = se.getEnd().vertex;
                }

                double lane_start = -(linewidth + linespace) * (ups[start.graphIndex].size() - 1) / 2.0
                        + (linewidth + linespace) * ups[start.graphIndex].indexOf(he);
                double lane_end = -(linewidth + linespace) * ((samelayer ? sameLayerDirs : downs)[end.graphIndex].size() - 1) / 2.0
                        + (linewidth + linespace) * (samelayer ? sameLayerDirs : downs)[end.graphIndex].indexOf(he);

                double lane_mid = -(linewidth + linespace) * (graph.hyperedges.size() - 1) / 2.0 + (linewidth + linespace) * graph.hyperedges.indexOf(he);

                Circle start_circ = new Circle(center, slen);
                Circle end_circ = new Circle(center, elen);
                Circle mid_circ = new Circle(center, slen + lane_mid + radstep / 2.0);

                HalfLine start_ray = new HalfLine(center.clone(), armstart.clone());
                HalfLine end_ray = new HalfLine(center.clone(), armend.clone());

                Vector laneoffset = armstart.clone();
                laneoffset.normalize();
                laneoffset.scale(lane_start);
                laneoffset.rotate90DegreesClockwise();
                start_ray.translate(laneoffset);

                Vector realstart;
                {
                    List<BaseGeometry> intersections = start_ray.intersect(start_circ);
                    if (intersections.size() > 0) {
                        realstart = (Vector) intersections.get(0);
                    } else {
                        realstart = null;
                    }
                }
                Vector startshrink;
                {
                    List<BaseGeometry> intersections = start_ray.intersect(mid_circ);
                    if (intersections.size() > 0) {
                        startshrink = (Vector) intersections.get(0);
                    } else {
                        startshrink = null;
                    }
                }

                laneoffset = armend.clone();
                laneoffset.normalize();
                laneoffset.scale(lane_end);
                laneoffset.rotate90DegreesClockwise();
                end_ray.translate(laneoffset);

                Vector realend = (Vector) end_ray.intersect(end_circ).get(0);
                Vector endshrink = (Vector) end_ray.intersect(mid_circ).get(0);

                if (realstart == null || startshrink == null) {
                    geoms.add(new LineSegment(end_ray.getOrigin(), realend));
                } else {
                    CircularArc ca = new CircularArc(center, startshrink, endshrink, ccw);
                    if (Math.abs(ca.centralAngle()) > Math.abs(angle) + 0.1 * Math.PI) {
                        ca.setCounterclockwise(!ccw);
                    }
                    if (DoubleUtil.close(ca.centralAngle(), 0)) {

                        geoms.add(new LineSegment(realstart, realend));
                    } else {
                        geoms.add(new LineSegment(realstart, startshrink));
                        geoms.add(ca);
                        geoms.add(new LineSegment(endshrink, realend));
                    }
                }

                drawing.hyperedges[he.graphIndex].getParts().add(new GeometryString(geoms));
            }
        }

        for (Hyperedge he : graph.hyperedges) {
            if (he.vertices.size() == 1) {
                Vector v = drawing.vertices[he.vertices.get(0).graphIndex];

                Vector arm = Vector.subtract(v, center);
                arm.rotate(Math.toRadians(singletonWedge) / 2.0);
                Vector start = Vector.add(center, arm);
                arm.rotate(-Math.toRadians(singletonWedge));
                Vector end = Vector.add(center, arm);

                drawing.hyperedges[he.graphIndex].getParts().add(CircularArc.byThroughPoint(start, v.clone(), end));
            }
        }

        return true;
    }

    @Override
    public void createInterface(SideTab tab) {
        super.createInterface(tab);

        tab.addLabel("Wedge (degrees): ");
        tab.addDoubleSpinner(singletonWedge, 1, 180, 1, (e, v) -> singletonWedge = v);
    }

    @Override
    public String toString() {
        return "Lanes";
    }

}
