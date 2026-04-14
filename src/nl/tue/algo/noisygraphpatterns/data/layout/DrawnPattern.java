package nl.tue.algo.noisygraphpatterns.data.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.curved.CircularArc;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.util.DoubleUtil;

import java.util.ArrayList;
import java.util.List;

public class DrawnPattern {

    public final Pattern pattern;
    public BaseGeometry shape, negativeShape;
    public Vector center;
    public double radius;
    public List<OrientedGeometry> vertexSets = new ArrayList();
    public List<Connector> connectors = new ArrayList();
    // incidentConnectors is ordered such that vertexSets.get(i) is connected by incidentConnectors.get(i)
    public List<Connector> incidentConnectors = new ArrayList();

    public DrawnPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean isLeftHyperedge() {
        return pattern.leftAdjacentCliques.size() > 1;
    }

    public boolean isBottomHyperedge() {
        return pattern.bottomAdjacentCliques.size() > 1;
    }

    public boolean isHyperedge() {
        return pattern.leftAdjacentCliques.size() > 1 || pattern.bottomAdjacentCliques.size() > 1;
    }

    public double centralAngle(OrientedGeometry boundary, double radius, boolean counterclockwise) {
        if (center == null) {
            return 0;
        }

        if (pattern.type == Pattern.PatternType.CLIQUE) {
            CircularArc arc = (CircularArc) boundary;
            return arc.centralAngle();
        }

        double xStart = (boundary.getStart().getX() - center.getX()) / radius;
        double yStart = (boundary.getStart().getY() - center.getY()) / radius;

        double xEnd = (boundary.getEnd().getX() - center.getX()) / radius;
        double yEnd = (boundary.getEnd().getY() - center.getY()) / radius;

        double angle = Math.asin(DoubleUtil.clipValue(
                Vector.crossProduct(xStart, yStart, xEnd, yEnd), -1, 1));

        // angle is now in range [-pi,pi]
        // check if we must make adjustments via dot product and angle
        if (Vector.dotProduct(xStart, yStart, xEnd, yEnd) < 0) {
            angle = Math.PI - angle;
        } else if (angle < 0) {
            angle = 2 * Math.PI + angle;
        }

        // now we have the CCW angle from start to end
        if (counterclockwise) {
            return angle;
        } else {
            // arc is the complement: central angle is (2pi - angle)
            // and multiply by -1 as its clockwise!
            return angle - 2 * Math.PI;
        }
    }

    public void initializeCircleGlyph(double glyphScaling, Vector center, Layout l) {
        radius = glyphScaling * Math.sqrt(pattern.possibleEdges() / Math.PI);
        shape = new Circle(center.clone(), radius);
        if(pattern.edges != pattern.possibleEdges()) {
            negativeShape = shape.clone();
            negativeShape.scale((pattern.possibleEdges() - pattern.edges)/pattern.possibleEdges(), center);
        }
        this.center = center.clone();
        // keep track of radii in layout
        l.newRadius(radius);
    }

    public void initializeDiamondGlyph(double glyphScaling, Vector center, Layout l) {
        radius = glyphScaling * Math.sqrt(pattern.possibleEdges()) / Math.sqrt(2);
        shape = createDiamondGlyph(center.clone(), radius);
        if(pattern.edges != pattern.possibleEdges()) {
            negativeShape = shape.clone();
            negativeShape.scale((pattern.possibleEdges() - pattern.edges) / pattern.possibleEdges(), center);
        }
        this.center = center.clone();
        // keep track of radii in layout
        l.newRadius(radius);
    }

    public void initializeTriangleGlyph(double glyphScaling, Vector center, Layout l) {
        radius = glyphScaling * Math.sqrt(pattern.possibleEdges() / (1.0/2 + (1.0/4) * Math.sqrt(3)) );
        shape = createTriangleGlyph(center.clone(), radius);
        if(pattern.edges != pattern.possibleEdges()) {
            negativeShape = shape.clone();
            negativeShape.scale((pattern.possibleEdges() - pattern.edges) / pattern.possibleEdges(), center);
        }
        this.center = center.clone();
        // keep track of radii in layout
        l.newRadius(radius);
    }

    private BaseGeometry createDiamondGlyph(Vector center, double radius) {
        Vector[] corners = new Vector[4];
        corners[0] = new Vector(center.getX() + radius, center.getY());
        corners[1] = new Vector(center.getX(), center.getY() - radius);
        corners[2] = new Vector(center.getX() - radius, center.getY());
        corners[3] = new Vector(center.getX(), center.getY() + radius);

        return new Polygon(corners);
    }

    private BaseGeometry createTriangleGlyph(Vector center, double radius) {
        Vector[] corners = new Vector[4];
        corners[0] = new Vector(center.getX(), center.getY() + radius);
        corners[1] = new Vector(center.getX() + radius * Math.cos(-Math.PI/6),
                center.getY() + radius * Math.sin(-Math.PI/6));
        corners[2] = new Vector(center.getX() + radius * Math.cos(Math.PI + Math.PI/6),
                center.getY() + + radius * Math.sin(Math.PI + Math.PI/6));

        Polygon triangle = new Polygon(corners);
        triangle.rotate(-Math.PI/2, center);
        return triangle;
    }

    public void translate(Vector translation) {
        shape.translate(translation);
        if(negativeShape != null) {
            negativeShape.translate(translation);
        }
        center.translate(translation);
    }

    public void rotate(double angle) {
        shape.rotate(angle, center);
        if(negativeShape != null) {
            negativeShape.rotate(angle, center);
        }
    }

    public void addBoundary(Vector start, Vector end, boolean counterclockwise) {
        if (pattern.type == Pattern.PatternType.CLIQUE) {
            vertexSets.add(new CircularArc(center.clone(), start.clone(), end.clone(), counterclockwise));
        } else {
            vertexSets.add(new LineSegment(start, end));
        }
    }
}
