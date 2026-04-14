package nl.tue.algo.noisygraphpatterns.algorithms.layout;

import java.util.ArrayList;
import java.util.List;
import nl.tue.algo.noisygraphpatterns.data.graph.Graph;
import nl.tue.algo.noisygraphpatterns.data.layout.Layout;
import nl.tue.algo.noisygraphpatterns.data.graph.Vertex;
import nl.tue.geometrycore.algorithms.hulls.ConvexHull;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

public abstract class LayoutAlgorithm {
    
    public Layout run(Graph g) {
        Layout L = new Layout(g);
        run(g, L);
        L.computeMetaData();
        return L;
    }

    protected abstract void run(Graph graph, Layout layout);
    
    public boolean allowIncremental() {
        return false;
    }
    
    protected void runIncrement(Graph graph, Layout layout) {
        throw new UnsupportedOperationException("No incremental method defined");
    }
    
    public void runIncremental(Graph graph, Layout layout) {
        runIncrement(graph, layout);
        layout.computeMetaData();
    }

    public String name() {
        return getClass().getSimpleName();
    }

    public abstract String parameters();

    public abstract void initParameterGUI(SideTab tab);

    @Override
    public String toString() {
        return name();
    }

    public static double y(Vertex v) {
        return v.graph.vertexCount() - v.index - 0.5;
    }

    public static GeometryCycle offsetHull(Graph graph, Layout layout, int from, int to, double off) {
        ConvexHull<Vector> ch = new ConvexHull();
        List<Vector> points = new ArrayList();
        for (int i = from; i <= to; i++) {
            points.add(layout.get(graph.vertices[i]).position);
        }
        List<Vector> hull = ch.computeHull(points, ConvexHull.ListMaintenance.MAINTAIN_ELEMENTS);
        Polygon P = new Polygon(hull);
        return offsetConvexPolygon(P, off);
    }

    public static GeometryCycle offsetRectangle(Graph graph, Layout layout, int from, int to, double off) {
        List<Vector> points = new ArrayList();
        for (int i = from; i <= to; i++) {
            points.add(layout.get(graph.vertices[i]).position);
        }
        Polygon P = Rectangle.byBoundingBox(points).toPolygon();
        return offsetConvexPolygon(P, off);
    }

    public static GeometryCycle offsetConvexPolygon(Polygon P, double off) {
        if (P.areaSigned() < 0) {
            P.reverse();
        }
        GeometryCycle offset = new GeometryCycle();
        for (int i = 0; i < P.vertexCount(); i++) {
            // offset edge
            LineSegment e = P.edge(i - 1).clone();
            Vector offdir = e.getDirection();
            offdir.rotate90DegreesClockwise();
            offdir.scale(off);
            e.translate(offdir);
            offset.edges().add(e);
            // offset corner
            LineSegment ee = P.edge(i).clone();
            offdir = ee.getDirection();
            offdir.rotate90DegreesClockwise();
            offdir.scale(off);
            ee.translate(offdir);
            offset.edges().add(new CircularArc(P.vertex(i), e.getEnd(), ee.getStart(), true));
        }
        return offset;
    }

    public static GeometryCycle offsetConvexPolygon(Polygon P, double[] off) {
        if (P.areaSigned() < 0) {
            P.reverse();
        }
        GeometryCycle offset = new GeometryCycle();
        for (int i = 0; i < P.vertexCount(); i++) {
            // offset edge
            LineSegment e = P.edge(i - 1).clone();
            Vector offdir = e.getDirection();
            offdir.rotate90DegreesClockwise();
            offdir.scale(off[i]);
            e.translate(offdir);
            offset.edges().add(e);
            // offset corner
            LineSegment ee = P.edge(i).clone();
            offdir = ee.getDirection();
            offdir.rotate90DegreesClockwise();
            offdir.scale(off[i]);
            ee.translate(offdir);
            offset.edges().add(new CircularArc(P.vertex(i), e.getEnd(), ee.getStart(), true));
        }
        return offset;
    }


}
