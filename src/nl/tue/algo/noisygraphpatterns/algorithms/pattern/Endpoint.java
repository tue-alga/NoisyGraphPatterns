package nl.tue.algo.noisygraphpatterns.algorithms.pattern;

public class Endpoint implements Comparable {

    public final Interval interval;
    public final boolean isLeft;

    public Endpoint(Interval interval, boolean isLeft) {
        this.interval = interval;
        this.isLeft = isLeft;
    }

    @Override
    public int compareTo(Object object) {
        Endpoint other = (Endpoint) object;
        if (isLeft) {
            if (other.isLeft) {
                return Double.compare(interval.left, other.interval.left);
            } else {
                return Double.compare(interval.left, other.interval.right);
            }
        } else {
            if (other.isLeft) {
                return Double.compare(interval.right, other.interval.left);
            } else {
                return Double.compare(interval.right, other.interval.right);
            }
        }
    }
}
