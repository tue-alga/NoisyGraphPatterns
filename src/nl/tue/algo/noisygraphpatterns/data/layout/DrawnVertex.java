package nl.tue.algo.noisygraphpatterns.data.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.Vertex;
import nl.tue.geometrycore.geometry.Vector;

public class DrawnVertex {

    public final Vertex vertex;
    public Vector position = null;

    public DrawnVertex(Vertex vertex) {
        this.vertex = vertex;
    }

}
