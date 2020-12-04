/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package concentricsetschema.algo;

import concentricsetschema.algo.edgerouting.EdgeRouter;
import concentricsetschema.algo.layersnapping.SnapAlgorithm;
import concentricsetschema.algo.supportgeneration.SupportGenerator;
import concentricsetschema.data.drawing.Drawing;

/**
 *
 * @author Wouter Meulemans (w.meulemans@tue.nl)
 */
public class DrawAlgorithm {

    public boolean run(Drawing drawing,
            SupportGenerator support,
            SnapAlgorithm snap,
            EdgeRouter route) {

        if (!support.run(drawing, true)) {
            System.err.println("Support failed");
            return false;
        }
        if (!snap.run(drawing)) {
            System.err.println("Snapping failed");
            return false;
        }
        if (support.recomputeAfterSnapping) {
            if (!support.run(drawing, false)) {
                System.err.println("Re-supporting failed");
                return false;
            }
        }
        if (!route.run(drawing)) {
            System.err.println("Routing failed");
            return false;
        }
        
        drawing.label = support+"::"+snap+"::"+route;

        return true;
    }
}
