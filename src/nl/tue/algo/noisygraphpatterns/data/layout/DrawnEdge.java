/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.algo.noisygraphpatterns.data.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.Edge;
import nl.tue.geometrycore.geometry.OrientedGeometry;

/**
 *
 * @author wmeulema
 */
public class DrawnEdge {

    public final Edge edge;
    public OrientedGeometry route;

    public DrawnEdge(Edge edge) {
        this.edge = edge;
    }

}
