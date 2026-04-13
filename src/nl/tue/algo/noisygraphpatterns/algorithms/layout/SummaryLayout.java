package nl.tue.algo.noisygraphpatterns.algorithms.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.Graph;
import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;
import nl.tue.algo.noisygraphpatterns.data.layout.Connector;
import nl.tue.algo.noisygraphpatterns.data.layout.DrawnPattern;
import nl.tue.algo.noisygraphpatterns.data.layout.Layout;
import nl.tue.geometrycore.geometry.OrientedGeometry;
import nl.tue.geometrycore.geometry.Vector;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

import javax.swing.*;
import java.util.*;

public class SummaryLayout extends LayoutAlgorithm {

    private int rounds = 100;
    private double iteration = 0.0;

    private double cliqueRotation = 0.8;
    private double connectorAttraction = 1;
    private double glyphRepulsion = 1;
    private double centerGravity = 1;
    private double gravityAlpha = 0.0;
    private double glyphScaling = 1.0;
    private double margin = 3;

    @Override
    protected void run(Graph graph, Layout layout) {
        iteration = 0.0;
        // initialize pattern glyphs
        Vector center;
        DrawnPattern dp;
        for (Pattern p : graph.patterns) {
            // compute center of pattern
            center = p.center();
            center.translate(0.5, -graph.vertexCount() + 0.5);
            center.scale(1, -1);
            // create glyph
            dp = layout.get(p);
            if (p.type == Pattern.PatternType.CLIQUE) {
//                List<DrawnPattern> componentPatterns = new ArrayList<>();
//                findComponent(layout, p , new boolean[graph.patterns.size()], componentPatterns);
//
//                Vector target = new Vector();
//                Vector centerOfGravity = new Vector();
//                double minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY;
//                for (DrawnPattern dp2 : componentPatterns) {
//                    if(dp.pattern.left < minx) {
//                        minx = dp2.pattern.left;
//                    }
//                    if (graph.vertexCount() - dp2.pattern.bottom < miny) {
//                        miny = graph.vertexCount() - dp2.pattern.bottom;
//                    }
//                }
//                target.translate(minx/2 + center.getX()/2, miny/2 + center.getY()/2);
                dp.initializeCircleGlyph(glyphScaling, center, layout);
            } else { // Pattern.PatternType.BICLIQUE
                dp.initializeDiamondGlyph(glyphScaling, center, layout);
            }
        }
        // initialize connectors for bicliques
        for (Pattern p : graph.patterns) {
            if(p.type == Pattern.PatternType.BICLIQUE &&
                    (!p.leftAdjacentCliques.isEmpty() || !p.bottomAdjacentCliques.isEmpty())) {
                // create connector
                List<List<Connector>> connectorLists;
                if (!p.leftAdjacentCliques.isEmpty()) {
                    for (Pattern p2 : p.leftAdjacentCliques) {
                        connectorLists = new ArrayList<>();
                        GeometryCycle shape = buildLeftConnector(p, p2, layout, connectorLists);
                        Connector connector = new Connector(p2, shape);
                        // check whether this biclique is part of connector
                        dp = layout.get(p);
//                    if(!dp.isLeftHyperedge()) {
                        connector.incidentPatterns.add(p);
//                    }
                        // set connector
                        dp.connectors.add(connector);

                        for (List<Connector> connectorList : connectorLists) {
                            connectorList.add(connector);
                        }
                    }

                }
                if (!p.bottomAdjacentCliques.isEmpty()) {
                    for (Pattern p2 : p.bottomAdjacentCliques) {
                        connectorLists = new ArrayList<>();
                        GeometryCycle shape = buildBottomConnector(p, p2, layout, connectorLists);
                        Connector connector = new Connector(p2, shape);
                        // check whether this biclique is part of connector
                        dp = layout.get(p);
//                    if(!dp.isBottomHyperedge()) {
                        connector.incidentPatterns.add(p);
//                    }
                        // set connector
                        dp.connectors.add(connector);

                        for (List<Connector> connectorList : connectorLists) {
                            connectorList.add(connector);
                        }
                    }
                }

            }
        }
        runIncrement(graph, layout);
    }

    @Override
    public boolean allowIncremental() {
        return true;
    }

    @Override
    protected void runIncrement(Graph graph, Layout layout) {

        int r = 0;

        Vector[] translations = new Vector[graph.patterns.size()];
        double[] angles = new double[graph.patterns.size()];

        while (r < rounds) {
            r++;
            iteration++;

            for (int i = 0; i < translations.length; i++) {
                translations[i] = new Vector();
                angles[i] = 0;
            }

            // compute forces
            computeCliqueRotation(graph, layout, angles);
            computeConnectorAttraction(graph, layout, translations);
            computeGlyphRepulsion(graph, layout, translations);
            computeCenterGravity(graph, layout, translations);

            // apply rotations and translations to cliques and bicliques
            for (int i = 0; i < graph.patterns.size(); i++) {
                Pattern p = graph.patterns.get(i);
                DrawnPattern dp = layout.get(p);

                translations[i].scale(1/(dp.radius));
                angles[i] /= dp.radius;

                if (translations[i].length() > layout.getMaxRadius()) {
                    translations[i].normalize();
                    translations[i].scale(layout.getMaxRadius()/2);
                }
                dp.translate(translations[i]);

                double reverseAngle = 1.0;
                if(p.type == Pattern.PatternType.BICLIQUE) {
                    reverseAngle = -1.0;
                }
                dp.rotate(reverseAngle * angles[i]);

                for (int j = 0; j < dp.vertexSets.size(); j++) {
                    OrientedGeometry boundary = dp.vertexSets.get(j);
                    Vector center = new Vector(dp.center);

                    // hyperedge bicliques have no incidentConnector (since they are on top of the connector)
//                    if(!dp.isLeftHyperedge()) {
                        // translate and rotate the incident connector
                        GeometryCycle connector = dp.incidentConnectors.get(j).shape;
                        int index = 0;
                        for (int k = 0; k < connector.vertexCount(); k++) {
                            Vector vertex = connector.vertex(k);
                            if (vertex.isApproximately(boundary.getStart())) { // boundary is not translated yet so this works
                                index = k;
                            }
                        }

                        Vector connectorVertex = connector.vertex(index);
                        // first translate, then rotate, because rotating around already translated dp.center
                        connectorVertex.translate(translations[i]);
                        connectorVertex.rotate(reverseAngle * angles[i], center);
                        if(index == connector.vertexCount()-1) {
                            connectorVertex = connector.vertex(0);
                            connectorVertex.translate(translations[i]);
                            connectorVertex.rotate(reverseAngle * angles[i], center);
                        } else {
                            connectorVertex = connector.vertex(index+1);
                            connectorVertex.translate(translations[i]);
                            connectorVertex.rotate(reverseAngle * angles[i], center);
                        }
//                    }

                    // translate and rotate boundary last because it is used in the earlier check
                    boundary.translate(translations[i]);
                    boundary.rotate(reverseAngle * angles[i], center);
                }
            }

            // translate hyperedge bicliques to their centroid
//            for (int i = 0; i < graph.patterns.size(); i++) {
//                Pattern p = graph.patterns.get(i);
//                DrawnPattern dp = layout.get(p);
//                if (dp.connector != null && dp.isLeftHyperedge()) { // p.type == Pattern.PatternType.BICLIQUE && dp.isHyperedge()
//                    GeometryCycle shape = dp.connector.shape;
//                    // find centroid of connector shape
//                    Vector corner;
//                    Vector avg = new Vector();
//                    for (int j = 0; j < shape.vertexCount(); j++) {
//                        corner = shape.vertex(j);
//                        avg.translate(corner.getX(), corner.getY());
//                    }
//                    avg.scale(1.0/shape.vertexCount());
//
//                    // set center of glyph to found centroid
//                    Vector translation = new Vector(avg);
//                    translation.translate(-dp.center.getX(), -dp.center.getY());
//
//                    dp.translate(translation);
//                    for(OrientedGeometry boundary : dp.vertexSets) {
//                        boundary.translate(translation);
//                    }
//                }
//            }
        }
    }

    @Override
    public String parameters() {
        return "[r=" + rounds + ";cr=" + cliqueRotation + ";ca=" + connectorAttraction + ";gr=" + glyphRepulsion + ";cg=" + centerGravity + ";gs=" + glyphScaling + "]";
    }

    @Override
    public void initParameterGUI(SideTab tab) {
        tab.addLabel("Rounds");
        tab.addIntegerSpinner(rounds, 0, Integer.MAX_VALUE, 1, (e, v) -> rounds = v);

        tab.addSeparator(1);

        tab.addLabel("Parameters");
        tab.makeSplit(4, 2);
        tab.addLabel("Clique Rotation");
        JSpinner rotation = tab.addDoubleSpinner(cliqueRotation, 0, 3, 0.01, (e, v) -> cliqueRotation = v);

        tab.makeSplit(4, 2);
        tab.addLabel("Connector Attraction");
        JSpinner attraction = tab.addDoubleSpinner(connectorAttraction, 0, 10, 0.1, (e, v) -> connectorAttraction = v);

        tab.makeSplit(4, 2);
        tab.addLabel("Glyph Repulsion");
        JSpinner repulsion = tab.addDoubleSpinner(glyphRepulsion, 0, 10, 0.1, (e, v) -> glyphRepulsion = v);

        tab.makeSplit(4, 2);
        tab.addLabel("Center Gravity");
        JSpinner gravity = tab.addDoubleSpinner(centerGravity, 0, 10, 0.1, (e, v) -> centerGravity = v);

        tab.makeSplit(4, 2);
        tab.addLabel("Gravity Alpha");
        JSpinner alpha = tab.addDoubleSpinner(gravityAlpha, 0, 1, 0.1, (e, v) -> centerGravity = v);

        tab.makeSplit(4, 2);
        tab.addLabel("Glyph Scaling");
        JSpinner scaling = tab.addDoubleSpinner(glyphScaling, 0.1, 3, 0.05, (e, v) -> glyphScaling = v);

        tab.makeSplit(4, 2);
        tab.addLabel("Interglyph Margin");
        JSpinner marg = tab.addDoubleSpinner(margin, 0.1, 20, 0.5, (e, v) -> margin = v);

        tab.addButton("Set Defaults", (e) -> {
            rotation.setValue(0.8);
            attraction.setValue(1.0);
            repulsion.setValue(1.0);
            gravity.setValue(1.0);
            alpha.setValue(0.0);
            scaling.setValue(1.0);
            marg.setValue(3.0);
        });
    }

    private GeometryCycle buildLeftConnector(Pattern p, Layout layout, List<List<Connector>> connectorLists) {
        ShapeBuilder sb = new ShapeBuilder();
        // create connector shape starting from first adjacent clique
        Pattern p2 = p.leftAdjacentCliques.get(0);
        int firstIndex, nextIndex, prevIndex;
        if (p.top > p2.top) { // p2 is adjacent to p so p2.bottom > p.top
            firstIndex = p.top;
        } else { // p.top <= p2.top
            firstIndex = p2.top;
        }
        Vector start = nextLeftGlyphBoundary(layout, p2, firstIndex, true);
        sb.start(start);

        if (p.bottom < p2.bottom) { // p2 is adjacent to p so p2.top < p.bottom
            nextIndex = p.bottom;
        } else { // p.bottom >= p2.bottom
            nextIndex = p2.bottom;
        }
        Vector end = nextLeftGlyphBoundary(layout, p2, nextIndex, false);
        sb.addAbsoluteSegment(end);
        // add arc at border of p2
        DrawnPattern dp = layout.get(p);
        DrawnPattern dp2 = layout.get(p2);
        dp2.addBoundary(start.clone(), end.clone(), false);
        connectorLists.add(dp2.incidentConnectors);

        // prepare to add arc to p for previous adjacent clique
        prevIndex = firstIndex;
        // then loop over remaining adjacent cliques from top to bottom
        for (int i = 1; i < p.leftAdjacentCliques.size(); i++) {
            // first add arc to p for previous adjacent clique
            start = nextLeftGlyphBoundary(layout, p, nextIndex, true);
            end = nextLeftGlyphBoundary(layout, p, prevIndex, false);
            dp.addBoundary(start.clone(), end.clone(), false);
            // then proceed with next adjacent clique
            p2 = p.leftAdjacentCliques.get(i);
            prevIndex = p2.top; // p2 must be adjacent to p and another clique is adjacent to p above p2
            start = nextLeftGlyphBoundary(layout, p2, prevIndex, true);
            sb.addAbsoluteSegment(start);

            if (p.bottom < p2.bottom) { // p2 is adjacent to p so p2.top < p.bottom
                nextIndex = p.bottom;
            } else { // p.bottom >= p2.bottom
                nextIndex = p2.bottom;
            }
            end = nextLeftGlyphBoundary(layout, p2, nextIndex, false);
            sb.addAbsoluteSegment(end);
            // add arc at border of p2
            dp2 = layout.get(p2);
            dp2.addBoundary(start.clone(), end.clone(), false);
            connectorLists.add(dp2.incidentConnectors);
        }
        // depending on whether the biclique interacts with multiple cliques, a different shape is made
        if (dp.isLeftHyperedge()) {
            // create last arc for p
            start = nextLeftGlyphBoundary(layout, p, nextIndex, true);
            end = nextLeftGlyphBoundary(layout, p, prevIndex, false);
            dp.addBoundary(start.clone(), end.clone(), false);
            // connect back to start
            sb.addAbsoluteSegment(sb.start());
        } else {
            start = nextLeftGlyphBoundary(layout, p, nextIndex, true);
            sb.addAbsoluteSegment(start);
            end = nextLeftGlyphBoundary(layout, p, firstIndex, false);
            sb.addAbsoluteSegment(end);
            // add boundary at border of p
            dp.addBoundary(start.clone(), end.clone(), false);
            connectorLists.add(dp.incidentConnectors);

            sb.addAbsoluteSegment(sb.start());
        }

        return sb.getResult();
    }

    private GeometryCycle buildLeftConnector(Pattern p, Pattern p2, Layout layout, List<List<Connector>> connectorLists) {
        ShapeBuilder sb = new ShapeBuilder();
        // create connector shape starting from first adjacent clique
        int firstIndex, nextIndex;
        if (p.top > p2.top) { // p2 is adjacent to p so p2.bottom > p.top
            firstIndex = p.top;
        } else { // p.top <= p2.top
            firstIndex = p2.top;
        }
        Vector start = nextLeftGlyphBoundary(layout, p2, firstIndex, true);
        sb.start(start);

        if (p.bottom < p2.bottom) { // p2 is adjacent to p so p2.top < p.bottom
            nextIndex = p.bottom;
        } else { // p.bottom >= p2.bottom
            nextIndex = p2.bottom;
        }
        Vector end = nextLeftGlyphBoundary(layout, p2, nextIndex, false);
        sb.addAbsoluteSegment(end);
        // add arc at border of p2
        DrawnPattern dp = layout.get(p);
        DrawnPattern dp2 = layout.get(p2);
        dp2.addBoundary(start.clone(), end.clone(), false);
        connectorLists.add(dp2.incidentConnectors);

        start = nextLeftGlyphBoundary(layout, p, nextIndex, true);
        sb.addAbsoluteSegment(start);
        end = nextLeftGlyphBoundary(layout, p, firstIndex, false);
        sb.addAbsoluteSegment(end);
        // add boundary at border of p
        dp.addBoundary(start.clone(), end.clone(), false);
        connectorLists.add(dp.incidentConnectors);

        sb.addAbsoluteSegment(sb.start());

        return sb.getResult();
    }

    private Vector nextLeftGlyphBoundary(Layout layout, Pattern p, int index, boolean start) {
        // set margin along glyph boundary depending on start
        double margin = 0.4, marginSign;
        if(start) {
            marginSign = -1.0;
        } else { // end of glyph boundary
            marginSign = 1.0;
        }
        DrawnPattern dp = layout.get(p);
        // find vector pointing from center of glyph to position of index
        Vector rotation;
        if (p.type == Pattern.PatternType.CLIQUE) {
            rotation = Vector.up();
            rotation.rotate(-2*Math.PI / (p.bottom - p.top + 1) * (index - p.top + 0.5 + marginSign * margin));
            rotation.scale(dp.radius);
        } else { // biclique counterclockwise because they connect vertical side and start right of cliques
            rotation = Vector.left();
            rotation.rotate(Math.PI/4);
            rotation.scale(dp.radius * Math.sqrt(2) / (p.bottom - p.top + 1) * (index - p.top + 0.5 - marginSign * margin));
            rotation.translate(0, dp.radius);
            // position for round glyphs
//            rotation.rotate(2*Math.PI/(p.right-p.left+1 + p.bottom-p.top+1) * (index - p.top + marginSign * margin));
        }
        // add this vector to center point to find absolute location of index
        Vector result = new Vector(dp.center);
        result.translate(rotation);
        return result;
    }

    private GeometryCycle buildBottomConnector(Pattern p, Layout layout, List<List<Connector>> connectorLists) {
        ShapeBuilder sb = new ShapeBuilder();
        // create connector shape starting from first adjacent clique
        Pattern p2 = p.bottomAdjacentCliques.get(0);
        int firstIndex, nextIndex, prevIndex;
        if (p.left > p2.left) { // p2 is adjacent to p so p2.right > p.left
            firstIndex = p.left;
        } else { // p.left <= p2.left
            firstIndex = p2.left;
        }
        Vector start = nextBottomGlyphBoundary(layout, p2, firstIndex, true);
        sb.start(start);

        if (p.right < p2.right) { // p2 is adjacent to p so p2.left < p.right
            nextIndex = p.right;
        } else { // p.right >= p2.right
            nextIndex = p2.right;
        }
        Vector end = nextBottomGlyphBoundary(layout, p2, nextIndex, false);
        sb.addAbsoluteSegment(end);
        // add arc at border of p2
        DrawnPattern dp = layout.get(p);
        DrawnPattern dp2 = layout.get(p2);
        dp2.addBoundary(start.clone(), end.clone(), false);
        connectorLists.add(dp2.incidentConnectors);

        // prepare to add arc to p for previous adjacent clique
        prevIndex = firstIndex;
        // then loop over remaining adjacent cliques from top to bottom
        for (int i = 1; i < p.bottomAdjacentCliques.size(); i++) {
            // first add arc to p for previous adjacent clique
            start = nextBottomGlyphBoundary(layout, p, nextIndex, true);
            end = nextBottomGlyphBoundary(layout, p, prevIndex, false);
            dp.addBoundary(start.clone(), end.clone(), false);
            // then proceed with next adjacent clique
            p2 = p.bottomAdjacentCliques.get(i);
            prevIndex = p2.left; // p2 must be adjacent to p and another clique is adjacent to p above p2
            start = nextBottomGlyphBoundary(layout, p2, prevIndex, true);
            sb.addAbsoluteSegment(start);

            if (p.right < p2.right) { // p2 is adjacent to p so p2.left < p.right
                nextIndex = p.right;
            } else { // p.right >= p2.right
                nextIndex = p2.right;
            }
            end = nextBottomGlyphBoundary(layout, p2, nextIndex, false);
            sb.addAbsoluteSegment(end);
            // add arc at border of p2
            dp2 = layout.get(p2);
            dp2.addBoundary(start.clone(), end.clone(), false);
            connectorLists.add(dp2.incidentConnectors);
        }
        // depending on whether the biclique interacts with multiple cliques, a different shape is made
        if (dp.isBottomHyperedge()) {
            // create last arc for p
            start = nextBottomGlyphBoundary(layout, p, nextIndex, true);
            end = nextBottomGlyphBoundary(layout, p, prevIndex, false);
            dp.addBoundary(start.clone(), end.clone(), false);
            // connect back to start
            sb.addAbsoluteSegment(sb.start());
        } else {
            start = nextBottomGlyphBoundary(layout, p, nextIndex, true);
            sb.addAbsoluteSegment(start);
            end = nextBottomGlyphBoundary(layout, p, firstIndex, false);
            sb.addAbsoluteSegment(end);
            // add boundary at border of p
            dp.addBoundary(start.clone(), end.clone(), false);
            connectorLists.add(dp.incidentConnectors);

            sb.addAbsoluteSegment(sb.start());
        }

        return sb.getResult();
    }

    private GeometryCycle buildBottomConnector(Pattern p, Pattern p2, Layout layout, List<List<Connector>> connectorLists) {
        ShapeBuilder sb = new ShapeBuilder();
        // create connector shape starting from first adjacent clique
        int firstIndex, nextIndex, prevIndex;
        if (p.left > p2.left) { // p2 is adjacent to p so p2.right > p.left
            firstIndex = p.left;
        } else { // p.left <= p2.left
            firstIndex = p2.left;
        }
        Vector start = nextBottomGlyphBoundary(layout, p2, firstIndex, true);
        sb.start(start);

        if (p.right < p2.right) { // p2 is adjacent to p so p2.left < p.right
            nextIndex = p.right;
        } else { // p.right >= p2.right
            nextIndex = p2.right;
        }
        Vector end = nextBottomGlyphBoundary(layout, p2, nextIndex, false);
        sb.addAbsoluteSegment(end);
        // add arc at border of p2
        DrawnPattern dp = layout.get(p);
        DrawnPattern dp2 = layout.get(p2);
        dp2.addBoundary(start.clone(), end.clone(), false);
        connectorLists.add(dp2.incidentConnectors);

        start = nextBottomGlyphBoundary(layout, p, nextIndex, true);
        sb.addAbsoluteSegment(start);
        end = nextBottomGlyphBoundary(layout, p, firstIndex, false);
        sb.addAbsoluteSegment(end);
        // add boundary at border of p
        dp.addBoundary(start.clone(), end.clone(), false);
        connectorLists.add(dp.incidentConnectors);

        sb.addAbsoluteSegment(sb.start());

        return sb.getResult();
    }

    private Vector nextBottomGlyphBoundary(Layout layout, Pattern p, int index, boolean start) {
        // set margin along glyph boundary depending on start
        double margin = 0.4, marginSign;
        if(start) {
            marginSign = -1.0;
        } else { // end of glyph boundary
            marginSign = 1.0;
        }
        DrawnPattern dp = layout.get(p);
        // find vector pointing from center of glyph to position of index
        Vector rotation;
        if (p.type == Pattern.PatternType.CLIQUE) {
            rotation = Vector.up();
            rotation.rotate(-2*Math.PI / (p.right - p.left + 1) * (index - p.left + 0.5 + marginSign * margin));
            rotation.scale(dp.radius);
        } else { // biclique counterclockwise because they connect vertical side and start right of cliques
            rotation = Vector.right();
            rotation.rotate(Math.PI/4);
            rotation.scale(dp.radius * Math.sqrt(2) / (p.right - p.left + 1) * (index - p.left + 0.5 - marginSign * margin));
            rotation.translate(0, -dp.radius);
            // position for round glyphs
//            rotation.rotate(2*Math.PI/(p.right-p.left+1 + p.bottom-p.top+1) * (index - p.top + marginSign * margin));
        }
        // add this vector to center point to find absolute location of index
        Vector result = new Vector(dp.center);
        result.translate(rotation);
        return result;
    }

    private void computeCliqueRotation(Graph graph, Layout layout, double[] angles) {
        for (int i = 0; i < graph.patterns.size(); i++) {
            Pattern p = graph.patterns.get(i);
            DrawnPattern dp = layout.get(p);
//            if (p.type == Pattern.PatternType.CLIQUE || (p.type == Pattern.PatternType.BICLIQUE && !dp.isLeftHyperedge())) {
                for (int j = 0; j < dp.vertexSets.size(); j++) {
                    OrientedGeometry boundary = dp.vertexSets.get(j);

                    // Rotate arc to centroid of connected (bi)-cliques
                    Connector connector = dp.incidentConnectors.get(j);
                    Vector centroid = new Vector();
                    for( int k = 0; k < connector.getPatternCount(); k++) {
                        centroid.translate(layout.get(connector.incidentPatterns.get(k)).center);
                    }
                    centroid.scale(1.0/connector.getPatternCount());
                    centroid.translate(-dp.center.getX(), -dp.center.getY());

                    Vector arcMid = new Vector();
                    arcMid.translate(boundary.getStart().getX() - dp.center.getX(), boundary.getStart().getY() - dp.center.getY());
                    arcMid.translate(boundary.getEnd().getX() - dp.center.getX(), boundary.getEnd().getY() - dp.center.getY());

                    double centralAngle;
                    if(p.type == Pattern.PatternType.CLIQUE) {
                        if (dp.centralAngle(boundary, dp.radius, false)>Math.PI || dp.centralAngle(boundary, dp.radius, false)<-Math.PI) {
                            arcMid.rotate(Math.PI);
                        }
                        centralAngle = dp.centralAngle(boundary, dp.radius, false);
                    } else { // p.type == Pattern.PatternType.BICLIQUE
                        centralAngle = dp.centralAngle(boundary, dp.radius, true);
                    }

                    angles[i] += cliqueRotation * centroid.computeSignedAngleTo(arcMid) * centralAngle/(2*Math.PI);
                    if (angles[i] < -Math.PI) {
                        angles[i] += 2*Math.PI;
                    } else if (angles[i] > Math.PI) {
                        angles[i] -= 2*Math.PI;
                    }

                }
//            }
        }
    }

    private void computeConnectorAttraction(Graph graph, Layout layout, Vector[] changes) {
        DrawnPattern dp1, dp2;
        Vector centroid;
        for (Pattern p : graph.patterns) {
            dp1 = layout.get(p);
            for (Connector connector : dp1.incidentConnectors) {
                centroid = new Vector();
                GeometryCycle shape = connector.shape;
                for( int k = 0; k < shape.vertexCount(); k++) {
                    centroid.translate(shape.vertex(k));
                }
                centroid.scale(1.0/shape.vertexCount());
//                dp2 = layout.get(connector.incidentPatterns.get(0));
//                if(dp1.pattern.index == dp2.pattern.index) {
//                    dp2 = layout.get(connector.incidentPatterns.get(1));
//                }
//                centroid.translate(dp2.center);

                centroid.translate(-dp1.center.getX(), -dp1.center.getY());
                centroid.normalize();

                centroid.scale(connectorAttraction);
                changes[p.index].translate(centroid);
            }
        }
    }

    private void computeGlyphRepulsion(Graph graph, Layout layout, Vector[] changes) {
        double length;
        for (int i = 0; i < graph.patterns.size(); i++) {
            Pattern p1 = graph.patterns.get(i);
            DrawnPattern dp1 = layout.get(p1);

            for (int j = i+1; j < graph.patterns.size(); j++) {
                Pattern p2 = graph.patterns.get(j);
                DrawnPattern dp2 = layout.get(p2);

                // repulse glyphs that are not on connectors
                Vector repulsion = new Vector();
                repulsion.translate(dp1.center.getX() - dp2.center.getX(), dp1.center.getY() - dp2.center.getY());
                length = repulsion.length();
                repulsion.normalize();
//                if (dist(dp1.center, dp2.center) >= 0.2) {
//                    repulsion.scale((dp1.radius + dp2.radius) / (dist(dp1.center, dp2.center) * dist(dp1.center, dp2.center)));
//                }


                // cliques and bicliques in same connector repulse harder
//                if (//(p2.bottomAdjacentCliques.contains(p1) || p2.leftAdjacentCliques.contains(p1)) &&
//                        dist(dp1.center, dp2.center) < (dp1.radius + dp2.radius) + 2 * margin) {
//                    length = repulsion.length();
//                    repulsion.normalize();
//                    repulsion.scale(length + (dp1.radius + dp2.radius) + 2 * margin - dist(dp1.center, dp2.center));
//                }

//                if (dist(dp1.center, dp2.center) < (dp1.radius + dp2.radius) + margin) {
//                    length = repulsion.length();
//                    repulsion.normalize();
//                    repulsion.scale(length + (dp1.radius + dp2.radius) + margin - dist(dp1.center, dp2.center));
//                }
//                else if (dist(dp1.center, dp2.center) > (dp1.radius + dp2.radius) + 2 * margin) {
//                    repulsion = new Vector();
//                }

//                if (p1.type == Pattern.PatternType.CLIQUE) {
//                if ((p2.bottomAdjacentCliques.contains(p1) || p2.leftAdjacentCliques.contains(p1)) ) {
                    repulsion.scale(glyphRepulsion * Math.pow((dp1.radius + dp2.radius + margin) / (length), 3));
//                } else {
//                    repulsion.scale(glyphRepulsion * Math.pow((dp1.radius + dp2.radius) / (length), 3));
//                }

//                } else { // bicliques repulse weaker
//                    repulsion.scale(glyphRepulsion / 2);
//                }
                //scale up the repulsion over first 1000 iterations
//                repulsion.scale(Math.min(iteration/2000, 1));



                changes[i].translate(repulsion);
                repulsion.invert();
                changes[j].translate(repulsion);
            }
        }
    }

    private void computeCenterGravity(Graph graph, Layout layout, Vector[] changes) {
        Pattern p;
        Vector translation, target, centerOfGravity;

        boolean[] discovered = new boolean[graph.patterns.size()];
        List<DrawnPattern> componentPatterns;

        for (int i = 0; i < graph.patterns.size(); i++) {
            p = graph.patterns.get(i);

            // do a dfs to find all cliques that are connected via bicliques by connectors
            if (p.type == Pattern.PatternType.CLIQUE && !discovered[p.index]) {
                componentPatterns = new ArrayList<>();
                findComponent(layout, p , discovered, componentPatterns);

                target = new Vector();
                centerOfGravity = new Vector();
                double minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY;
                for (DrawnPattern dp : componentPatterns) {
                    target.translate(dp.pattern.center().getX(), graph.vertexCount() - dp.pattern.center().getY());
                    centerOfGravity.translate(dp.center.getX(), dp.center.getY());
                    if(dp.pattern.left < minx) {
                        minx = dp.pattern.left;
                    }
                    if (graph.vertexCount() - dp.pattern.bottom < miny) {
                        miny = graph.vertexCount() - dp.pattern.bottom;
                    }
                }
                target.scale(1.0 / componentPatterns.size());
                centerOfGravity.scale(1.0 / componentPatterns.size());

                for (DrawnPattern dp : componentPatterns) {
//                    target = new Vector(minx, miny);
//                    translation = new Vector(target.getX()-dp.center.getX(), target.getY() - dp.center.getY());
                    translation = new Vector((1 - gravityAlpha) * (target.getX()-centerOfGravity.getX()) + gravityAlpha * (target.getX()-dp.center.getX()),
                            (1 - gravityAlpha) * (target.getY() - centerOfGravity.getY()) + gravityAlpha * (target.getY()-dp.center.getY()));
//                    translation = new Vector(target.getX() - centerOfGravity.getX(), target.getY() - centerOfGravity.getY());

                    translation.normalize();
                    translation.scale(centerGravity);
                    //scale up the gravity over first 1000 iterations
//                    translation.scale(Math.min(iteration/2000, 1));
                    changes[dp.pattern.index].translate(translation);

                    if(translation.length() > 50) {
                        System.out.println("High gravity");
                    }
                }
            } else if (p.type == Pattern.PatternType.BICLIQUE) {
                target = p.center();
                DrawnPattern dp = layout.get(p);

                translation = new Vector(target.getX()-dp.center.getX(), graph.vertexCount() - target.getY() - dp.center.getY());
                translation.normalize();
                if (dp.incidentConnectors.isEmpty()) {
                    translation.scale(centerGravity);
                } else { // bicliques with connectors have less gravity to connect cliques better
                    translation.scale(centerGravity/5);
                }
                //scale up the gravity over first 1000 iterations
//                translation.scale(Math.min(iteration/2000, 1));
                changes[i].translate(translation);

                if(translation.length() > 50) {
                    System.out.println("High gravity");
                }
            }
        }
    }

    private void findComponent(Layout layout, Pattern p, boolean[] discovered, List<DrawnPattern> componentPatterns) {
        ArrayList<Pattern> stack = new ArrayList<>();
        Pattern currentP, biclique;
        DrawnPattern currentDP;
        stack.add(p);
        while (!stack.isEmpty()) {
            currentP = stack.removeLast();
            if (!discovered[currentP.index]) {
                discovered[currentP.index] = true;
                currentDP = layout.get(currentP);
                componentPatterns.add(currentDP);
                // find other cliques through connectors
                for (Connector connector : currentDP.incidentConnectors) {
                    // works only without hyperedge connectors!
                    // biclique always last in incidentPattern list
                    biclique = connector.incidentPatterns.getLast();
                    stack.addAll(biclique.leftAdjacentCliques);
                    stack.addAll(biclique.bottomAdjacentCliques);
                }
            }
        }
    }

    private double dist(Vector a, Vector b) {
        double xSquare = Math.pow(a.getX()-b.getX(), 2);
        double ySquare = Math.pow(a.getY()-b.getY(), 2);
        return Math.sqrt(xSquare+ySquare);
    }
}
