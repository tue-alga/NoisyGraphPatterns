package nl.tue.algo.noisygraphpatterns.data.layout;

import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;
import nl.tue.geometrycore.geometry.mix.GeometryCycle;

import java.util.ArrayList;
import java.util.List;

public class Connector {

    public List<Pattern> incidentPatterns;
    public GeometryCycle shape;

    public Connector(List<Pattern> patterns, GeometryCycle shape) {
        this.incidentPatterns = new ArrayList();
        for (Pattern pattern : patterns) {
            this.incidentPatterns.add(pattern);
        }
        this.shape = shape;
    }

    public Connector(Pattern pattern, GeometryCycle shape) {
        this.incidentPatterns = new ArrayList();
        this.incidentPatterns.add(pattern);
        this.shape = shape;
    }

    public int getPatternCount() {
        return incidentPatterns.size();
    }
}
