/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.data.hypergraph;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class Hyperedge {

    public String name;
    public Color color;
    public int graphIndex;
    public List<Vertex> vertices = new ArrayList();
}
