/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.edgerouting;

import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Vertex;
import concentricsetschema.data.support.Supportedge;
import concentricsetschema.data.support.Supportgraph;
import concentricsetschema.data.support.Supportnode;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.BezierCurve;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometry.mix.GeometryString;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.Pair;

import javax.swing.*;
import java.util.*;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SmoothPathRouter extends EdgeRouter {

    // using the Andreas' Conjecture
    private static final double PHI = (1 + Math.sqrt(5)) / 2.0;
    private static final double phi = 2 - PHI;
    private double ratio = phi;
    private boolean splitAtVertex = true;
    private boolean diametrical = false;

    @Override
    public void createInterface(SideTab tab) {
        super.createInterface(tab);

        tab.addLabel("Ratio: ");
        JSpinner spin = tab.addDoubleSpinner(ratio, 0, 1, 0.01, (e, v) -> ratio = v);
        tab.addButton("Set ratio to phi", (e) -> {
            ratio = phi;
            spin.setValue(ratio);
        });

        tab.addCheckbox("Split at vertex", splitAtVertex, (e, b) -> splitAtVertex = b);
        tab.addCheckbox("Diametrical decomp", diametrical, (e, b) -> diametrical = b);
    }

    @Override
    public boolean run(Drawing drawing) {
        super.run(drawing);

        Vector center = drawing.center();

//        System.err.println("");
//        System.err.println("Computing... ");
        for (Hyperedge he : drawing.hypergraph.hyperedges) {
            if (he.vertices.size() == 1) {
                Vector v = getLocation(drawing, he.vertices.get(0), he);

                Vector arm = Vector.subtract(v, center);
                arm.rotate90DegreesClockwise();
                arm.normalize();
                arm.scale((linespace + linewidth) * drawing.hypergraph.hyperedges.size());
                Vector start = Vector.add(v, arm);
                Vector end = Vector.subtract(v, arm);

                drawing.hyperedges[he.graphIndex].getParts().add(new LineSegment(start, end));
            } else {

                List<List<Supportnode>> pathdecomp = decompose(drawing, he, drawing.support);

//                System.err.println("Decomp of " + he.name);
                for (List<Supportnode> path : pathdecomp) {
//                    System.err.print("  >");
//                    for (Supportnode n : path) {
//                        System.err.print(" " + n.vertex.name);
//                    }
//                    System.err.println("");

                    if (path.get(0) == path.get(path.size() - 1)) {
//                        System.err.println("  :cycle");
                        // cycle
                        List<BezierCurve> bcs = new ArrayList();
                        int size = path.size() - 1;
                        for (int i = 1; i <= size; i++) {
//                            System.err.println("   ** " + path.get(i).vertex.name + " > " + path.get((i + 1) % size).vertex.name);
                            Vector p = getLocation(drawing, path.get((i - 1) % size).vertex, he);
                            Vector a = getLocation(drawing, path.get(i % size).vertex, he);
                            Vector b = getLocation(drawing, path.get((i + 1) % size).vertex, he);
                            Vector n = getLocation(drawing, path.get((i + 2) % size).vertex, he);
                            BezierCurve bc = recompute(p, a, b, n);
                            bcs.add(bc);
                        }
                        drawing.hyperedges[he.graphIndex].getParts().add(new GeometryCycle(bcs));
                    } else {
//                        System.err.println("  :path");
                        // path
                        List<BezierCurve> bcs = new ArrayList();
                        int size = path.size();
                        for (int i = 0; i < size - 1; i++) {
//                            System.err.println("   ** " + path.get(i).vertex.name + " > " + path.get(i + 1).vertex);
                            Vector p = i <= 0 ? null : getLocation(drawing, path.get(i - 1).vertex, he);
                            Vector a = getLocation(drawing, path.get(i).vertex, he);
                            Vector b = getLocation(drawing, path.get(i + 1).vertex, he);
                            Vector n = i + 2 >= size ? null : getLocation(drawing, path.get(i + 2).vertex, he);
                            BezierCurve bc = recompute(p, a, b, n);
                            bcs.add(bc);
                        }
                        drawing.hyperedges[he.graphIndex].getParts().add(new GeometryString(bcs));
                    }
                }

            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "Smooth";
    }

    private Vector getLocation(Drawing drawing, Vertex vertex, Hyperedge hyperedge) {
        Vector loc = drawing.vertices[vertex.graphIndex];
        if (splitAtVertex) {
            Vector center = drawing.center();
            if (!loc.isApproximately(center)) {
                Vector arm = Vector.subtract(loc, center);
                arm.normalize();
                double scale = -(linewidth + linespace) * (vertex.hyperedges.size() - 1) / 2.0
                        + (linewidth + linespace) * vertex.hyperedges.indexOf(hyperedge);
                arm.scale(scale);
                loc = Vector.add(loc, arm);
            }
        }
        return loc;

    }

    private List<List<Supportnode>> decompose(Drawing drawing, Hyperedge he, Supportgraph support) {
        if (diametrical) {
            return decomposeDiametrical(he, support);
        } else {
            return decomposeSmoothly(drawing, he, support);
        }
    }

    private List<List<Supportnode>> decomposeSmoothly(Drawing drawing, Hyperedge he, Supportgraph support) {

        List<List<Supportnode>> result = new ArrayList();
        for (Supportedge se : support.getEdges()) {
            if (se.hyperedges.contains(he)) {
                List<Supportnode> path = new ArrayList();
                path.add(se.getStart());
                path.add(se.getEnd());
                result.add(path);
            }
        }

        for (Supportnode node : support.getVertices()) {
            List<Pair<Boolean, List<Supportnode>>> paths = new ArrayList();

//            System.err.println("merging at node " + node.vertex.graphIndex);
            for (List<Supportnode> path : result) {
                if (path.get(0) == node) {
//                    System.err.print("  start: ");
//                    for (Supportnode n : path) {
//                        System.err.print(" " + n.vertex.graphIndex);
//                    }
//                    System.err.println("");
                    paths.add(new Pair(true, path));
                }
                if (path.get(path.size() - 1) == node) {
//                    System.err.print("  end: ");
//                    for (Supportnode n : path) {
//                        System.err.print(" " + n.vertex.graphIndex);
//                    }
//                    System.err.println("");
                    paths.add(new Pair(false, path));
                }
            }

            while (paths.size() >= 2) {
                // find the best pair to merge
                Pair<Boolean, List<Supportnode>> bestA = null;
                Pair<Boolean, List<Supportnode>> bestB = null;
                double score = 50;
                int c = paths.size();
                for (int i = 0; i < c; i++) {
                    Pair<Boolean, List<Supportnode>> candA = paths.get(i);
                    Supportnode otherA = candA.getFirst() ? candA.getSecond().get(1) : candA.getSecond().get(candA.getSecond().size() - 2);
                    Vector dirA = Vector.subtract(drawing.vertices[otherA.vertex.graphIndex], drawing.vertices[node.vertex.graphIndex]);
                    dirA.normalize();
                    //System.err.println(" A: " + otherA.vertex.graphIndex);
                    for (int j = i + 1; j < c; j++) {
                        Pair<Boolean, List<Supportnode>> candB = paths.get(j);
                        Supportnode otherB = candB.getFirst() ? candB.getSecond().get(1) : candB.getSecond().get(candB.getSecond().size() - 2);
                        Vector dirB = Vector.subtract(drawing.vertices[node.vertex.graphIndex], drawing.vertices[otherB.vertex.graphIndex]);
                        dirB.normalize();
                        double angle = Math.abs(dirB.computeSignedAngleTo(dirA, false, false));
                        //System.err.println("    B: " + otherB.vertex.graphIndex);
                        //System.err.println("    angle: " + angle);
                        if (angle < score) {
                            bestA = candA;
                            bestB = candB;
                            score = angle;
                        }
                    }
                }

//                System.err.print("  bestA: ");
//                for (Supportnode n : bestA.getSecond()) {
//                    System.err.print(" " + n.vertex.graphIndex);
//                }
//                System.err.println("");
//                System.err.print("  bestB: ");
//                for (Supportnode n : bestB.getSecond()) {
//                    System.err.print(" " + n.vertex.graphIndex);
//                }
//                System.err.println("");
                if (bestA.getSecond() == bestB.getSecond()) {
                    // cycle, do nothing, just remove both
                    paths.remove(bestA);
                    paths.remove(bestB);
                    //System.err.println("  best is cycle");
                } else {

                    // merge the lists
                    if (bestA.getFirst()) {
                        Collections.reverse(bestA.getSecond());
                    }
                    if (!bestB.getFirst()) {
                        Collections.reverse(bestB.getSecond());
                    }
                    // update bestA to also include bestB
                    bestA.getSecond().remove(bestA.getSecond().size() - 1);
                    bestA.getSecond().addAll(bestB.getSecond());
                    result.remove(bestB.getSecond());

//                    System.err.print("  result: ");
//                    for (Supportnode n : bestA.getSecond()) {
//                        System.err.print(" " + n.vertex.graphIndex);
//                    }
//                    System.err.println("");
                    paths.remove(bestA);
                    paths.remove(bestB);

                    // be careful here, if we just used a cycle, but not for the cycle, it may still occur in the other list                    
                    if (bestA.getSecond().get(0) == node) {
                        paths.add(new Pair(true, bestA.getSecond()));
                    }
                    if (bestA.getSecond().get(bestA.getSecond().size() - 1) == node) {
                        paths.add(new Pair(false, bestA.getSecond()));
                    }
                }
            }
        }

        return result;
    }

    private List<List<Supportnode>> decomposeDiametrical(Hyperedge he, Supportgraph support) {

        boolean[] used = new boolean[support.getEdges().size()];
        int unused = 0;
        for (Supportedge se : support.getEdges()) {
            if (!se.hyperedges.contains(he)) {
                used[se.getGraphIndex()] = true;
            } else {
                used[se.getGraphIndex()] = false;
                unused++;
            }
        }

        List<List<Supportnode>> result = new ArrayList();
        while (unused > 0) {
            List<Supportnode> diam = diametricalPath(he, support, used);
            unused -= (diam.size() - 1);
            result.add(diam);
        }

        // see if any can be merged into a cycle greedily
        for (int i = 0; i < result.size(); i++) {
            List<Supportnode> p_i = result.get(i);
            for (int j = i + 1; j < result.size(); j++) {
                List<Supportnode> p_j = result.get(j);

                boolean merged = false;
                if (p_i.get(0) == p_j.get(0) && p_i.get(p_i.size() - 1) == p_j.get(p_j.size() - 1)) {
                    // NB: skip copying the first to avoid duplicates, but do copy the last to indicate a cycle
                    for (int k = p_j.size() - 2; k >= 0; k--) {
                        p_i.add(p_j.get(k));
                    }
                    merged = true;
                } else if (p_i.get(p_i.size() - 1) == p_j.get(0) && p_i.get(0) == p_j.get(p_j.size() - 1)) {
                    // NB: skip copying the first to avoid duplicates, but do copy the last to indicate a cycle
                    for (int k = 1; k < p_j.size(); k++) {
                        p_i.add(p_j.get(k));
                    }
                    merged = true;
                }
                if (merged) {
                    if (j < result.size() - 1) {
                        result.set(j, result.remove(result.size() - 1));
                    } else {
                        result.remove(j);
                    }
                    break;
                }
            }
        }

        return result;
    }

    private List<Supportnode> diametricalPath(Hyperedge he, Supportgraph support, boolean[] used) {
        // we assume a tree, but cope with cycles...

        // first, find an element as far away from an arbitrary one (with an edge still) with BFS (last element to be added to the queue)
        boolean[] visited = new boolean[support.getVertices().size()];
        Arrays.fill(visited, false);
        Queue<Supportnode> Q = new LinkedList();
        Supportnode firstElement = null;
        for (Supportedge se : support.getEdges()) {
            if (!used[se.getGraphIndex()]) {
                firstElement = se.getStart();
                break;
            }
        }

        Q.add(firstElement);
        visited[firstElement.getGraphIndex()] = true;
        while (!Q.isEmpty()) {
            firstElement = Q.poll();
            for (Supportedge se : firstElement.getEdges()) {
                if (!used[se.getGraphIndex()]) {
                    Supportnode nbr = se.getOtherVertex(firstElement);
                    if (!visited[nbr.getGraphIndex()]) {
                        Q.add(nbr);
                        visited[nbr.getGraphIndex()] = true;
                    }
                }
            }
        }

        Supportnode lastElement = firstElement;
        // now, do it again, starting from the previous last element
        Arrays.fill(visited, false);
        Q.add(lastElement);
        visited[lastElement.getGraphIndex()] = true;
        Supportnode[] prev = new Supportnode[support.getVertices().size()];
        while (!Q.isEmpty()) {
            lastElement = Q.poll();
            for (Supportedge se : lastElement.getEdges()) {
                if (!used[se.getGraphIndex()]) {
                    Supportnode nbr = se.getOtherVertex(lastElement);
                    if (!visited[nbr.getGraphIndex()]) {
                        Q.add(nbr);
                        prev[nbr.getGraphIndex()] = lastElement;
                        visited[nbr.getGraphIndex()] = true;
                    }
                }
            }
        }

        List<Supportnode> result = new ArrayList();
        Supportnode p = lastElement;
        while (p != null) {
            result.add(p);
            Supportnode np = prev[p.getGraphIndex()];
            if (np != null) {
                Supportedge se = p.getEdgeTo(np);
                used[se.getGraphIndex()] = true;
            }
            p = np;
        }

        return result;
    }

    private BezierCurve recompute(Vector p, Vector a, Vector b, Vector n) {

        double dist = a.distanceTo(b);
        double cpdist = dist * ratio;

        Vector dirAB = Vector.subtract(b, a);
        dirAB.normalize();

        Vector dirA;
        if (p != null) {
            double prevdist = a.distanceTo(p);

            dirA = dirAB.clone();

            Vector helpdir = Vector.subtract(a, p);
            helpdir.normalize();
            double angle = dirA.computeSignedAngleTo(helpdir, false, false);

            dirA.rotate(angle * prevdist / (dist + prevdist));
        } else {
            dirA = dirAB.clone();
        }

        dirA.normalize();
        dirA.scale(cpdist);

        Vector dirB;
        if (n != null) {
            double prevdist = b.distanceTo(n);

            dirB = dirAB.clone();
            dirB.invert();

            Vector helpdir = Vector.subtract(b, n);
            helpdir.normalize();
            double angle = dirB.computeSignedAngleTo(helpdir, false, false);

            dirB.rotate(angle * prevdist / (dist + prevdist));
        } else {
            dirB = dirAB.clone();
            dirB.invert();
        }
        dirB.normalize();
        dirB.scale(cpdist);

        Vector u = Vector.add(a, dirA);
        Vector v = Vector.add(b, dirB);

        BezierCurve bc = new BezierCurve(a, u, v, b);
        return bc;
    }

}
