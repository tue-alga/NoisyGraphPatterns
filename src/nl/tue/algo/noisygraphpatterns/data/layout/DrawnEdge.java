package nl.tue.algo.noisygraphpatterns.data.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.Edge;
import nl.tue.geometrycore.geometry.OrientedGeometry;

public class DrawnEdge {

    public final Edge edge;
    public OrientedGeometry route;

    public DrawnEdge(Edge edge) {
        this.edge = edge;
    }

}
