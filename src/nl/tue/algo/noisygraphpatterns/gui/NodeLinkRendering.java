package nl.tue.algo.noisygraphpatterns.gui;

import java.awt.*;
import java.util.Arrays;

import nl.tue.algo.noisygraphpatterns.data.graph.*;
import nl.tue.algo.noisygraphpatterns.data.layout.*;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.curved.Circle;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;

public class NodeLinkRendering {

    final double scale;

    public NodeLinkRendering(double scale) {
        this.scale = scale;
    }

    public void render(GeometryRenderer draw, Graph graph, Layout layout, double dx, boolean horizontal) {
        draw.setSizeMode(SizeMode.WORLD);

        double dy = 0;

        draw.setAlpha(0.5);
        for (int c = 0; c < graph.patterns.size(); c++) {
            Pattern p = graph.patterns.get(c);

            // draw connecting shapes of bicliques
            BaseGeometry shape;
            for (Connector connector : layout.get(p).connectors) {
                if (connector != null) {
                    draw.setStroke(null, 1, Dashing.SOLID);
                    draw.setFill(Color.lightGray, Hashures.SOLID);

                    shape = connector.shape.clone();
                    shape.scale(scale);
                    shape.translate(dx, dy);
                    draw.draw(shape);
                }
            }

        }
        draw.setAlpha(1);

        for (int c = 0; c < graph.patterns.size(); c++) {
            Pattern p = graph.patterns.get(c);
            DrawnPattern dp = layout.get(p);
            // draw glyphs for cliques and bicliques
            BaseGeometry shape = dp.shape;
            if (shape == null) {
                continue;
            }
            draw.setStroke(null, 1, Dashing.SOLID);

            shape = shape.clone();
            shape.scale(scale);
            shape.translate(dx, dy);
            // cover connecting shapes first, then draw glyph
            draw.setFill(Color.white, Hashures.SOLID);
            draw.setAlpha(1);
            draw.draw(shape);
            draw.setFill(p.color, Hashures.SOLID);
            draw.setAlpha(0.5);
            draw.draw(shape);

            draw.setAlpha(1);

            // draw hole in pattern glyphs
            shape = dp.negativeShape;
            if(shape != null) {
                draw.setStroke(null, 1, Dashing.SOLID);
                draw.setFill(Color.white, Hashures.SOLID);

                shape = shape.clone();
                shape.scale(scale);
                shape.translate(dx, dy);
                draw.draw(shape);
            }

            // draw pronounced boundaries at connectors
            for (OrientedGeometry boundary : dp.vertexSets) {
                shape = boundary.clone();
                shape.scale(scale);
                shape.translate(dx, dy);

                draw.setStroke(Color.white, 8, Dashing.SOLID);
                draw.setFill(p.color, Hashures.SOLID);
                draw.draw(shape);

                draw.setStroke(p.color, 4, Dashing.SOLID);
                draw.setFill(p.color, Hashures.SOLID);
                draw.draw(shape);
            }


            for (int r = c+1; r < graph.patterns.size(); r++) {
                Pattern q = graph.patterns.get(r);
                DrawnEdgeSummary es = layout.get(new EdgeSummary(graph, p, q));
                if (es.route == null) {
                    continue;
                }

                BaseGeometry route = es.route.clone();
                route.scale(scale);
                route.translate(dx, 0);
                draw.setAlpha(es.summary.density());

                draw.setStroke(ExtendedColors.black, 0.8 + 3 * (es.summary.density()), Dashing.SOLID);
                draw.draw(route);

                draw.setAlpha(1);
            }
        }

        if(horizontal) {
            // draw special glyph for vertices not in patterns
            double left = 0.5, right = graph.vertexCount() - 0.5, bottom = -0.05 * graph.vertexCount(), top = -0.01 * graph.vertexCount();
            double midLeft, midRight;
            double inEdges = 0, inNotEdges = 0, possibleEdges = (double) (graph.vertexCount() * graph.vertexCount() - graph.vertexCount()) / 2;
            for (Pattern p : graph.patterns) {
                if (p.type == Pattern.PatternType.CLIQUE) {
                    inEdges += ((double) p.edges) / 2;
                    inNotEdges += (p.possibleEdges() - p.edges) / 2;
                } else { // p.type == Pattern.PatternType.BICLIQUE
                    inEdges += (double) p.edges;
                    inNotEdges += (p.possibleEdges() - p.edges);
                }
            }

            draw.setStroke(null, 1, Dashing.SOLID);
            // not in pattern and not edge
            draw.setFill(Color.white, Hashures.SOLID);
            midRight = left + (possibleEdges - graph.edgeCount() - inNotEdges) / possibleEdges * (right - left);
            BaseGeometry shape = new Rectangle(left, midRight, bottom, top);
            shape.scale(scale);
            shape.translate(dx, 0);
            draw.draw(shape);
            // in pattern but not edge
            draw.setFill(Color.lightGray, Hashures.SOLID);
            midLeft = midRight;
            midRight = midLeft + inNotEdges / possibleEdges * (right - left);
            shape = new Rectangle(midLeft, midRight, bottom, top);
            shape.scale(scale);
            shape.translate(dx, 0);
            draw.draw(shape);
            // in pattern edge
            draw.setFill(Color.darkGray, Hashures.SOLID);
            midLeft = midRight;
            midRight = midLeft + inEdges / possibleEdges * (right - left);
            shape = new Rectangle(midLeft, midRight, bottom, top);
            shape.scale(scale);
            shape.translate(dx, 0);
            draw.draw(shape);
            // not in pattern edge
            draw.setFill(Color.red, Hashures.SOLID);
            midLeft = midRight;
            midRight = midLeft + (graph.edgeCount() - inEdges) / possibleEdges * (right - left);
            shape = new Rectangle(midLeft, midRight, bottom, top);
            shape.scale(scale);
            shape.translate(dx, 0);
            draw.draw(shape);
            // outline
            draw.setStroke(Color.black, 0.5, Dashing.SOLID);
            draw.setFill(null, Hashures.SOLID);
            shape = new Rectangle(left, right, bottom, top);
            shape.scale(scale);
            shape.translate(dx, 0);
            draw.draw(shape);
        }

        boolean[] hasHidden = new boolean[graph.vertexCount()];
        Arrays.fill(hasHidden, false);

        for (Edge e : graph.allEdges()) {
            DrawnEdge de = layout.get(e);
            if (de.route == null) {
                continue;
            }
            BaseGeometry route = de.route.clone();
            route.scale(scale);
            route.translate(dx, 0);

            if (e.exists) {
                draw.setStroke(ExtendedColors.black, 0.8, Dashing.SOLID);
            } else {
                draw.setStroke(ExtendedColors.darkRed, 0.8, Dashing.dotted(0.8));
            }
            
            draw.draw(route);
        }

        draw.setTextStyle(TextAnchor.CENTER, scale * 0.5);
        for (Vertex v : graph.vertices) {
            DrawnVertex dv = layout.get(v);
            if(dv.position == null) {
                continue;
            }
            Vector loc = dv.position.clone();
            loc.scale(scale);
            loc.translate(dx, 0);

            if (v.complete) {
                draw.setStroke(ExtendedColors.black, 0.8, Dashing.SOLID);
                draw.setFill(ExtendedColors.black, Hashures.SOLID);
            } else {
                draw.setStroke(ExtendedColors.black, 0.8, Dashing.SOLID);
                draw.setFill(ExtendedColors.lightGray, Hashures.SOLID);
            }

            draw.draw(new Circle(loc, scale * 0.4));
            
            if (v.complete) {                
                draw.setStroke(ExtendedColors.lightGray, 0.8, Dashing.SOLID);
            } else {
                draw.setStroke(ExtendedColors.black, 0.8, Dashing.SOLID);
                
            }
            
            draw.draw(loc, "" + (v.index + 1));
        }
    }
}
