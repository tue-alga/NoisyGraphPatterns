package nl.tue.algo.noisygraphpatterns.algorithms.pattern;

import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;

public class Interval implements Comparable {

    public final Pattern pattern;
    public final double left;
    public final double right;
    public int index;
    public int weightPrefixMWIS;

    public Interval(Pattern pattern, double edges) {
        this.pattern = pattern;
        double offset = ((double) (pattern.bottom - pattern.top + 1) * (pattern.bottom - pattern.top + 1)) / (edges + 1);
        this.left = (double) pattern.top - offset;
        this.right = (double) pattern.bottom + offset;
    }

    @Override
    public int compareTo(Object o) {
        Interval other = (Interval) o;
        return Double.compare(this.right, other.right);
    }
}
