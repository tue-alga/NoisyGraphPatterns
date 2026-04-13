/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.algo.noisygraphpatterns.algorithms.layout;

import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;

/**
 *
 * @author wmeulema
 */
public class ShapeBuilder {

    private GeometryCycle gc = new GeometryCycle();
    private Vector start;
    private Vector curr;

    public void start(Vector v) {
        start = curr = v;
    }

    public Vector start() {
        return start;
    }

    public Vector current() {
        return curr;
    }

    public GeometryCycle getResult() {
        return gc;
    }

    public void addRelativeSegment(Vector dir) {
        Vector next = Vector.add(curr, dir);
        gc.edges().add(new LineSegment(curr, next));
        curr = next;
    }
    
    public void addAbsoluteSegment(Vector next) {
        gc.edges().add(new LineSegment(curr, next));
        curr = next;
    }
    
    public void addRelativeArc(Vector dir) {
        Vector next = Vector.add(curr, dir);
        gc.edges().add(CircularArc.fromStartTangent(curr, gc.edge(gc.edgeCount()-1).getEndTangent(), next));
        curr = next;
    }
    
    public void addRelativeArc(Vector dir, Vector tan) {
        Vector next = Vector.add(curr, dir);
        gc.edges().add(CircularArc.fromStartTangent(curr, tan, next));
        curr = next;
    }
}
