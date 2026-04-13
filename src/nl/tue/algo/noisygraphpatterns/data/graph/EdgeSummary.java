package nl.tue.algo.noisygraphpatterns.data.graph;

import java.util.ArrayList;
import java.util.List;

public class EdgeSummary {

    public final Pattern from, to;
    public List<Edge> edges = new ArrayList();
    public final Graph graph;

    public EdgeSummary(Graph graph, Pattern from, Pattern to) {
        this.graph = graph;
        this.from = from;
        this.to = to;
    }

    public double density() {
        int possibleEdges = 0;
        int fromVert = from.bottom - from.top + 1;
        int fromHor = from.right - from.left + 1;
        int toVert = to.bottom - to.top + 1;
        int toHor = to.right - to.left + 1;

        if (from.type == Pattern.PatternType.CLIQUE && to.type == Pattern.PatternType.CLIQUE) {
            possibleEdges += fromVert * toHor;
        } else if (to.type == Pattern.PatternType.BICLIQUE) {
            // Note: from.right < to.left
            int overlap = 0;
            if (to.top < from.top && from.top <= to.bottom) { // if to.bottom < from.top, then no overlap
                // biclique top is above from top
                if (to.bottom < from.bottom) {
                    // biclique bottom is between from top and bottom
                    overlap = to.bottom - from.top + 1;
                } else { // from.bottom <= to.bottom
                    // biclique bottom is below from bottom
                    overlap = from.bottom - from.top + 1;
                }

            } else if (from.top <= to.top && to.top <= from.bottom) {
                // biclique top is between from top and bottom
                if (to.bottom < from.bottom) {
                    // biclique top and bottom between from top and bottom
                    overlap = to.bottom - to.top + 1;
                } else { // from.bottom <= to.bottom
                    // biclique bottom is below from bottom
                    overlap =  from.bottom - to.top + 1;
                }
            }
            fromVert -= overlap;
            toVert -= overlap;

            if (from.type == Pattern.PatternType.BICLIQUE) {
                int overlap2 = 0;
                if (to.top < from.left && from.left <= to.bottom) { // if to.bottom < from.left, then no overlap
                    // biclique top is above from top
                    if (to.bottom < from.right) {
                        // biclique bottom is between from top and bottom
                        overlap2 = to.bottom - from.left + 1;
                    } else { // from.bottom <= to.bottom
                        // biclique bottom is below from bottom
                        overlap2 = from.right - from.left + 1;
                    }

                } else if (from.left <= to.top && to.top <= from.right) {
                    // biclique top is between from top and bottom
                    if (to.bottom < from.right) {
                        // biclique top and bottom between from top and bottom
                        overlap2 = to.bottom - to.top + 1;
                    } else { // from.bottom <= to.bottom
                        // biclique bottom is below from bottom
                        overlap2 =  from.right - to.top + 1;
                    }
                }

                possibleEdges += (to.bottom - to.top + 1) * (from.right - from.left + 1) - (overlap2 * overlap2 + overlap2)/2;

                // different sizes of bicliques also interact
                // from above diagonal
                int fromVert2 = from.bottom - from.top + 1;
                int fromHor2 = from.right - from.left + 1 - overlap2;
                // to under diagonal (horizontal and vertical swapped)
                int toVert2 = to.right - to.left + 1;
                int toHor2 = to.bottom - to.top + 1 - overlap2;

                possibleEdges += fromVert2 * toHor2 + toVert2 * fromHor2;
            }

            possibleEdges += fromVert * toHor + toVert * fromHor;
        }


        if (possibleEdges == 0) {
            return 0.0;
        } else {
            return (double) edges.size() / possibleEdges;
        }
    }
}
