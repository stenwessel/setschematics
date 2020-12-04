/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.io;

import concentricsetschema.data.hypergraph.Hyperedge;
import concentricsetschema.data.hypergraph.Hypergraph;
import concentricsetschema.data.hypergraph.Vertex;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author wmeulema
 */
public class Export {

    private final static JFileChooser chooser;

    static {
        chooser = new JFileChooser("./data");
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("TSV files", "tsv"));
        chooser.setAcceptAllFileFilterUsed(false);
    }

    public static void chooseFile(Hypergraph H) {
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            try {
                if (file.getName().endsWith("tsv")) {
                    saveTSVfile(H, file);
                } else {
                    System.err.println("Unexpected file extension: " + file.getName());
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void saveTSVfile(Hypergraph H, File f) throws IOException {
        BufferedWriter write = null;
        try {
            write = new BufferedWriter(new FileWriter(f));

            write.append("SETS\n");
            for (Hyperedge he : H.hyperedges) {
                write.append(he.graphIndex + "\t" + he.name + "\t" + he.color.getRed() + "\t" + he.color.getGreen() + "\t" + he.color.getBlue()+"\n");
            }
            write.append("VERTICES\n");
            for (Vertex v : H.vertices) {
                write.append(v.name + "\t"+v.getX()+"\t"+(-1*v.getY()));
                for (Hyperedge he : v.hyperedges) {
                    write.append("\t"+he.graphIndex);
                }
                write.append("\n");
            }

        } finally {
            if (write != null) {
                write.close();
            }
        }
    }
}
