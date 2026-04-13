package nl.tue.algo.noisygraphpatterns.algorithms.pattern;

import nl.tue.algo.noisygraphpatterns.data.graph.Graph;
import nl.tue.algo.noisygraphpatterns.gui.SidePanel;

public abstract class PatternSelector {

    protected boolean preprocessed = false;

    protected boolean filter = false;
    public enum FilterOptions {LessThan5, Relative1Percent, Relative5Percent};
    public FilterOptions selectedFilter = FilterOptions.LessThan5;

    public abstract void preprocess(Graph graph);

    public boolean filters() {
        return filter;
    }

    public boolean isPreprocessed() {
        return preprocessed;
    }

    public void needsPreprocessing() {
        preprocessed = false;
    }

    public abstract void run(Graph graph);

    public abstract void initParameterGUI(SidePanel panel);

    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return name();
    }

}
