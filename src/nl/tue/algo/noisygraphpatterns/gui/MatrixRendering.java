package nl.tue.algo.noisygraphpatterns.gui;

import nl.tue.algo.noisygraphpatterns.data.graph.Graph;
import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;
import nl.tue.geometrycore.geometry.BaseGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.geometryrendering.styling.Dashing;
import nl.tue.geometrycore.geometryrendering.styling.ExtendedColors;
import nl.tue.geometrycore.geometryrendering.styling.Hashures;
import nl.tue.geometrycore.geometryrendering.styling.SizeMode;
import nl.tue.geometrycore.geometryrendering.styling.TextAnchor;

import java.awt.*;

public class MatrixRendering {

    final double scale;

    public MatrixRendering(double scale) {
        this.scale = scale;
    }

    public void render(GeometryRenderer draw, Graph graph, double dx, boolean drawOrdering, boolean drawPatterns, boolean layoutAbsent, boolean drawPrecisionBar, boolean verticalBar) {
        draw.setSizeMode(SizeMode.WORLD);

        int n = graph.vertices.length;
        Rectangle R;

        draw.setStroke(ExtendedColors.darkGray, 0.4, Dashing.SOLID);

        Vector anchor;
        for (int r = 0; r < n; r++) {
            if (drawOrdering) {
                draw.setTextStyle(TextAnchor.RIGHT, scale * 0.75);
                anchor = Vector.multiply(scale, new Vector(-0.1, n - r - 0.5));
                anchor.translate(dx, 0);
                draw.draw(anchor, "" + (graph.vertices[r].id + 1));

                draw.setTextStyle(TextAnchor.BASELINE_CENTER, scale * 0.75);
                anchor = Vector.multiply(scale, new Vector(r + 0.5, n + 0.1));
                anchor.translate(dx, 0);
                draw.draw(anchor, "" + (graph.vertices[r].id + 1));
            }

            for (int c = r; c < n; c++) {

                if (c == r) {
                    draw.setFill(ExtendedColors.darkGray, Hashures.SOLID);
                } else if (graph.edges[c][r].exists) {
                    draw.setFill(ExtendedColors.black, Hashures.SOLID);
                } else {
                    draw.setFill(ExtendedColors.white, Hashures.SOLID);
                }

                R = cell(c, r, n);
                R.translate(dx, 0);
                draw.draw(R);
                if (r != c) {
                    R = cell(r, c, n);
                    R.translate(dx, 0);
                    draw.draw(R);
                }

            }
        }

        if(drawPatterns) {
            for (Pattern p : graph.patterns) {
                draw.setStroke(null, 1, Dashing.SOLID);
                draw.setFill(p.color, Hashures.SOLID);
                draw.setAlpha(0.5);
                R = Rectangle.byBoundingBox(cell(p.left, p.top, n), cell(p.right, p.bottom, n));
                R.translate(dx, 0);
                draw.draw(R);
                if (p.type == Pattern.PatternType.BICLIQUE) {
                    R = Rectangle.byBoundingBox(cell(p.top, p.left, n), cell(p.bottom, p.right, n));
                    R.translate(dx, 0);
                    draw.draw(R);
                }
                draw.setAlpha(1);
            }
        }

        if(drawPrecisionBar) {
            // draw special glyph for vertices not in patterns
            if (verticalBar) {
                double left = 1.03 * graph.vertexCount(), right = 1.07 * graph.vertexCount() - 0.5, bottom = 0.5, top = graph.vertexCount() - 0.5;
                double midTop, midBottom;
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
                midBottom = top - (possibleEdges - graph.edgeCount() - inNotEdges) / possibleEdges * (top - bottom);
                BaseGeometry shape = new Rectangle(left, right, midBottom, top);
                shape.scale(scale);
                shape.translate(dx, 0);
                draw.draw(shape);
                // in pattern but not edge
                draw.setFill(Color.lightGray, Hashures.SOLID);
                midTop = midBottom;
                midBottom = midTop - inNotEdges / possibleEdges * (top - bottom);
                shape = new Rectangle(left, right, midBottom, midTop);
                shape.scale(scale);
                shape.translate(dx, 0);
                draw.draw(shape);
                // in pattern edge
                draw.setFill(Color.darkGray, Hashures.SOLID);
                midTop = midBottom;
                midBottom = midTop - inEdges / possibleEdges * (top - bottom);
                shape = new Rectangle(left, right, midBottom, midTop);
                shape.scale(scale);
                shape.translate(dx, 0);
                draw.draw(shape);
                // not in pattern edge
                draw.setFill(Color.red, Hashures.SOLID);
                midTop = midBottom;
                midBottom = midTop - (graph.edgeCount() - inEdges) / possibleEdges * (top - bottom);
                shape = new Rectangle(left, right, midBottom, midTop);
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
            } else if (layoutAbsent) {
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
        }
    }

    private Rectangle cell(int column, int row, int n) {
        return Rectangle.byCornerAndSize(Vector.multiply(scale, new Vector(column, n - row - 1)), scale, scale);
    }

}
