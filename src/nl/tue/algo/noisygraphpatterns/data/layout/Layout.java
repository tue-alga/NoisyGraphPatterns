/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.algo.noisygraphpatterns.data.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.*;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.linear.Rectangle;

/**
 *
 * @author wmeulema
 */
public class Layout {

    private final Graph graph;
    private final DrawnVertex[] vertices;
    private final DrawnEdge[][] edges;
    private final DrawnPattern[] patterns;
    private final DrawnEdgeSummary[][] edgeSummaries;

    private double width, height;
    private double dx, dy;
    private double minRadius, maxRadius;
    
    public BaseGeometry debug;

    public Layout(Graph graph) {
        this.graph = graph;

        int n = graph.vertexCount();
        vertices = new DrawnVertex[n];
        for (int i = 0; i < n; i++) {
            vertices[i] = new DrawnVertex(graph.vertices[i]);
        }
        edges = new DrawnEdge[n][n];
        for (int c = 0; c < n; c++) {
            for (int r = c; r < n; r++) {
                edges[c][r] = edges[r][c] = new DrawnEdge(graph.edges[c][r]);
            }
        }
        patterns = new DrawnPattern[graph.patterns.size()];
        for (int p = 0; p < patterns.length; p++) {
            patterns[p] = new DrawnPattern(graph.patterns.get(p));
        }
        edgeSummaries = new DrawnEdgeSummary[graph.patterns.size()][graph.patterns.size()];
        for (int c = 0; c < graph.patterns.size(); c++) {
            for (int r = c; r < graph.patterns.size(); r++) {
                edgeSummaries[c][r] = new DrawnEdgeSummary(graph.edgeSummaries[c][r]);
            }
        }
    }

    public void computeMetaData() {
        width = 0;
        height = 0;
        for (DrawnVertex v : vertices) {
            if(v.position != null) {
                width = Math.max(width, v.position.getX());
            }
        }
        int n = graph.vertexCount();
        for (int c = 0; c < n; c++) {
            for (int r = c; r < n; r++) {
                if (edges[c][r].route != null) {
                    width = Math.max(width, Rectangle.byBoundingBox(edges[c][r].route).getRight());
                }
            }
        }

        double minX = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (DrawnPattern dp : patterns) {
            if (dp.shape != null) {
                if (dp.center.getX() - dp.radius < minX) {
                    minX = dp.center.getX() - dp.radius;
                }
                if (dp.center.getY() + dp.radius > maxY) {
                    maxY = dp.center.getY() + dp.radius;
                }

                width = Math.max(width, dp.center.getX() + dp.radius);
                height = Math.max(height, dp.center.getY() + dp.radius);
            }
        }
        dy = maxY;
        dx = minX;

        for (int c = 0; c < graph.patterns.size(); c++) {
            for (int r = c; r < graph.patterns.size(); r++) {
                if (edgeSummaries[c][r].route != null) {
                    width = Math.max(width, Rectangle.byBoundingBox(edgeSummaries[c][r].route).getRight());
                }
            }
        }
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double dx() {
        return dx;
    }

    public double dy() {
        return dy;
    }

    public double getMinRadius() {
        return minRadius;
    }

    public double getMaxRadius() {
        return maxRadius;
    }

    public DrawnVertex get(Vertex v) {
        return vertices[v.index];
    }

    public DrawnEdge get(Edge e) {
        return edges[e.from.index][e.to.index];
    }

    public DrawnPattern get(Pattern p) {
        return patterns[p.index];
    }

    public DrawnEdgeSummary get(EdgeSummary s) {
        return edgeSummaries[s.from.index][s.to.index];
    }

    public void newRadius(double radius) {
        if(radius < minRadius) {
            minRadius = radius;
        }
        if(radius > maxRadius) {
            maxRadius = radius;
        }
    }

}
