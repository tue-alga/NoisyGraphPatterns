/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.algo.noisygraphpatterns.data.graph;

/**
 *
 * @author wmeulema
 */
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
