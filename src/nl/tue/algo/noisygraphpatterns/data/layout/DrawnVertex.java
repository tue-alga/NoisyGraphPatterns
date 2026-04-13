/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.algo.noisygraphpatterns.data.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.Vertex;
import nl.tue.geometrycore.geometry.Vector;

/**
 *
 * @author wmeulema
 */
public class DrawnVertex {

    public final Vertex vertex;
    public Vector position = null;

    public DrawnVertex(Vertex vertex) {
        this.vertex = vertex;
    }

}
