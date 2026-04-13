/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.algo.noisygraphpatterns.data.graph;

import java.util.*;

/**
 *
 * @author wmeulema
 */
public class Graph {

    public String name;
    public List<Pattern> patterns = new ArrayList();
    public EdgeSummary[][] edgeSummaries;
    public final Vertex[] vertices;
    public final Edge[][] edges;
    public final Set<Vertex> notInPattern = new HashSet();

    private int edgeCount = 0;
    private double moransI, moransMean;

    public Graph(String name, int size, int patternNum) {
        this.name = name;
        vertices = new Vertex[size];
        edges = new Edge[size][size];
        edgeSummaries = new EdgeSummary[patternNum][patternNum];

        for (int i = 0; i < size; i++) {
            vertices[i] = new Vertex(this, i);
        }

        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                edges[i][j] = edges[j][i] = new Edge(this, vertices[i], vertices[j]);
            }
        }
    }

    public Graph(Graph graph, int[] ordering) {
        this.name = graph.name;
        int size = graph.vertexCount();

        vertices = new Vertex[size];
        edges = new Edge[size][size];
        edgeSummaries = null;

        for (int i = 0; i < size; i++) {
            vertices[i] = new Vertex(this, i, ordering[i]);
        }

        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                edges[i][j] = edges[j][i] = new Edge(this, vertices[i], vertices[j]);
            }
        }

        // set edges according to input graph and input ordering
        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                if (graph.edges[ordering[i]][ordering[j]].exists) {
                    addEdge(i, j);
                }
            }
        }

        computeMoransI();
    }

    public void addEdge(int c, int r) {
        if (!edges[c][r].exists) {
            edges[c][r].exists = true;
            edgeCount++;
        }

    }

    public void reindexPatterns() {
        for (int i = 0; i < patterns.size(); i++) {
            patterns.get(i).setIndex(i);
        }
    }

    public void postProcess() {
        computeMoransI();

        Collections.sort(patterns);
        reindexPatterns();

        edgeSummaries = new EdgeSummary[patterns.size()][patterns.size()];
        for (int i = 0; i < patterns.size(); i++) {
            Pattern p = patterns.get(i);

            for (int j = 0; j < patterns.size(); j++) {
                // initialize edgeSummaries
                if (j > i) {
                    edgeSummaries[i][j] = new EdgeSummary(this, patterns.get(i), patterns.get(j));
                }
                // find adjacent bicliques for cliques
                if (p.type == Pattern.PatternType.CLIQUE) {
                    Pattern p2 = patterns.get(j);
                    if (p2.type == Pattern.PatternType.BICLIQUE ) {
                        if (p2.bottom >= p.top && p2.top <= p.bottom) {
                            p2.leftAdjacentCliques.add(p);
                        } else if (p2.right >= p.left && p2.left <= p.right) {
                            p2.bottomAdjacentCliques.add(p);
                        }
                    }
                }
            }
        }

        for (Edge e : allEdges()) {
            e.inPattern = false;
            Vertex u = e.from;
            Vertex v = e.to;

            for (int c = 0; c<patterns.size(); c++) {
                Pattern p = patterns.get(c);
                if (p.suggests(e)) {
                    e.inPattern = true;
                    e.pattern = p;
                }
            }
            if (!e.inPattern && e.exists) {
                for (int c = 0; c < patterns.size(); c++) {
                    for (int r = c + 1; r < patterns.size(); r++) {
                        Pattern p = patterns.get(c);
                        Pattern q = patterns.get(r);

                        if (p.involves(u) && q.involves(v)) {
                            e.summarizable = true;
                            edgeSummaries[p.index][q.index].edges.add(e);
                        } else if (p.involves(v) && q.involves(u)) {
                            e.summarizable = true;
                            edgeSummaries[p.index][q.index].edges.add(e);
                        }
                    }
                }
            }
        }

        for (Vertex v : vertices) {
            // does v have an edge in a pattern that does not exist?
            v.complete = !edges(v, (e) -> e.from != e.to && !e.exists && e.inPattern).iterator().hasNext();
            // find vertices in no pattern

            notInPattern.add(v);
            for (Pattern p : patterns) {
                if(p.involves(v)) {
                    notInPattern.remove(v);
                }
            }
        }
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
        postProcess();
    }

    public interface EdgeFilter {

        boolean accept(Edge e);
    }

    public Iterable<Edge> allEdges() {
        return edges((e) -> true);
    }

    public Iterable<Edge> existingEdges() {
        return edges((e) -> e.exists);
    }

    public Iterable<Edge> nonExistingEdges() {
        return edges((e) -> !e.exists);
    }

    public Iterable<Edge> patternEdges() {
        return edges((e) -> e.inPattern);
    }

    public Iterable<Edge> nonPatternEdges() {
        return edges((e) -> !e.inPattern);
    }

    public Iterable<Edge> allEdges(Vertex v) {
        return edges(v, (e) -> true);
    }

    public Iterable<Edge> existingEdges(Vertex v) {
        return edges(v, (e) -> e.exists);
    }

    public Iterable<Edge> nonExistingEdges(Vertex v) {
        return edges(v, (e) -> !e.exists);
    }

    public Iterable<Edge> patternEdges(Vertex v) {
        return edges(v, (e) -> e.inPattern);
    }

    public Iterable<Edge> nonPatternEdges(Vertex v) {
        return edges(v, (e) -> !e.inPattern);
    }

    public Iterable<Edge> edges(Vertex v, EdgeFilter filter) {
        final int col = v.index;
        int row = 0;
        while (row < edges.length && !filter.accept(edges[col][row])) {
            row++;
        }
        final int first_row = row;
        return () -> new Iterator<Edge>() {
            int row = first_row;

            @Override
            public boolean hasNext() {
                return row < edges.length;
            }

            @Override
            public Edge next() {
                Edge e = edges[col][row];
                row++;
                while (row < edges.length && !filter.accept(edges[col][row])) {
                    row++;
                }
                return e;
            }
        };
    }

    public Iterable<Edge> edges(EdgeFilter filter) {
        int row = 0;
        int col = 0;
        while (col < edges.length && !filter.accept(edges[col][row])) {
            row++;
            if (row >= edges.length) {
                col++;
                row = col;
            }
        }
        final int first_row = row;
        final int first_col = col;

        return () -> new Iterator<Edge>() {
            int row = first_row;
            int col = first_col;

            @Override
            public boolean hasNext() {
                return col < edges.length;
            }

            @Override
            public Edge next() {
                Edge e = edges[col][row];
                row++;
                if (row >= edges.length) {
                    col++;
                    row = col;
                }
                while (col < edges.length && !filter.accept(edges[col][row])) {
                    row++;
                    if (row >= edges.length) {
                        col++;
                        row = col;
                    }
                }
                return e;
            }
        };
    }

    public int vertexCount() {
        return vertices.length;
    }

    public int edgeCount() {
        return edgeCount;
    }

    public void computeMoransI() {
        // calculate mean
        double n = vertexCount(), m = 2 * edgeCount();
        moransMean = m / (n * n);

        // count black-black and white-white adjacencies
        int[] d1 = {0, 1}; //, 0, -1};
        int[] d2 = {1, 0};//, -1, 0};
        double B = 0, W = 0;
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < n; j++)
            {
                for (int k = 0; k < d1.length; k++)
                {
                    if (i + d1[k] < 0 || i + d1[k] >= n)
                        continue;
                    if (j + d2[k] < 0 || j + d2[k] >= n)
                        continue;
                    if (edges[i][j].exists && edges[i+d1[k]][j+d2[k]].exists)
                        B++;
                    if (!edges[i][j].exists && !edges[i+d1[k]][j+d2[k]].exists)
                        W++;
                }
            }
        }

        // multiply by coefficients and subtract 1
        moransI = B * (n / (2 * (n-1) * m)) + W * (n / (2 * (n-1) * (n * n - m))) - 1;
    }

    public double getMoransI() {
        return moransI;
    }

    public double moransITSPCost(int j, int k) {
//        double result = 0.0;
//        double aij, aik;
//        for (int i = 0; i < vertexCount(); i++) {
//            // find aij depending on whether edge i j exists
//            if (edges[i][j].exists) {
//                aij = 1 - moransMean;
//            } else {
//                aij = -moransMean;
//            }
//            // find aik depending on whether edge i k exists
//            if (edges[i][k].exists) {
//                aik = 1 - moransMean;
//            } else {
//                aik = -moransMean;
//            }
//            // add multiplication of values to sum
//            result += (aij * aik);
//        }
//        // negate sum
//        result = -result + 35; // add small constant to make everything positive (too large and NEOS is inefficient)
//        return result;
        double n = vertexCount(), m = 2 * edgeCount();
        double B = 0, W = 0;
        for (int i = 0; i < edges.length; i++) {
            if (edges[i][j].exists && edges[i][k].exists) {
                B++;
            }
            if (!edges[i][j].exists && !edges[i][k].exists) {
                W++;
            }
        }

        return (1 - (B * (n / (2 * (n-1) * m)) + W * (n / (2 * (n-1) * (n * n - m))) ));
    }
}
