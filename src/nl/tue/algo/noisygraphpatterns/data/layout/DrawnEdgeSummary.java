package nl.tue.algo.noisygraphpatterns.data.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.EdgeSummary;
import nl.tue.geometrycore.geometry.OrientedGeometry;

public class DrawnEdgeSummary {

    public final EdgeSummary summary;
    public OrientedGeometry route;

    public DrawnEdgeSummary(EdgeSummary summary) {
        this.summary = summary;
    }
}
