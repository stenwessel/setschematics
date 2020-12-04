package concentricsetschema.algo.integrated;

import concentricsetschema.algo.edgerouting.StraightRouter;
import concentricsetschema.data.drawing.ConcentricDrawing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.data.drawing.GridDrawing;
import concentricsetschema.data.support.Supportedge;
import concentricsetschema.data.support.Supportgraph;
import concentricsetschema.data.support.Supportnode;
import nl.hannahsten.utensils.math.matrix.DoubleVector;
import nl.hannahsten.utensils.math.matrix.VectorKt;
import nl.tue.setschematics.Data;
import nl.tue.setschematics.SetSchematicSAConfig;
import nl.tue.setschematics.grid.Grid;
import nl.tue.setschematics.grid.GridLocation;
import nl.tue.setschematics.grid.circular.ConcentricGrid;
import nl.tue.setschematics.grid.rectangular.RectangularGrid;
import nl.tue.setschematics.heuristic.simanneal.SAResult;
import nl.tue.setschematics.heuristic.simanneal.impl.DefaultSimulatedAnnealing;
import nl.tue.setschematics.hypergraph.Hyperedge;
import nl.tue.setschematics.hypergraph.Hypergraph;
import nl.tue.setschematics.hypergraph.Vertex;
import nl.tue.setschematics.state.Edge;
import nl.tue.setschematics.state.EdgeSet;
import nl.tue.setschematics.state.State;
import nl.tue.setschematics.util.BiMap;
import nl.tue.setschematics.util.SupportKt;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class SAPrototype {

    private int interations = 10_000;
    private double alpha = 420500.0;
    private double beta = 10;
    private double gamma = 2.5;
    private double delta = 5312.0;
    private double epsilon = 0.85;
    private double zeta = 123.25;
    private double octilinearity = 29.0;

    public void createInterface(SideTab tab) {
        tab.addLabel("Iterations:");
        tab.addIntegerSpinner(interations, 0, Integer.MAX_VALUE, 1, (e, value) -> {
            this.interations = value;
        });

        tab.addLabel("alpha:");
        tab.addDoubleSpinner(alpha, -Double.MAX_VALUE, Double.MAX_VALUE, 1, (e, value) -> {
            this.alpha = value;
        });

        tab.addLabel("beta:");
        tab.addDoubleSpinner(beta, -Double.MAX_VALUE, Double.MAX_VALUE, 1, (e, value) -> {
            this.beta = value;
        });

        tab.addLabel("gamma:");
        tab.addDoubleSpinner(gamma, -Double.MAX_VALUE, Double.MAX_VALUE, 1, (e, value) -> {
            this.gamma = value;
        });

        tab.addLabel("delta:");
        tab.addDoubleSpinner(delta, -Double.MAX_VALUE, Double.MAX_VALUE, 1, (e, value) -> {
            this.delta = value;
        });

        tab.addLabel("epsilon:");
        tab.addDoubleSpinner(epsilon, -Double.MAX_VALUE, Double.MAX_VALUE, 1, (e, value) -> {
            this.epsilon = value;
        });

        tab.addLabel("zeta:");
        tab.addDoubleSpinner(zeta, -Double.MAX_VALUE, Double.MAX_VALUE, 1, (e, value) -> {
            this.zeta = value;
        });

        tab.addLabel("Octilinearity:");
        tab.addDoubleSpinner(octilinearity, -Double.MAX_VALUE, Double.MAX_VALUE, 1, (e, value) -> {
            this.octilinearity = value;
        });
    }

    public boolean run(Drawing<?> drawing, int rows, int cols) {
        Grid<?> grid = null;
        if (drawing instanceof GridDrawing) {
            grid = constructGrid((GridDrawing)drawing, rows, cols);
        }
        else if (drawing instanceof ConcentricDrawing) {
            grid = constructGrid((ConcentricDrawing)drawing);
        }

        return anneal(drawing, grid) != null;
    }

    private RectangularGrid constructGrid(GridDrawing baseDrawing, int rows, int cols) {
        double minX = baseDrawing.x_coords.get(0);
        double maxX = baseDrawing.x_coords.get(baseDrawing.x_coords.size() - 1);
        double minY = baseDrawing.y_coords.get(0);
        double maxY = baseDrawing.y_coords.get(baseDrawing.y_coords.size() - 1);

        return RectangularGrid.evenlySpaced(maxX - minX, maxY - minY, rows, cols, new DoubleVector(minX, minY));
    }

    private ConcentricGrid constructGrid(ConcentricDrawing baseDrawing) {
        return ConcentricGrid.fromRadii(
                new DoubleVector(baseDrawing.center().getX(), baseDrawing.center().getY()),
                baseDrawing.circles.stream().map(Circle::getRadius).collect(Collectors.toList()),
                baseDrawing.angularResolution
        );
    }

    public Drawing<?> anneal(Drawing<?> baseDrawing, Grid<?> grid) {
        Pair<Hypergraph, Map<concentricsetschema.data.hypergraph.Vertex, Vertex>> pair = convertHypergraph(baseDrawing.hypergraph);
        Hypergraph hypergraph = pair.getFirst();
        Map<concentricsetschema.data.hypergraph.Vertex, Vertex> vertexMap = pair.getSecond();

        Data data = new Data(hypergraph);
        BiMap<Vertex, GridLocation> locAssignment = SupportKt.greedyLocationAssignment(data, grid);
        EdgeSet support = SupportKt.mstSupport(data, locAssignment);
        State initialState = State.initial(data, grid, locAssignment, support);

        SetSchematicSAConfig config = new SetSchematicSAConfig(
                interations,
                initialState,
                alpha,
                beta,
                gamma,
                delta,
                epsilon,
                zeta,
                octilinearity,
                1984
        );

        SAResult<State> result = new DefaultSimulatedAnnealing<State>().anneal(config);

        updateDrawing(baseDrawing, result.getState(), vertexMap);

        return baseDrawing;
    }

    private void updateDrawing(Drawing<?> drawing, State state,
                               Map<concentricsetschema.data.hypergraph.Vertex, Vertex> vertexMap) {
        drawing.label = "SA Prototype";
        drawing.linespace = 0.4;
        drawing.linewidth = 1.6;

        drawing.vertices = new Vector[state.getVertices().size()];
        for (Map.Entry<concentricsetschema.data.hypergraph.Vertex, Vertex> entry :
                vertexMap.entrySet()) {
            nl.hannahsten.utensils.math.matrix.Vector<Double> location =
                    state.getLocationAssignment().getValue(entry.getValue()).getLocation();
            drawing.vertices[entry.getKey().graphIndex] = new Vector(
                    VectorKt.getX(location),
                    VectorKt.getY(location)
            );
        }

        // Anchor points
        List<Vertex> anchors = new ArrayList<>(state.getAnchorPoints());
        for (int i = vertexMap.size(); i < drawing.vertices.length; i++) {
            Vertex anchor = anchors.get(i - vertexMap.size());
            drawing.vertices[i] = new Vector(
                    VectorKt.getX(anchor.getLocation()),
                    VectorKt.getY(anchor.getLocation())
            );
        }

        // Build the support graph
        drawing.support = new Supportgraph();
        Map<Vertex, Supportnode> supportnodeMap = new HashMap<>();
        for (concentricsetschema.data.hypergraph.Vertex v : drawing.hypergraph.vertices) {
            Supportnode supportnode = drawing.support.addVertex(v);
            supportnode.vertex = v;
            supportnodeMap.put(vertexMap.get(v), supportnode);
        }

        // Add the anchorpoints
        for (Vertex v : anchors) {
            Supportnode supportnode = drawing.support.addVertex(
                    VectorKt.getX(v.getLocation()),
                    VectorKt.getY(v.getLocation())
            );
            supportnodeMap.put(v, supportnode);
        }

        for (Edge edge : state.getEdges()) {
            Supportedge supportedge =
                    drawing.support.addEdge(supportnodeMap.get(edge.getEndpoints().getFirst()),
                                            supportnodeMap.get(edge.getEndpoints().getSecond()));
            supportedge.hyperedges = edge.getHyperedges().stream().map(e -> drawing.hypergraph.hyperedges.stream().filter(h -> h.name.equals(e.getLabel())).findAny().get()).collect(Collectors.toList());
        }

        new StraightRouter().run(drawing);

    }

    private Pair<Hypergraph, Map<concentricsetschema.data.hypergraph.Vertex, Vertex>> convertHypergraph(concentricsetschema.data.hypergraph.Hypergraph hypergraph) {
        Map<concentricsetschema.data.hypergraph.Vertex, Vertex> vertexMap = hypergraph.vertices.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> new Vertex(
                            new DoubleVector(v.getX(), v.getY()),
                            v.name
                        )
                ));

        LinkedHashSet<Hyperedge> edges = hypergraph.hyperedges.stream().map(e -> new Hyperedge(
                e.vertices.stream().map(vertexMap::get).collect(Collectors.toCollection(LinkedHashSet::new)),
                e.name
        )).collect(Collectors.toCollection(LinkedHashSet::new));

        return new Pair<>(new Hypergraph(new LinkedHashSet<>(vertexMap.values()), edges), vertexMap);
    }
}
