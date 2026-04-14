package nl.tue.algo.noisygraphpatterns.gui;

import nl.tue.algo.noisygraphpatterns.data.graph.Edge;
import nl.tue.algo.noisygraphpatterns.data.graph.Graph;
import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.linear.LineSegment;
import nl.tue.geometrycore.geometry.linear.Polygon;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.geometryrendering.GeometryRenderer;
import nl.tue.geometrycore.geometryrendering.styling.*;

import java.util.ArrayList;

public class BioFabricRendering {

    final double scale;

    public BioFabricRendering(double scale) {
        this.scale = scale;
    }

    public void render(GeometryRenderer draw, Graph graph, double dx) {
        draw.setSizeMode(SizeMode.WORLD);

        int n = graph.vertexCount();
        int m = graph.edgeCount();

        // draw vertex lines with labels
        LineSegment line;
        Vector position;
        for (int r = 0; r < n; r++) {
            draw.setStroke(ExtendedColors.darkGray, 0.4, Dashing.SOLID);
            draw.setTextStyle(TextAnchor.RIGHT, scale * 0.75);
            position = new Vector(-0.1, n - r - 0.5);
            position = Vector.multiply(scale, position);
            position.translate(dx, 0);
            draw.draw(position, "" + (graph.vertices[r].id + 1));

            draw.setStroke(ExtendedColors.lightGray, 3, Dashing.SOLID);
            line = new LineSegment(new Vector(0, scale * (n - r - 0.5)), new Vector(scale * m+1, scale * (n - r - 0.5)));
            line.translate(dx, 0);
            draw.draw(line);
        }

        // draw edges
        double i = 0.5; // "cursor position for edge x-coordinate"
        Vector top, bottom;
        Rectangle endPoint;
        Edge e;
        for (int r = 0; r < n; r++) {
            for (int c = r; c < n; c++) {
                e = graph.edges[c][r];
                if (e.exists) {
                    // draw biofabric edge
                    // first draw white casing of edge
                    draw.setStroke(ExtendedColors.white, 6, Dashing.SOLID);
                    top = new Vector(scale * i, scale * (n - r - 0.5));
                    bottom = new Vector(scale * i, scale * (n - c - 0.5));
                    line = new LineSegment(top, bottom);
                    line.translate(dx, 0);
                    draw.draw(line);
                    // draw endpoints as small rectangles, with casing
                    draw.setStroke(ExtendedColors.white, 2, Dashing.SOLID);
                    if (e.inPattern) {
                        draw.setFill(e.pattern.color, Hashures.SOLID);
                    } else {
                        draw.setFill(ExtendedColors.black, Hashures.SOLID);
                    }
                    endPoint = endPoint(top);
                    draw.draw(endPoint);
                    endPoint = endPoint(bottom);
                    draw.draw(endPoint);
                    // draw line last, so that it goes over endpoint casing
                    if (e.inPattern) {
                        draw.setStroke(e.pattern.color, 3, Dashing.SOLID);
                    } else {
                        draw.setStroke(ExtendedColors.black, 3, Dashing.SOLID);
                    }
                    draw.draw(line);
                    // move "cursor"
                    i++;
                }

            }
        }
    }

    public void renderMotif(GeometryRenderer draw, Graph graph, double dx, double dy) {
        draw.setSizeMode(SizeMode.WORLD);

        int n = graph.vertexCount();
        int m = graph.edgeCount();

        // draw vertex lines with labels
        LineSegment line;
        Vector position;
        for (int r = 0; r < n; r++) {
            draw.setStroke(ExtendedColors.darkGray, 0.4, Dashing.SOLID);
            draw.setTextStyle(TextAnchor.RIGHT, scale * 0.75);
            position = new Vector(-0.1, n - r - 0.5);
            position = Vector.multiply(scale, position);
            position.translate(dx, dy);
            draw.draw(position, "" + (graph.vertices[r].id + 1));

            draw.setStroke(ExtendedColors.lightGray, 3, Dashing.SOLID);
            line = new LineSegment(new Vector(0, scale * (n - r - 0.5)), new Vector(scale * m+1, scale * (n - r - 0.5)));
            line.translate(dx, dy);
            draw.draw(line);
        }

        // draw edges and/or patterns
        double i = 0.5; // "cursor position for edge x-coordinate"
        double sideLength, margin;
        Vector top, bottom;
        Rectangle rectangle;
        Edge e;
        boolean[] patternDrawn = new boolean[graph.patterns.size()];
        ArrayList<Integer> starEdges, starPatternEdges;
        Polygon starShape;
        Pattern p;
        for (int r = 0; r < n; r++) {
            starEdges = new ArrayList<>();
            for (int c = r; c < n; c++) {
                e = graph.edges[c][r];
                if (e.inPattern) {
                    p = e.pattern;
                    if (!patternDrawn[p.index]) {
                        // draw glyph for pattern
                        if (p.type == Pattern.PatternType.BICLIQUE && p.top == p.bottom) {
                            // for horizontal stars
                            // find edges
                            starPatternEdges = new ArrayList<>();
                            for (int j = p.left; j <= p.right; j++) {
                                starPatternEdges.add(j);
                            }
                            // draw star motif downwards
                            starShape = drawStar(starPatternEdges, i, r, n);
                            draw.setStroke(p.color, 3, Dashing.SOLID);
                            draw.setFill(p.color, Hashures.SOLID);
                            starShape.translate(dx, dy);
                            draw.draw(starShape);

                            // draw biofabric edges in star motif
                            for (Integer column : starPatternEdges) {
                                top = new Vector(scale * i, scale * (n - r - 0.5));
                                bottom = new Vector(scale * i, scale * (n - column - 0.5));

                                draw.setFill(ExtendedColors.black, Hashures.SOLID);
                                draw.setStroke(p.color, 2, Dashing.SOLID);
                                rectangle = endPoint(top, 0.8);
                                rectangle.translate(dx, dy);
                                draw.draw(rectangle);
                                rectangle = endPoint(bottom, 0.8);
                                rectangle.translate(dx, dy);
                                draw.draw(rectangle);

                                draw.setStroke(ExtendedColors.black, 3, Dashing.SOLID);
                                line = new LineSegment(top, bottom);
                                line.translate(dx, dy);
                                draw.draw(line);
                                // move "cursor"
                                i++;
                            }
                            patternDrawn[p.index] = true;
                        } else if (p.type == Pattern.PatternType.BICLIQUE && p.left == p.right) {
                            // for vertical stars
                            starPatternEdges = new ArrayList<>();
                            for (int j = p.top; j <= p.bottom; j++) {
                                starPatternEdges.add(j);
                            }
                            // draw star motif downwards
                            starShape = drawStarUpwards(starPatternEdges, i, c, n);
                            draw.setStroke(p.color, 3, Dashing.SOLID);
                            draw.setFill(p.color, Hashures.SOLID);
                            starShape.translate(dx, dy);
                            draw.draw(starShape);

                            // draw biofabric edges in star motif
                            for (Integer row : starPatternEdges) {
                                top = new Vector(scale * i, scale * (n - row - 0.5));
                                bottom = new Vector(scale * i, scale * (n - c - 0.5));

                                draw.setFill(ExtendedColors.black, Hashures.SOLID);
                                draw.setStroke(p.color, 2, Dashing.SOLID);
                                rectangle = endPoint(top, 0.8);
                                rectangle.translate(dx, dy);
                                draw.draw(rectangle);
                                rectangle = endPoint(bottom, 0.8);
                                rectangle.translate(dx, dy);
                                draw.draw(rectangle);

                                draw.setStroke(ExtendedColors.black, 3, Dashing.SOLID);
                                line = new LineSegment(top, bottom);
                                line.translate(dx, dy);
                                draw.draw(line);
                                // move "cursor"
                                i++;
                            }
                            patternDrawn[p.index] = true;
                        } else {
                            // bicliques and cliques
                            if (p.type == Pattern.PatternType.BICLIQUE && p.left < p.right && p.top < p.bottom) {
                                // for "normal" bicliques
                                for (int j = p.left; j <= p.right; j++) {
                                    // draw biofabric edge
                                    // first draw white casing of edge
                                    draw.setStroke(ExtendedColors.white, 6, Dashing.SOLID);
                                    top = new Vector(scale * (i + j - p.left), scale * (n - p.bottom - 0.5));
                                    bottom = new Vector(scale * (i + j - p.left), scale * (n - j - 0.5));
                                    line = new LineSegment(top, bottom);
                                    line.translate(dx, dy);
                                    draw.draw(line);
                                    // draw endpoints as small rectangles, with casing
                                    draw.setStroke(ExtendedColors.white, 2, Dashing.SOLID);
                                    if (e.inPattern) {
                                        draw.setFill(e.pattern.color, Hashures.SOLID);
                                    } else {
                                        draw.setFill(ExtendedColors.black, Hashures.SOLID);
                                    }
                                    rectangle = endPoint(bottom);
                                    draw.draw(rectangle);
                                    // draw line last, so that it goes over endpoint casing
                                    draw.setStroke(p.color, 3, Dashing.SOLID);
                                    draw.draw(line);
                                }
                            }
                            // both bicliques and cliques
                            draw.setStroke(p.color, 1, Dashing.SOLID);
                            draw.setFill(p.color, Hashures.SOLID);
                            rectangle = new Rectangle(scale * (i - 0.3), scale * (i + p.right - p.left + 0.3), scale * (n - p.bottom - 0.8), scale * (n - p.top - 0.2));
                            rectangle.translate(dx, dy);
                            draw.draw(rectangle);
                            // draw hole to indicate missing edges
                            draw.setStroke(ExtendedColors.white, 2, Dashing.SOLID);
                            draw.setFill(ExtendedColors.white, Hashures.SOLID);
                            rectangle.scale((p.possibleEdges() - p.edges) / p.possibleEdges(), rectangle.center());
                            if(p.possibleEdges() - p.edges > 0) {
                                draw.draw(rectangle);
                            }
                            // prep for next draws
                            patternDrawn[p.index] = true;
                            i += p.right - p.left + 1;
                        }
                    }
                } else if (e.exists) {
                    starEdges.add(c);
                }
            }
            if (starEdges.size() > 1) {
                starShape = drawStar(starEdges, i, r, n);
                draw.setStroke(ExtendedColors.gray, 3, Dashing.SOLID);
                draw.setFill(ExtendedColors.gray, Hashures.SOLID);
                starShape.translate(dx, dy);
                draw.draw(starShape);
                // draw biofabric edges in star motif
                for (Integer c : starEdges) {
                    top = new Vector(scale * i, scale * (n - r - 0.5));
                    bottom = new Vector(scale * i, scale * (n - c - 0.5));

                    draw.setFill(ExtendedColors.black, Hashures.SOLID);
                    draw.setStroke(ExtendedColors.gray, 2, Dashing.SOLID);
                    rectangle = endPoint(top, 0.8);
                    rectangle.translate(dx, dy);
                    draw.draw(rectangle);
                    rectangle = endPoint(bottom, 0.8);
                    rectangle.translate(dx, dy);
                    draw.draw(rectangle);

                    draw.setStroke(ExtendedColors.black, 3, Dashing.SOLID);
                    line = new LineSegment(top, bottom);
                    line.translate(dx, dy);
                    draw.draw(line);
                    // move "cursor"
                    i++;
                }
            } else if (starEdges.size() == 1) {
                // draw normal biofabric edge
                // first draw white casing of edge
                draw.setStroke(ExtendedColors.white, 6, Dashing.SOLID);
                top = new Vector(scale * i, scale * (n - r - 0.5));
                bottom = new Vector(scale * i, scale * (n - starEdges.get(0) - 0.5));
                line = new LineSegment(top, bottom);
                line.translate(dx, dy);
                draw.draw(line);
                // draw endpoints as small rectangles, with casing
                draw.setStroke(ExtendedColors.white, 2, Dashing.SOLID);
                draw.setFill(ExtendedColors.black, Hashures.SOLID);
                rectangle = endPoint(top);
                draw.draw(rectangle);
                rectangle = endPoint(bottom);
                draw.draw(rectangle);
                // draw line last, so that it goes over endpoint casing
                draw.setStroke(ExtendedColors.black, 3, Dashing.SOLID);
                draw.draw(line);
                // move "cursor"
                i++;
            }
        }
    }

    private Rectangle endPoint(Vector center) {
        return endPoint(center, 1.0);
    }

    private Rectangle endPoint(Vector center, double scaling) {
        return new Rectangle(center.getX() - scale * 0.3 * scaling, center.getX() + scale * 0.3 * scaling,
                center.getY() - scale * 0.3 * scaling, center.getY() + scale * 0.3 * scaling);
    }

    private Polygon drawStar(ArrayList<Integer> starEdges, double i, int r, int n) {
        ArrayList<Vector> points = new ArrayList<>();
        int counter = 0;

        points.add(new Vector(scale * (i - 0.25), scale * (n - r - 0.25)));
        for (Integer c : starEdges) {
            points.add(new Vector(scale * (i + counter - 0.25), scale * (n - c - 0.75)));
            counter++;
        }
        points.add(new Vector(scale * (i + counter - 1 + 0.25), scale * (n - starEdges.getLast() - 0.75)));
        points.add(new Vector(scale * (i + counter - 1 + 0.25), scale * (n - r - 0.25)));
        return new Polygon(points);
    }

    private Polygon drawStarUpwards(ArrayList<Integer> starEdges, double i, int c, int n) {
        ArrayList<Vector> points = new ArrayList<>();
        int counter = 0;

        points.add(new Vector(scale * (i - 0.25), scale * (n - c - 0.75)));
        points.add(new Vector(scale * (i - 0.25), scale * (n - starEdges.getFirst() - 0.25)));
        for (Integer r : starEdges) {
            points.add(new Vector(scale * (i + counter + 0.25), scale * (n - r - 0.25)));
            counter++;
        }
        points.add(new Vector(scale * (i + counter - 1 + 0.25), scale * (n - c - 0.75)));
        return new Polygon(points);
    }
}
