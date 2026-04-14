package nl.tue.algo.noisygraphpatterns.data.graph;

public class Edge {

    public final Vertex from, to;
    public boolean exists = false;
    public boolean inPattern = false;
    public Pattern pattern;
    public boolean summarizable = false;
    public final Graph graph;

    public Edge(Graph graph, Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
        this.graph = graph;
    }
}
