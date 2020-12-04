/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.gui;

import concentricsetschema.algo.DrawAlgorithm;
import concentricsetschema.algo.centers.BoundingBoxCenter;
import concentricsetschema.algo.centers.CenterPlacement;
import concentricsetschema.algo.centers.MeanCenter;
import concentricsetschema.algo.centers.MedianCenter;
import concentricsetschema.algo.edgerouting.*;
import concentricsetschema.algo.layersnapping.*;
import concentricsetschema.algo.supportgeneration.CycleSupports;
import concentricsetschema.algo.supportgeneration.MSTSupports;
import concentricsetschema.algo.supportgeneration.ShortestPathGraph;
import concentricsetschema.algo.supportgeneration.SupportGenerator;
import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.drawing.GridDrawing;
import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.io.Import;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.gui.GUIUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Data {

    public static boolean avoidcplex = false;

    public static void main(String[] args) {
        avoidcplex = args.length > 0;
        Data data = new Data();
        GUIUtil.makeMainFrame("Concentric Set Schematization", data.draw, data.side);
        data.loadTSV();
    }

    CenterPlacement[] centerplacements = {new BoundingBoxCenter(), new MeanCenter(), new MedianCenter()};
    CenterPlacement centerplace = centerplacements[0];

    SupportGenerator[] supporters = {new CycleSupports(), new MSTSupports(), new ShortestPathGraph()};
    SupportGenerator support = supporters[1], base_support = supporters[0];

    SnapAlgorithm[] snappers = avoidcplex
            ? new SnapAlgorithm[]{new NoSnap(), new RoundingSnap(), new CapacityCirclesSnap(), new ConvexHullSnapping(), new GreedyGridSnap()}
            : new SnapAlgorithm[]{new NoSnap(), new RoundingSnap(), new CapacityCirclesSnap(), new ConvexHullSnapping(), new GreedyGridSnap()};
    SnapAlgorithm snap = snappers[2], base_snap = snappers[0];

    EdgeRouter[] routers = {new StraightRouter(), new LanesRouter(), new SmoothPathRouter(), new TangentRouting()};
    EdgeRouter router = routers[2], base_router = routers[0];

    public Hypergraph graph = null;
    public boolean circleMode = false;
    // circle spec
    public Vector center = Vector.origin();
    public int numCircles = 6;
    public int angularResolution = 6;
    // grid spec
    public int numCols = 30;
    public int numRows = 30;

    List<Drawing> drawings = new ArrayList();
    public Drawing drawing = null;

    public boolean showleaders = false;
    public boolean useCasing = true;
    public double labelsize = 6.5;
    public boolean minimizeVertices = true;
    public VertexStyle colorVertices = VertexStyle.RINGS;

    public final DrawPanel draw = new DrawPanel(this);
    public final SidePanel side = new SidePanel(this);

    public Drawing generateBaseDrawing() {

        Rectangle R = Rectangle.byBoundingBox(graph.vertices);

        if (circleMode) {
            ConcentricDrawing D = new ConcentricDrawing();
            D.hypergraph = graph;

            double radstep = R.diagonal() / (2.0 * numCircles);

            D.circles = new ArrayList();
            for (int i = 1; i <= numCircles; i++) {
                D.circles.add(new Circle(center, i * radstep));
            }

            D.angularResolution = angularResolution;

            return D;
        } else {
            GridDrawing D = new GridDrawing();
            D.hypergraph = graph;

            D.x_coords = new ArrayList();
            double min_x = R.getLeft();
            double max_x = R.getRight();
            for (int i = 0; i < numCols; i++) {
                D.x_coords.add(min_x + (max_x - min_x) * i / (double) (numCols - 1));
            }

            D.y_coords = new ArrayList();
            double min_y = R.getBottom();
            double max_y = R.getTop();
            for (int i = 0; i < numRows; i++) {
                D.y_coords.add(min_y + (max_y - min_y) * i / (double) (numRows - 1));
            }

            return D;
        }
    }

    void loadTSV() {
        graph = Import.chooseFile();
        newGraph();
    }

    void newGraph() {
        drawings.clear();
        if (graph != null) {
            center = centerplace.compute(graph);
            // run a cycle > no-snap > straight
            DrawAlgorithm alg = new DrawAlgorithm();
            Drawing D = generateBaseDrawing();
            boolean success = alg.run(D, base_support, base_snap, base_router);
            if (success) {
                D.label = "Input";
                drawings.add(D);
            }
            drawing = D;
        } else {
            drawing = null;
        }
        draw.zoomToFit();
    }

    void runAlgorithm() {
        DrawAlgorithm alg = new DrawAlgorithm();
        Drawing D = generateBaseDrawing();
        boolean success = alg.run(D, support, snap, router);
        if (success) {
            drawing = D;
            drawings.add(D);
            draw.repaint();
            side.refreshSolutions();
        } else {
            JOptionPane.showMessageDialog(null,
                    "No solution was found",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

}
