/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.gui;

import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Vertex;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.geometryrendering.GeometryPanel;
import nl.tue.geometrycore.geometryrendering.glyphs.GlyphFillMode;
import nl.tue.geometrycore.geometryrendering.glyphs.GlyphStrokeMode;
import nl.tue.geometrycore.geometryrendering.glyphs.PointStyle;
import nl.tue.geometrycore.geometryrendering.styling.*;

import java.awt.event.MouseEvent;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DrawPanel extends GeometryPanel {

    private final Data data;

    private final PointStyle disk = new PointStyle() {
        @Override
        public GlyphStrokeMode getStrokeMode() {
            return GlyphStrokeMode.CLEAR;
        }

        @Override
        public BaseGeometry getGlyphShape() {
            return new Circle(Vector.origin(), 1);
        }

        @Override
        public double getStrokeWidth() {
            return 0;
        }

        @Override
        public GlyphFillMode getFillMode() {
            return GlyphFillMode.STROKE;
        }
    };

    public DrawPanel(Data data) {
        this.data = data;
    }

    @Override
    protected void drawScene() {
        if (data.graph == null) {
            return;
        }

        setSizeMode(SizeMode.WORLD);

        boolean isInput = data.drawing == data.drawings.get(0);

        setLayer("circles");
        setStroke(ExtendedColors.darkGray, 0.5, Dashing.SOLID);        
        setFill(null, Hashures.SOLID);
        if (isInput) {
            draw(data.generateBaseDrawing().structure());
        } else {
            draw(data.drawing.structure());
        }

        setFill(null, Hashures.SOLID);
        for (Hyperedge he : data.graph.hyperedges) {
            setLayer(he.name);
            if (data.useCasing) {
                setStroke(ExtendedColors.white, data.drawing.linewidth + 2 * data.drawing.linespace, Dashing.SOLID);
                draw(data.drawing.hyperedges[he.graphIndex]);
            }
            setStroke(he.color, data.drawing.linewidth, Dashing.SOLID);
            draw(data.drawing.hyperedges[he.graphIndex]);
        }

        setLayer("elements");
        setStroke(ExtendedColors.black, 1, Dashing.SOLID);
        double toff = 0;
        double rad = 0;
        if (!data.minimizeVertices) {
            rad = (data.drawing.linewidth + data.drawing.linespace) * data.graph.hyperedges.size() / 2.0;
            toff = rad + 1;
        }
        setTextStyle(TextAnchor.LEFT, data.labelsize);

        if (data.showleaders && !isInput) {
            for (Vertex v : data.graph.vertices) {
                LineSegment ls = new LineSegment(v, data.drawing.vertices[v.graphIndex]);
                draw(ls);
            }
        }

        for (Vertex v : data.graph.vertices) {
            Vector loc = data.drawing.vertices[v.graphIndex].clone();
            if (data.minimizeVertices) {
                rad = data.drawing.linespace + (data.drawing.linewidth + data.drawing.linespace) * v.hyperedges.size() / 2.0;
                toff = rad + 1;
            }
            // casing
            if (data.useCasing) {
                setPointStyle(disk, rad + data.drawing.linespace);
                setStroke(ExtendedColors.white, 1, Dashing.SOLID);
                draw(loc);
            }
            setStroke(ExtendedColors.black, 1, Dashing.SOLID);
            setPointStyle(disk, rad);
            // node
            switch (data.colorVertices) {
                default:
                case BLACK:
                    draw(loc);
                    break;
                case RINGS:
                    double dr = (rad - data.drawing.linespace) / v.hyperedges.size();
                    for (int i = v.hyperedges.size() - 1; i >= 0; i--) {
                        setStroke(v.hyperedges.get(i).color, 1, Dashing.SOLID);
                        setPointStyle(disk, data.drawing.linespace + dr * (i + 1));
                        draw(loc);
                    }
                    setStroke(ExtendedColors.black, 1, Dashing.SOLID);
                    break;
                case PIZZA:
                    if (v.hyperedges.size() == 1) {
                        setStroke(v.hyperedges.get(0).color, 1, Dashing.SOLID);
                        draw(loc);
                        setStroke(ExtendedColors.black, 1, Dashing.SOLID);
                    } else {
                        double da = Math.PI * 2.0 / v.hyperedges.size();
                        LineSegment arm = new LineSegment(loc.clone(), Vector.add(loc, Vector.up(rad)));
                        LineSegment arm2 = arm.clone();
                        arm2.reverse();
                        arm2.rotate(da, loc);
                        CircularArc arc = new CircularArc(loc.clone(), arm.getEnd().clone(), arm2.getStart().clone(), true);
                        GeometryCycle cycle = new GeometryCycle(arm, arc, arm2);
                        setStroke(null, 1, Dashing.SOLID);
                        for (int i = 0; i < v.hyperedges.size(); i++) {
                            setFill(v.hyperedges.get(i).color, Hashures.SOLID);
                            draw(cycle);
                            cycle.rotate(da, loc);
                        }
                        setStroke(ExtendedColors.black, 1, Dashing.SOLID);
                        setFill(null, Hashures.SOLID);
                    }
                    break;
            }
            loc.translate(toff, 0);
            draw(loc, v.name);
        }

        setLayer("legend");
        Vector root = convertViewToWorld(getView().leftTop());
        setTextStyle(TextAnchor.LEFT, data.labelsize);
        root.translate(0.25 * data.labelsize, -data.labelsize);
        LineSegment ls = LineSegment.byStartAndOffset(root.clone(), Vector.multiply(data.labelsize, Vector.right()));
        root.translate(1.25 * data.labelsize, 0);
        for (Hyperedge he : data.graph.hyperedges) {
            setStroke(he.color, data.drawing.linewidth, Dashing.SOLID);
            draw(ls);
            draw(root, he.name);

            root.translate(0, -1.25 * data.labelsize);
            ls.translate(0, -1.25 * data.labelsize);
        }
    }

    @Override
    public Rectangle getBoundingRectangle() {
        if (data.graph == null) {
            return null;
        } else {
            return Rectangle.byBoundingBox(data.graph.vertices);
        }
    }

    @Override
    protected void mousePress(Vector loc, int button, boolean ctrl, boolean shift, boolean alt) {

        if (data.drawings.isEmpty()) {
            return;
        }
        
        boolean isInput = data.drawing == data.drawings.get(0);

        switch (button) {
            case MouseEvent.BUTTON1:
                if (shift && isInput && data.circleMode) {
                    data.center = loc;
                    repaint();
                }
                break;
        }
    }

    @Override
    protected void mouseDrag(Vector loc, Vector prevloc, int button, boolean ctrl, boolean shift, boolean alt) {

        boolean isInput = data.drawing == data.drawings.get(0);

        switch (button) {
            case MouseEvent.BUTTON1:
                if (shift && isInput && data.circleMode) {
                    data.center = loc;
                    repaint();
                }
                break;
        }
    }

    @Override
    protected void keyPress(int keycode, boolean ctrl, boolean shift, boolean alt) {

    }

}
