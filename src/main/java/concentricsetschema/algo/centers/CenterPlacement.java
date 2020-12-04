/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo.centers;

import concentricsetschema.data.hypergraph.Hypergraph;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

/**
 *
 * @author wmeulema
 */
public abstract class CenterPlacement {

    public abstract Vector compute(Hypergraph H);

    @Override
    public abstract String toString();

    public void createInterface(SideTab tab) {

    }
}
