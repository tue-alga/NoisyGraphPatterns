/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.algo.noisygraphpatterns.data.graph;

/**
 *
 * @author wmeulema
 */
public class Vertex implements Comparable {

    public final int index;
    public final int id;
    public final Graph graph;
    public boolean complete;

    public Vertex(Graph graph, int index, int id) {
        this.graph = graph;
        this.index = index;
        this.id = id;
    }

    public Vertex(Graph graph, int index) {
        this(graph, index, index);
    }

    @Override
    public int compareTo(Object o) {
        Vertex other = (Vertex) o;
        return Integer.compare(index, other.index);
    }
}
