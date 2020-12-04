/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.gui;

import concentricsetschema.algo.integrated.SAPrototype;
import concentricsetschema.algo.integrated.SimulatedAnnealing;
import concentricsetschema.data.drawing.Drawing;
import concentricsetschema.io.Export;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;
import nl.tue.geometrycore.io.BaseWriter;
import nl.tue.geometrycore.io.ipe.IPEWriter;
import nl.tue.geometrycore.io.raster.RasterWriter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class SidePanel extends TabbedSidePanel {

    private final Data data;
    private SideTab viewtab;
    private SideTab algoTab;
    private SideTab centerTab;

    public SidePanel(Data data) {
        this.data = data;
        addIOtab();
        addCenterTab();
        addAlgorithmTab();
        addAnnealingTab();
        addSAPrototypeTab();
        addViewTab();
        addSolutions();
    }

    private void addIOtab() {
        SideTab tab = addTab("IO");

        tab.addButton("Load file", (e) -> {
            data.loadTSV();
        });

        tab.addButton("Save TSV file", (e) -> {
            if (data.graph != null) {
                Export.chooseFile(data.graph);
            }
        });

        JFileChooser chooser = new JFileChooser("./screenshots/");
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("IPE graphic", "ipe"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG image", "png"));

        tab.addButton("Screenshot", (e) -> {
            if (data.graph == null) {
                return;
            }
            int result = chooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                Rectangle R = data.draw.getBoundingRectangle();
                R.grow(R.diagonal() * 0.05);
                BaseWriter write = null;
                try {
                    if (file.getName().endsWith(".ipe")) {
                        IPEWriter ipe;
                        write = ipe = IPEWriter.fileWriter(file);
                        ipe.setWorldview(data.draw.getBoundingRectangle());
                        ipe.initialize();
                        String[] layers = new String[3 + data.graph.hyperedges.size()];
                        layers[0] = "circles";
                        layers[1] = "elements";
                        layers[2] = "legend";
                        for (int i = 0; i < data.graph.hyperedges.size(); i++) {
                            layers[3 + i] = data.graph.hyperedges.get(i).name;
                        }
                        ipe.newPage(layers);
                    } else if (file.getName().endsWith(".png")) {
                        write = RasterWriter.imageWriter(R, 1920, 1080, file);
                        write.initialize();
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Unsupported file extension, use .png or .ipe",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    data.draw.render(write);
                    write.close();
                } catch (IOException ex) {
                    Logger.getLogger(SidePanel.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        if (write != null) {
                            write.close();
                        }
                    } catch (IOException ex1) {
                        Logger.getLogger(SidePanel.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }

        });

    }

    private void addCenterTab() {
        centerTab = addTab("Center");

        refreshCenterTab();

    }

    private void refreshCenterTab() {
        centerTab.clearTab();

        centerTab.addCheckbox("Circle mode", data.circleMode, (e, b) -> {
            data.circleMode = b;
            refreshCenterTab();
            data.draw.repaint();
        });

        if (data.circleMode) {
            centerTab.addLabel("Shift-click to move center");

            centerTab.addLabel("Number of circles:");
            centerTab.addIntegerSpinner(data.numCircles, 1, Integer.MAX_VALUE, 1, (e, v) -> {
                data.numCircles = v;
                data.draw.repaint();
            });

            centerTab.addLabel("Angular resolution:");
            centerTab.addIntegerSpinner(data.angularResolution, 1, Integer.MAX_VALUE, 1, (e, v) -> {
                data.angularResolution = v;
                data.draw.repaint();
            });

            centerTab.addLabel("Automated placement");
            centerTab.addComboBox(data.centerplacements, data.centerplace, (e, s) -> {
                data.centerplace = s;
                refreshCenterTab();
            });
            data.centerplace.createInterface(centerTab);

            centerTab.addButton("Compute", (e) -> {
                if (data.graph != null) {
                    data.center = data.centerplace.compute(data.graph);
                    data.draw.repaint();
                }
            });
        } else {
            centerTab.makeSplit(4, 2);
            centerTab.addLabel("#columns");
            centerTab.addIntegerSpinner(data.numCols, 2, Integer.MAX_VALUE, 1, (e,v) ->{
                data.numCols = v;
                data.draw.repaint();
            });
            
            centerTab.makeSplit(4, 2);
            centerTab.addLabel("#rows");
            centerTab.addIntegerSpinner(data.numRows, 2, Integer.MAX_VALUE, 1, (e,v) ->{
                data.numRows = v;
                data.draw.repaint();
            });
        }

        centerTab.invalidate();
    }

    private void addAlgorithmTab() {
        algoTab = addTab("Algorithms");

        refreshAlgorithmTab();
    }

    private void refreshAlgorithmTab() {
        algoTab.clearTab();

        algoTab.addButton("RUN!", (e) -> {
            data.runAlgorithm();
        });

        algoTab.addSeparator(4);

        algoTab.addLabel("Support");
        algoTab.addComboBox(data.supporters, data.support, (e, gen) -> {
            data.support = gen;
            refreshAlgorithmTab();
        });
        data.support.createInterface(algoTab);

        algoTab.addSeparator(4);

        algoTab.addLabel("Circle snapping");
        algoTab.addComboBox(data.snappers, data.snap, (e, s) -> {
            data.snap = s;
            refreshAlgorithmTab();
        });
        data.snap.createInterface(algoTab);

        algoTab.addSeparator(4);

        algoTab.addLabel("Edge routing");
        algoTab.addComboBox(data.routers, data.router, (e, s) -> {
            data.router = s;
            refreshAlgorithmTab();
        });
        data.router.createInterface(algoTab);

        algoTab.invalidate();
    }

    private void addSolutions() {
        viewtab = addTab("Solutions");
        refreshSolutions();
    }

    public void refreshSolutions() {
        viewtab.clearTab();

        if (data.graph != null) {
            ButtonGroup select = viewtab.addButtonGroup();

            for (Drawing D : data.drawings) {
                viewtab.addRadioButton(D.label, data.drawing == D, select, (e) -> {
                    data.drawing = D;
                    data.draw.repaint();
                });
            }
        }

        viewtab.invalidate();
    }

    private void addViewTab() {
        SideTab tab = addTab("View");

        tab.addCheckbox("Show leaders", data.showleaders, (e, b) -> {
            data.showleaders = b;
            data.draw.repaint();
        });

        tab.addLabel("Label size:");
        tab.addDoubleSpinner(data.labelsize, 0, Double.MAX_VALUE, 0.1, (e, v) -> {
            data.labelsize = v;
            data.draw.repaint();
        });

        tab.addCheckbox("Minimize vertices", data.minimizeVertices, (e, b) -> {
            data.minimizeVertices = b;
            data.draw.repaint();
        });

        tab.addComboBox(VertexStyle.values(), data.colorVertices, (e, v) -> {
            data.colorVertices = v;
            data.draw.repaint();
        });

        tab.addCheckbox("Use casing", data.useCasing, (e, b) -> {
            data.useCasing = b;
            data.draw.repaint();
        });
    }

    private void addAnnealingTab() {
        SideTab tab = addTab("SimAnneal");

        SimulatedAnnealing SA = new SimulatedAnnealing();
        SA.createInterface(tab);

        tab.addButton("Run!", (e) -> {
            Drawing D = data.generateBaseDrawing();
            boolean success = SA.run(D);
            if (success) {
                data.drawing = D;
                data.drawings.add(D);
                data.draw.repaint();
                data.side.refreshSolutions();
            } else {
                JOptionPane.showMessageDialog(null,
                        "No solution was found",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void addSAPrototypeTab() {
        SideTab tab = addTab("SA Prototype");

        SAPrototype potato = new SAPrototype();
        potato.createInterface(tab);

        tab.addButton("Run!", (e) -> {
            Drawing<?> D = data.generateBaseDrawing();
            boolean success = potato.run(D, data.numRows, data.numCols);
            if (success) {
                data.drawing = D;
                data.drawings.add(D);
                data.draw.repaint();
                data.side.refreshSolutions();
            } else {
                JOptionPane.showMessageDialog(null,
                                              "No solution was found",
                                              "Warning",
                                              JOptionPane.WARNING_MESSAGE);
            }
        });
    }

}
