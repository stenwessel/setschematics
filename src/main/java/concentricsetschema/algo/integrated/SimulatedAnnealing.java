/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.integrated;

import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.hypergraph.Vertex;
import concentricsetschema.data.support.Supportgraph;
import concentricsetschema.data.support.Supportnode;
import nl.tue.geometrycore.datastructures.list2d.List2D;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.HalfLine;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.mix.GeometryGroup;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SimulatedAnnealing {

    // params
    private int ringsAllowed = 2;
    private double wedge = Math.toRadians(10);
    private int numSpokes = 36;
    // internals
    private Hypergraph H;
    private List<Circle> circles;
    private List2D<GridLocation> grid; // grid of locations
    private GridLocation[] assigned; // maps vertex to currently selected location
    private List<GridLocation>[] assignable; // maps vertex to valid locations
    private Tree[] trees; // maps hyperedge to tree

    public void createInterface(SideTab tab) {

        tab.addLabel("Number of spokes:");
        tab.addIntegerSpinner(numSpokes, 1, Integer.MAX_VALUE, 1, (e, v) -> numSpokes = v);

        tab.addLabel("Vertex rings allowed:");
        tab.addIntegerSpinner(ringsAllowed, 1, Integer.MAX_VALUE, 1, (e, v) -> ringsAllowed = v);

        tab.addLabel("Vertex wedge (deg):");
        tab.addDoubleSpinner(Math.toDegrees(wedge), 0, 180, 1, (e, v) -> wedge = Math.toRadians(v));
    }

    public boolean run(Drawing D) {
        if (!(D instanceof ConcentricDrawing)) {
            System.err.println("Cannot run SA on nonConcentric drawing");
            return false;
        }
        ConcentricDrawing cD = (ConcentricDrawing) D;
        this.H = D.hypergraph;
        this.circles = cD.circles;
        // construct the grid, and assign vertices to grid locations
        initGrid();
        // make some tree for each hyperedge
        initTrees();
        // do SA to improve
        anneal();
        // convert the current state to a drawing
        updateDrawing(cD);
        return true;        
    }

    private void initGrid() {
        grid = new List2D(2 * numSpokes, 2 * circles.size() + 1);
        Vector center = circles.get(0).getCenter();
        double dcirc = circles.get(1).getRadius() - circles.get(0).getRadius();

        for (int s = 0; s < 2 * numSpokes; s++) {
            HalfLine ray = new HalfLine(Vector.origin(), Vector.right());
            ray.rotate(s * Math.PI * 2.0 / (2 * numSpokes));
            ray.translate(center);
            for (int c = 0; c < circles.size(); c++) {
                Vector next = (Vector) circles.get(c).intersect(ray).get(0);
                GridLocation gl = new GridLocation();
                gl.loc = next.clone();
                gl.loc.translate(Vector.multiply(-dcirc / 2.0, ray.getDirection()));
                gl.vertex = null;
                grid.set(s, 2 * c, gl);
                gl.circ = 2 * c;
                gl.spoke = s;

                gl = new GridLocation();
                gl.loc = next;
                gl.vertex = null;
                grid.set(s, 2 * c + 1, gl);
                gl.circ = 2 * c + 1;
                gl.spoke = s;

                if (c == circles.size() - 1) {
                    gl = new GridLocation();
                    gl.loc = next.clone();
                    gl.loc.translate(Vector.multiply(dcirc / 2.0, ray.getDirection()));
                    gl.vertex = null;
                    grid.set(s, 2 * c + 2, gl);
                    gl.circ = 2 * c + 2;
                    gl.spoke = s;
                }
            }
        }

        assignable = new List[H.vertices.size()];
        assigned = new GridLocation[H.vertices.size()];
        for (Vertex v : H.vertices) {
            Vector arm = Vector.subtract(v, center);
            double d = arm.normalize();

            assignable[v.graphIndex] = new ArrayList();
            for (GridLocation gl : grid.perColumn()) {
                Vector arm2 = Vector.subtract(gl.loc, center);
                double d2 = arm2.normalize();

                double dist = Math.abs(d2 - d);
                if (dist / dcirc > ringsAllowed) {
                    continue;
                }

                double angle = Math.abs(arm.computeSignedAngleTo(arm2, false, false));
                if (angle > wedge) {
                    continue;
                }

                assignable[v.graphIndex].add(gl);
            }

            assignable[v.graphIndex].sort((GridLocation a, GridLocation b) -> Double.compare(a.loc.distanceTo(v), b.loc.distanceTo(v)));
            for (GridLocation gl : assignable[v.graphIndex]) {
                if (gl.allowsVertex() && gl.vertex == null) {
                    gl.vertex = v;
                    assigned[v.graphIndex] = gl;
                    break;
                }
            }
        }

    }

    private void initTrees() {
        trees = new Tree[H.hyperedges.size()];
        for (Hyperedge he : H.hyperedges) {
            Tree t = trees[he.graphIndex] = new Tree(grid.getColumns(), grid.getRows());

            for (int s = 0; s < 2 * numSpokes; s++) {
                for (int c = 0; c < 2 * circles.size() + 1; c++) {
                    TreeNode tn = new TreeNode(t, s, c);
                    t.set(s, c, tn);
                }
            }

            // build some support incrementally
            List2D<TreeNode> prevs = new List2D(grid.getColumns(), grid.getRows());

            for (int index = 1; index < he.vertices.size(); index++) {
                // make path from prev to v
                Vertex v = he.vertices.get(index);
                GridLocation loc = assigned[v.graphIndex];
                TreeNode tn = t.get(loc);
                if (tn.degree() == 0) {
                    // find shortest path to some treenode that is already connected, 
                    // avoiding cells assigned to vertices not in this hyperedge
                    for (int s = 0; s < grid.getColumns(); s++) {
                        for (int c = 0; c < grid.getRows(); c++) {
                            prevs.set(s, c, null);
                        }
                    }
                    //System.err.println("Setting up Q");
                    prevs.set(tn.spoke, tn.circle, tn);
                    LinkedList<TreeNode> Q = new LinkedList();
                    Q.add(tn);
                    TreeNode node = tn;
                    while (!Q.isEmpty()) {
                        //System.err.println("  poll"+node);
                        node = Q.poll();
                        if (node.degree() > 0 || node.vertex() == he.vertices.get(0)) {
                            // done
                            //System.err.println("  done");
                            break;
                        } else {
                            for (TreeNode nbr : node.neighbors(false)) {
                                TreeNode prv = prevs.get(nbr.spoke, nbr.circle);
                                if (prv == null
                                        && (nbr.gridLocation().vertex == null || he.vertices.contains(nbr.gridLocation().vertex))) {
                                    //System.err.println("  adding");
                                    int dir = dir(node.gridLocation(), nbr.gridLocation());
                                    int prevdir = prv != null ? dir(prv.gridLocation(), node.gridLocation()) : -1;
                                    if (dir == prevdir) {
                                        // same dir, continue on!
                                        Q.addFirst(nbr);
                                    } else {
                                        Q.addLast(nbr);
                                    }
                                    prevs.set(nbr.spoke, nbr.circle, node);
                                }
                            }
                        }
                    }
                    // reconstruct path
                    while (node != tn) {
                        TreeNode prev = prevs.get(node.spoke, node.circle);
                        TreeEdge edge = new TreeEdge();
                        edge.a = prev;
                        edge.b = node;
                        t.edges.add(edge);
                        edge.a.arr[dir(edge.a.gridLocation(), edge.b.gridLocation())] = edge;
                        edge.b.arr[dir(edge.b.gridLocation(), edge.a.gridLocation())] = edge;
                        node = prev;
                    }
                }
            }
        }
    }

    private void anneal() {
        // TODO
    }

    private Drawing updateDrawing(ConcentricDrawing drawing) {
        
        drawing.label = "SimAnnealing";
        drawing.linespace = 0.4;
        drawing.linewidth = 1.6;

        drawing.support = new Supportgraph();
        for (Vertex v : drawing.hypergraph.vertices) {
            Supportnode node = drawing.support.addVertex(v);
            node.vertex = v;
        }
        // TODO: infer a support?
        //for (Tree t : trees) {
        //  for (TreeEdge te : t.edges) {
        //drawing.support.addEdge(
        //        drawing.support.getVertices().get(te.a.vertex.graphIndex),
        //        drawing.support.getVertices().get(te.b.vertex.graphIndex));
        //    }
        // }

        drawing.vertices = new Vector[H.vertices.size()];
        for (Vertex v : H.vertices) {
            drawing.vertices[v.graphIndex] = assigned[v.graphIndex].loc;
        }

        drawing.hyperedges = new GeometryGroup[H.hyperedges.size()];
        for (Hyperedge he : H.hyperedges) {
            GeometryGroup grp = drawing.hyperedges[he.graphIndex] = new GeometryGroup();
            for (TreeEdge te : trees[he.graphIndex].edges) {
                // render each edge
                if (te.isArc()) {
                    // same circ, make an arc
                    boolean ccw = (te.a.spoke + 1) % (2 * numSpokes) == te.b.spoke;
                    grp.getParts().add(new CircularArc(
                            circles.get(0).getCenter().clone(),
                            te.a.gridLocation().loc.clone(),
                            te.b.gridLocation().loc.clone(),
                            ccw));
                } else {
                    // same spoke, make a segment
                    grp.getParts().add(new LineSegment(
                            te.a.gridLocation().loc.clone(),
                            te.b.gridLocation().loc.clone()));
                }
            }
        }
        return drawing;
    }

    private class GridLocation {

        int circ;
        int spoke;
        Vector loc;
        Vertex vertex;

        private boolean allowsVertex() {
            return circ % 2 == 1 && spoke % 2 == 1;
        }
    }

    private class Tree extends List2D<TreeNode> {

        List<TreeEdge> edges = new ArrayList();

        public Tree(int columns, int rows) {
            super(columns, rows);
        }

        TreeNode get(GridLocation gl) {
            return get(gl.spoke, gl.circ);
        }
    }

    private class TreeNode {

        Tree tree;
        int circle, spoke;
        TreeEdge[] arr;

        public TreeNode(Tree tree, int spoke, int circle) {
            this.tree = tree;
            this.circle = circle;
            this.spoke = spoke;
            this.arr = new TreeEdge[DIRS.length];
        }

        int degree() {
            int d = 0;
            for (int dir : DIRS) {
                if (arr[dir] != null) {
                    d++;
                }
            }
            return d;
        }

        Vertex vertex() {
            return gridLocation().vertex;
        }

        GridLocation gridLocation() {
            return grid.get(spoke, circle);
        }

        List<TreeNode> neighbors(boolean edgeOnly) {
            List< TreeNode> nbr = new ArrayList(4);
            for (int dir : DIRS) {
                if (arr[dir] != null || !edgeOnly) {
                    TreeNode tn = neighbor(dir);
                    if (tn != null) {
                        nbr.add(tn);
                    }
                }
            }
            return nbr;
        }

        TreeNode neighbor(int dir) {
            int c = circle, s = spoke;
            switch (dir) {
                case UP:
                    c++;
                    break;
                case DOWN:
                    c--;
                    break;
                case LEFT:
                    s++;
                    break;
                case RIGHT:
                    s--;
                    break;
            }
            if (0 <= c && c < tree.getRows()) {
                s = (s + tree.getColumns()) % tree.getColumns();
                return tree.get(s, c);
            } else {
                return null;
            }
        }

    }

    private static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
    private static final int[] DIRS = {UP, DOWN, LEFT, RIGHT};

    private int dir(GridLocation from, GridLocation to) {
        if (from.spoke == to.spoke) {
            if (from.circ < to.circ) {
                return UP;
            } else {
                return DOWN;
            }
        } else if ((from.spoke + 1) % (2 * numSpokes) == to.spoke) {
            return LEFT;
        } else {
            return RIGHT;
        }
    }

    private class TreeEdge {

        TreeNode a, b;

        boolean isArc() {
            return a.gridLocation().circ == b.gridLocation().circ;
        }
    }
}
