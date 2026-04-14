package nl.tue.algo.noisygraphpatterns.data.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;

public class Pattern implements Comparable {

    public enum PatternType {
        CLIQUE, BICLIQUE;
    }

    public static int n = 1;
    static Color[] scheme = ExtendedColors.paired;

    public final Graph graph;
    public int index;
    public Color color;
    public final int top, bottom, left, right, edges, edgePairs;
    public final PatternType type;
    public List<Pattern> leftAdjacentCliques = new ArrayList();
    public List<Pattern> bottomAdjacentCliques = new ArrayList();

    public static Pattern clique(Graph graph, int from, int to) {
        return new Pattern(graph, PatternType.CLIQUE, from, to, from, to);
    }

    public static Pattern biclique(Graph graph, int left, int right, int top, int bottom) {
        return new Pattern(graph, PatternType.BICLIQUE, left, right, top, bottom);
    }

    private Pattern(Graph graph, PatternType type, int left, int right, int top, int bottom) {
        color = scheme[n];
        n += 2;
        if (n == scheme.length) {
            n -= scheme.length;
            n++;
        } else if (n > scheme.length) {
            n -= scheme.length;
            n--;
        }
        this.top = top;
        this.right = right;
        this.left = left;
        this.bottom = bottom;
        this.type = type;
        this.graph = graph;
        this.index = graph.patterns.size();

        int edgeCount = 0, edgePairs = 0;
        for (int c=left; c<=right; c++) {
            for (int r=top; r<=bottom; r++) {
                if (graph.edges[c][r].exists) {
                    edgeCount++;
                    if (c < right && graph.edges[c+1][r].exists) {
                        edgePairs++;
                    }
                    if (r < bottom && graph.edges[c][r+1].exists) {
                        edgePairs++;
                    }
                }
            }
        }
        this.edges = edgeCount;
        this.edgePairs = edgePairs;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean suggests(Edge e) {
        int row = e.from.index;
        int col = e.to.index;
        return (left <= col && col <= right && top <= row && row <= bottom)
                || (left <= row && row <= right && top <= col && col <= bottom);
    }

    public boolean involves(Vertex v) {

        return (left <= v.index && v.index <= right)
                || (top <= v.index && v.index <= bottom);
    }

    public Vector center() {
        return new Vector(left+(right-left)/2, bottom+(top-bottom)/2);
    }

    public double possibleEdges() {
        double result = (right-left+1)*(bottom-top+1);
        if (type == PatternType.CLIQUE) {
            result -= (right - left+1); // do not count diagonal
        }
        return result;
    }

    public double edges() {
        return edges;
    }

    public double density() {
        return edges / possibleEdges();
    }

    public int weight() {
//        return (bottom - top + 1) * (right - left + 1);
        return edgePairs;
    }

    public Iterable<Vertex> vertices() {
        return () -> new Iterator<Vertex>() {
            int i = top;

            @Override
            public boolean hasNext() {
                return i <= right;
            }

            @Override
            public Vertex next() {
                Vertex v = graph.vertices[i];
                i++;
                if (i > bottom && i < left) {
                    i = left;
                }
                return v;
            }
        };
    }

    public Iterable<Vertex> verticalVertices() {
        return () -> new Iterator<Vertex>() {
            int i = top;

            @Override
            public boolean hasNext() {
                return i <= bottom;
            }

            @Override
            public Vertex next() {
                Vertex v = graph.vertices[i];
                i++;
                return v;
            }
        };
    }

    public Iterable<Vertex> horizontalVertices() {
        return () -> new Iterator<Vertex>() {
            int i = left;

            @Override
            public boolean hasNext() {
                return i <= right;
            }

            @Override
            public Vertex next() {
                Vertex v = graph.vertices[i];
                i++;
                return v;
            }
        };
    }

    public int blackAdjacencies() {
        return edgePairs;
    }

    public void assignColor() {
        this.color = scheme[n];
        n += 2;
        if (n == scheme.length) {
            n -= scheme.length;
            n++;
        } else if (n > scheme.length) {
            n -= scheme.length;
            n--;
        }
    }

    public boolean intersect(Pattern p) {
        return (this.top <= p.bottom && this.bottom >= p.top && this.left <= p.right && this.right >= p.left);
    }

    @Override
    public int compareTo(Object object) {
        Pattern p = (Pattern) object;
        if (Integer.compare(left, p.left) == 0) {
            return Integer.compare(p.top, top); // Is this below p?
        } else {
            return Integer.compare(left, p.left); // Is this left of p?
        }
    }
}
