package nl.tue.algo.noisygraphpatterns.algorithms.pattern;

import nl.tue.algo.noisygraphpatterns.data.graph.Graph;
import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;
import nl.tue.algo.noisygraphpatterns.gui.SidePanel;
import nl.tue.geometrycore.gui.sidepanel.SideTab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptimalCliqueSelector extends PatternSelector {

    public boolean localMetric = true;
    public boolean stars = true;
    public int bicliqueSize = 2;
    public int starSize = 4;
    public double threshold = 0.8;
    public double localThreshold = 0.1;
    public double linePercent = 0.9;

    private int maxSize = 10;
    private List<Pattern> candidateBicliques = new ArrayList<>();

    private List<Interval> intervals = new ArrayList<>();
    private List<Endpoint> endpoints = new ArrayList<>();

    public OptimalCliqueSelector() {

    }

    @Override
    public void preprocess(Graph graph) {
        System.out.println("Start preprocessing patterns for " + graph.name);

        maxSize = graph.vertexCount();
        if (bicliqueSize > maxSize) {
            bicliqueSize = maxSize;
        }
        if (starSize > maxSize) {
            starSize = maxSize;
        }

        intervals.clear();
        endpoints.clear();
        List<Pattern> candidateCliques = new ArrayList<>();
        candidateBicliques.clear();

        long startTime = System.currentTimeMillis();
        findCandidateCliques(graph, candidateCliques);
        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Cliques enumerated in " + elapsedTime + " ms");

        startTime = System.currentTimeMillis();
        findCandidateBicliques(graph);
        elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Bicliques enumerated in " + elapsedTime + " ms");
        if (stars) {
            startTime = System.currentTimeMillis();
            findCandidateStars(graph);
            elapsedTime = System.currentTimeMillis() - startTime;
            System.out.println("Stars enumerated in " + elapsedTime + " ms");
        }
        System.out.println("------------------------------");

        setUpMWIS(graph, candidateCliques);
        super.preprocessed = true;
    }

    private void findCandidateCliques(Graph graph, List<Pattern> candidateCliques) {
        Pattern p;
        for (int i = 0; i < graph.vertexCount() - 1; i++) {
            for (int j = i + 2; j < graph.vertexCount(); j++) {
                p = Pattern.clique(graph, i, j);
                if ( (localMetric && checkLocalThreshold(p)) ||
                        (!localMetric && ((double) p.blackAdjacencies()) / ((j-i+1) * (j-i) - 2*(j-i)) >= threshold) ) {
                    candidateCliques.add(p);
                }
            }
        }
    }

    private void findCandidateBicliques(Graph graph) {
        Pattern p, p2;
        int b, r;
        boolean growing = true;
        for (int l = 2; l < graph.vertexCount() - 1; l++) {
            for (int t = 0; t < l - 1; t++) {
                r = l + bicliqueSize - 1;
                b = t + bicliqueSize - 1;
                p = Pattern.biclique(graph, l, r, t, b);
                growing = true;

                if ( !( (localMetric && checkLocalThreshold(p)) ||
                        (!localMetric && ((double) p.blackAdjacencies()) / ((b-t+1) * (r-l)) >= threshold) ) ) {
                    continue;
                }

                while (growing) {
                    growing = false;
                    // first try to grow both r and b
                    if (r + 1 < graph.vertexCount() && b + 1 < l) {
                        p2 = Pattern.biclique(graph, l, r+1, t, b+1);
                        if ( (localMetric && checkLocalThreshold(p2)) ||
                                (!localMetric && ((double) p2.blackAdjacencies()) / ((b+1-t+1) * (r+1-l)) >= threshold) ) {
                            p = p2;
                            b++;
                            r++;
                            growing = true;
                            continue;
                        }
                    }
                    // second try to grow r
                    if (r + 1 < graph.vertexCount()) {
                        p2 = Pattern.biclique(graph, l, r+1, t, b);
                        if ( (localMetric && checkLocalThreshold(p2)) ||
                                (!localMetric && ((double) p2.blackAdjacencies()) / ((b-t+1) * (r+1-l)) >= threshold) ) {
                            p = p2;
                            r++;
                            growing = true;
                            continue;
                        }
                    }
                    // third try to grow b
                    if (b + 1 < l) {
                        p2 = Pattern.biclique(graph, l, r, t, b+1);
                        if ( (localMetric && checkLocalThreshold(p2)) ||
                                (!localMetric && ((double) p2.blackAdjacencies()) / ((b+1-t+1) * (r-l)) >= threshold) ) {
                            p = p2;
                            b++;
                            growing = true;
                        }
                    }
                }

                candidateBicliques.add(p);
            }
        }
    }

    private void findCandidateStars(Graph graph) {
        Pattern p, p2;
        int b, r;
        boolean growing = true;
        for (int l = 1; l < graph.vertexCount(); l++) {
            for (int t = 0; t < l; t++) {
                r = l + starSize - 1;
                b = t;

                if (r < graph.vertexCount()) {
                    p = Pattern.biclique(graph, l, r, t, b);
                    growing = true;

                    if ((localMetric && checkLocalStarThreshold(p, true)) ||
                            (!localMetric && ((double) p.blackAdjacencies()) / (r - l) >= threshold)) {

                        while (growing) {
                            growing = false;
                            // try to grow r
                            if (r + 1 < graph.vertexCount()) {
                                p2 = Pattern.biclique(graph, l, r + 1, t, b);
                                if ((localMetric && checkLocalStarThreshold(p2, true)) ||
                                        (!localMetric && ((double) p2.blackAdjacencies()) / (r - l + 1) >= threshold)) {
                                    p = p2;
                                    r++;
                                    growing = true;
                                }
                            }
                        }

                        candidateBicliques.add(p);
                    }
                }

                r = l;
                b = t + starSize - 1;
                if (b < graph.vertexCount()) {
                    p = Pattern.biclique(graph, l, r, t, b);
                    growing = true;

                    if ((localMetric && checkLocalStarThreshold(p, false)) ||
                            (!localMetric && ((double) p.blackAdjacencies()) / (b - t) >= threshold)) {

                        while (growing) {
                            growing = false;
                            // third try to grow b
                            if (b + 1 < l) {
                                p2 = Pattern.biclique(graph, l, r, t, b + 1);
                                if ((localMetric && checkLocalStarThreshold(p2, false)) ||
                                        (!localMetric && ((double) p2.blackAdjacencies()) / ((b - t + 1)) >= threshold)) {
                                    p = p2;
                                    b++;
                                    growing = true;
                                }
                            }
                        }

                        candidateBicliques.add(p);
                    }
                }
            }
        }
    }

    private boolean checkLocalThreshold(Pattern p) {
        int goodColumns = 0, goodRows = 0;
        boolean result;

        int edgePairs;
        for (int c = p.left; c < p.right; c++) {
            edgePairs = 0;
            for (int r = p.top; r <= p.bottom; r++) {
                if (p.graph.edges[c][r].exists && p.graph.edges[c+1][r].exists) {
                    edgePairs++;
                }
            }
            if ( (p.type == Pattern.PatternType.CLIQUE && // cliques miss 2 pairs: consecutive columns hit 2 diagonal cells
                            ((double) edgePairs) / (p.bottom - p.top - 1) >= localThreshold) ||
                    (p.type == Pattern.PatternType.BICLIQUE && // bicliques do not hit diagonal so every row can add one pair
                            ((double) edgePairs) / (p.bottom - p.top + 1) >= localThreshold) ) {
                goodColumns++;
            }
        }

        if (p.type == Pattern.PatternType.BICLIQUE) {
            for (int r = p.top; r < p.bottom; r++) {
                edgePairs = 0;
                for (int c = p.left; c <= p.right; c++) {
                    if (p.graph.edges[c][r].exists && p.graph.edges[c][r+1].exists) {
                        edgePairs++;
                    }
                }
                if ( ((double) edgePairs) / (p.right - p.left + 1) >= localThreshold ) {
                    goodRows++;
                }
            }
            result = ((double) goodColumns) / (p.right - p.left) >= linePercent && ((double) goodRows) / (p.bottom - p.top) >= linePercent;
        } else {
            result = ((double) goodColumns) / (p.right - p.left) >= linePercent;
        }

        return result;
    }

    private boolean checkLocalStarThreshold(Pattern p, boolean row) {
        int goodColumns = 0, goodRows = 0;
        boolean result;

        int edgePairs;
        if (row) {
            for (int c = p.left; c < p.right; c++) {
                edgePairs = 0;
                for (int r = p.top; r <= p.bottom; r++) {
                    if (p.graph.edges[c][r].exists && p.graph.edges[c + 1][r].exists) {
                        edgePairs++;
                    }
                }
                if ((p.type == Pattern.PatternType.CLIQUE && // cliques miss 2 pairs: consecutive columns hit 2 diagonal cells
                        ((double) edgePairs) / (p.bottom - p.top - 1) >= localThreshold) ||
                        (p.type == Pattern.PatternType.BICLIQUE && // bicliques do not hit diagonal so every row can add one pair
                                ((double) edgePairs) / (p.bottom - p.top + 1) >= localThreshold)) {
                    goodColumns++;
                }
            }
            result = ((double) goodColumns) / (p.right - p.left) >= linePercent;

        } else { // not row, so column star
            for (int r = p.top; r < p.bottom; r++) {
                edgePairs = 0;
                for (int c = p.left; c <= p.right; c++) {
                    if (p.graph.edges[c][r].exists && p.graph.edges[c][r+1].exists) {
                        edgePairs++;
                    }
                }
                if ( ((double) edgePairs) / (p.right - p.left + 1) >= localThreshold ) {
                    goodRows++;
                }
            }
            result = ((double) goodRows) / (p.bottom - p.top) >= linePercent;
        }

        return result;
    }

    private void setUpMWIS(Graph graph, List<Pattern> candidateCliques) {

        Interval interval;
        for (Pattern p : candidateCliques) {
            interval = new Interval(p, graph.vertexCount() * graph.vertexCount());
            intervals.add(interval);
            endpoints.add(new Endpoint(interval, true));
            endpoints.add(new Endpoint(interval, false));
        }

        Collections.sort(intervals);
        for (int i = 0; i < intervals.size(); i++) {
            intervals.get(i).index = i;
        }

        Collections.sort(endpoints);
    }

    @Override
    public void run(Graph graph) {
        List<Pattern> result = new ArrayList<>();
        int tempMax = 0, lastIndex = 0;
        Endpoint current;

        // phase 1: set all weightPrefixMWIS and find lastIndex of MWIS
        for (int i = 0; i < endpoints.size(); i++) {
            current = endpoints.get(i);
            if (current.isLeft) {
                current.interval.weightPrefixMWIS = current.interval.pattern.weight() + tempMax;
            } else { // current is right endpoint
                if (current.interval.weightPrefixMWIS > tempMax) {
                    tempMax = current.interval.weightPrefixMWIS;
                    lastIndex = current.interval.index;
                }
            }
        }

        // phase 2: reconstruct MWIS by going through intervals backwards
        if (intervals.size() - 1 >= lastIndex) {
            Interval lastInterval = intervals.get(lastIndex);
            result.add(lastInterval.pattern);
            tempMax -= lastInterval.pattern.weight();


            Interval currentInterval;
            for (int i = lastIndex - 1; i > 0; i--) {
                currentInterval = intervals.get(i);
                if (currentInterval.right < lastInterval.left && currentInterval.weightPrefixMWIS == tempMax) {
                    result.add(currentInterval.pattern);
                    tempMax -= currentInterval.pattern.weight();
                    lastInterval = currentInterval;
                }
                if (tempMax == 0) {
                    break;
                }
            }
        }

        if (!candidateBicliques.isEmpty()) {
            selectBicliques(result);
        }

        reassignColors(result);
        graph.setPatterns(result);
    }

    private void selectBicliques(List<Pattern> result) {
        int maxBiclique, maxWeight = -1;
        boolean intersects, filterOut;
        Pattern selected = candidateBicliques.get(0);
        List<Pattern> leftoverBicliques = new ArrayList<>(candidateBicliques);
        List<Pattern> intersectedBicliques;

        // keep track of max weight for filtering
        if (filter) {
            for (Pattern p : result) {
                if (p.weight() > maxWeight) {
                    maxWeight = p.weight();
                }
            }
        }

        while (!leftoverBicliques.isEmpty()) {
            // select "largest" pattern
            maxBiclique = -1;
            for (Pattern p : leftoverBicliques) {
                if (p.weight() > maxBiclique) {
                    maxBiclique = p.weight();
                    selected = p;
                }
            }
            // check whether it can be added to result
            intersects = false;
            filterOut = false;
            for (Pattern p : result) {
                if (selected.intersect(p)) {
                    intersects = true;
                } else if (filter) {
                    switch (selectedFilter) {
                        case LessThan5:
                            if (selected.weight() < 5) {
                                filterOut = true;
                            }
                            break;
                        case Relative1Percent:
                            if (selected.weight() < maxWeight * 0.01) {
                                filterOut = true;
                            }
                            break;
                        case Relative5Percent:
                            if (selected.weight() < maxWeight * 0.05) {
                                filterOut = true;
                            }
                            break;
                    }
                }
            }
            // adapt the candidates and result
            if(!intersects && !filterOut) {
                result.add(selected);
                if (filter && selected.weight() > maxWeight) {
                    maxWeight = selected.weight();
                }

                intersectedBicliques = new ArrayList<>();
                for (Pattern p : leftoverBicliques) {
                    if (selected.intersect(p)) {
                        intersectedBicliques.add(p);
                    }
                }
                leftoverBicliques.removeAll(intersectedBicliques);
            }
            leftoverBicliques.remove(selected);
        }

    }

    private void reassignColors(List<Pattern> result) {
        for (Pattern p : result) {
            p.assignColor();
        }
    }

    @Override
    public void initParameterGUI(SidePanel panel) {
        SideTab tab = panel.getTab("Patterns");
        tab.addLabel("Candidates");
        tab.makeSplit(4, 2);
        tab.addLabel("Bicliques min. size");
        tab.addIntegerSpinner(bicliqueSize, 2, maxSize, 1, (e, v) -> {
            bicliqueSize = v;
            panel.patternSelectorNeedsPreprocessing();
        });
        tab.addCheckbox("Stars", stars, (e, v) -> {
            stars = v;
            panel.patternSelectorNeedsPreprocessing();
            panel.refreshPatternTab();
        });
        if (stars) {
            tab.addIntegerSpinner(starSize, 3, maxSize, 1, (e, v) -> {
                starSize = v;
                panel.patternSelectorNeedsPreprocessing();
            });
        }

        tab.addSeparator(1);

        tab.addLabel("Parameters");
        tab.addLabel("Global");
        tab.makeSplit(4, 2);
        tab.addLabel("Threshold");
        tab.addDoubleSpinner(threshold, 0, 1, 0.01, (e, v) -> {
            threshold = v;
            panel.patternSelectorNeedsPreprocessing();
        });

        tab.addSpace(2);

        tab.makeSplit(4, 2);
        tab.addLabel("Local");
        tab.addCheckbox("Local metric", localMetric, (e, v) -> {
            localMetric = v;
            panel.patternSelectorNeedsPreprocessing();
        });
        tab.makeSplit(4, 2);
        tab.addLabel("Threshold");
        tab.addDoubleSpinner(localThreshold, 0, 1, 0.01, (e, v) -> {
            localThreshold = v;
            panel.patternSelectorNeedsPreprocessing();
        });

        tab.makeSplit(4, 2);
        tab.addLabel("Row/Column %");
        tab.addDoubleSpinner(linePercent, 0, 1, 0.01, (e, v) -> {
            linePercent = v;
            panel.patternSelectorNeedsPreprocessing();
        });

        tab.addCheckbox("Filter", filter, (e, v) -> {
            filter = v;
            panel.refreshPatternTab();;
        });
    }
}
