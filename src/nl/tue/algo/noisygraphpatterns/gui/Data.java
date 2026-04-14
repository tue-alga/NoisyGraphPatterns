package nl.tue.algo.noisygraphpatterns.gui;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import nl.tue.algo.noisygraphpatterns.algorithms.layout.*;
import nl.tue.algo.noisygraphpatterns.algorithms.pattern.OptimalCliqueSelector;
import nl.tue.algo.noisygraphpatterns.algorithms.pattern.PatternSelector;
import nl.tue.algo.noisygraphpatterns.data.graph.Graph;
import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;
import nl.tue.algo.noisygraphpatterns.data.layout.Layout;
import nl.tue.algo.noisygraphpatterns.io.Loading;
import nl.tue.geometrycore.gui.GUIUtil;

public class Data {

    public static void main(String[] args) {
        File initialGraph = new File("./data/sch_2_s.ipe");
        Data data = new Data(initialGraph);
        GUIUtil.makeMainFrame("Noisy Graph Pattern Visualizer", data.draw, data.side);
        data.load(initialGraph);
    }

    public LayoutAlgorithm[] layoutAlgorithms = {new SummaryLayout()};
    public LayoutAlgorithm selectedLayoutAlgorithm = layoutAlgorithms[0];

    public PatternSelector[] patternSelectors = {new OptimalCliqueSelector()};
    public PatternSelector selectedPatternSelector = patternSelectors[0];

    public boolean precisionBar = true;
    public boolean horizontalBar = false;
    public boolean drawDefault = false;
    public boolean drawRandom = false;
    public boolean show_ordering = true;
    public boolean show_rulers = false;
    public boolean show_biofabric = false;
    public boolean show_biomotifs = false;

    public Map<String, Graph> orderings = new HashMap<>();
    private String currentOrder;

    public Map<String, Graph> graphCollection;
    private String currentGraph;

    public Graph graph;
    public Layout layout;

    private final DrawPanel draw;
    private final SidePanel side;

    private IncrementRunner incrementer = new IncrementRunner();

    public Data(File f) {
        draw = new DrawPanel(this);
        side = new SidePanel(this);
        graph = Loading.loadFile(f);
    }

    public void setDrawDefault(boolean v) {
        drawDefault = v;
        draw.zoomToFit();
    }

    public void setDrawRandom(boolean v) {
        drawRandom = v;
        draw.zoomToFit();
    }

    public void setPrecisionBar(Boolean v) {
        precisionBar = v;
        draw.repaint();
    }

    void setHorizontalBar(boolean v) {
        horizontalBar = v;
        draw.repaint();
    }

    void setShowOrdering(boolean v) {
        show_ordering = v;
        draw.repaint();
    }

    void setShowBiofabric(boolean v) {
        show_biofabric = v;
        draw.zoomToFit();
    }

    void setShowBioMotifs(boolean v) {
        show_biomotifs = v;
        draw.zoomToFit();
    }

    void runLayoutAlgorithm() {
        layout = selectedLayoutAlgorithm.run(graph);
        layout.computeMetaData();
        draw.zoomToFit();
    }

    void runLayoutAlgorithmIncremental() {
        if (layout == null) {
            runLayoutAlgorithm();
        } else {
            selectedLayoutAlgorithm.runIncremental(graph, layout);
            draw.zoomToFit();
        }
    }

    void runPatternSelector() {
        selectedPatternSelector.run(graph);
        reload();
    }

    JFileChooser choose = new JFileChooser("./data/");

    void openJSONFile() {
        choose.setCurrentDirectory(new File("./data/"));
        choose.resetChoosableFileFilters();
        choose.setAcceptAllFileFilterUsed(false);
        choose.setFileFilter(new FileNameExtensionFilter("JSON file", new String[] { "json" }));

        int res = choose.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = choose.getSelectedFile();
            load(f);
        }
    }

    void openIPEFile() {
        choose.setCurrentDirectory(new File("./data/"));
        choose.resetChoosableFileFilters();
        choose.setAcceptAllFileFilterUsed(false);
        choose.setFileFilter(new FileNameExtensionFilter("IPE file", new String[] { "ipe" }));

        int res = choose.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = choose.getSelectedFile();
            load(f);
        }
    }

    public void openGraphCollection() {
        choose.setCurrentDirectory(new File("./data/"));
        choose.resetChoosableFileFilters();
        choose.setAcceptAllFileFilterUsed(false);
        choose.setFileFilter(new FileNameExtensionFilter("Collection file", new String[] { "csv", "dyjson" }));

        int res = choose.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = choose.getSelectedFile();
            graphCollection = Loading.loadCollection(f);
            // load first entry
            ArrayList<String> graphNames = new ArrayList<>();
            for (String graphName : graphCollection.keySet()) {
                graphNames.add(graphName);
            }
            Collections.sort(graphNames);
            load(graphNames.getFirst());
        }
    }

    void saveIPE() {
        choose.setCurrentDirectory(new File("./figures/"));
        choose.resetChoosableFileFilters();
        choose.setAcceptAllFileFilterUsed(false);
        choose.setFileFilter(new FileNameExtensionFilter("IPE file", new String[] { "ipe" }));

        int res = choose.showSaveDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = choose.getSelectedFile();
            draw.writeIPE(f);
        }
    }

    void load(File f) {
        // load new graph and reset layout
        graph = Loading.loadFile(f);
        currentGraph = graph.name;
        graphCollection = null;
        layout = null;
        // reset orderings and add default ordering
        orderings.clear();
        orderings.put("Default", graph);
        currentOrder = "Default";
        addRandomOrdering(graph);
        if(f.getName().contains("lesmis")) {
            addOrdering("Motif paper (manual-tuned)", graph, new int[]{9,8,7,6,5,4,1,0,3,2,11,34,35,36,37,38,29,10,12,13,14,15,26,28,31,32,33,43,44,49,51,55,72,27,68,69,70,71,41,75,24,25,47,73,74,48,64,65,66,76,57,59,60,61,62,63,58,30,23,17,18,19,20,21,22,16,46,67,45,53,52,50,40,42,56,39,54});
        }
        // reset side panel
        selectedPatternSelector.preprocess(graph);
        side.refreshPatternTab();
        side.resetNEOS();
        side.refreshOrderingTab();
        side.refreshGraphsTab();
        // draw the matrix
        draw.zoomToFit();
    }

    // when loading in new ordering on patterns
    public void reload() {
        // reset layout
        layout = null;
        // reset pattern tab
        selectedPatternSelector.preprocess(graph);
        side.refreshPatternTab();
        // draw matrix
        draw.zoomToFit();
    }

    // when loading in different graph in collection
    protected void load(String graphName) {
        graph = graphCollection.get(graphName);
        currentGraph = graphName;
        layout = null;
        // reset orderings and add default ordering
        orderings.clear();
        orderings.put("Default", graph);
        currentOrder = "Default";
        addRandomOrdering(graph);
        // reset side panel
        selectedPatternSelector.preprocess(graph);
        side.refreshPatternTab();
        side.resetNEOS();
        side.refreshOrderingTab();
        side.refreshGraphsTab();
        // draw the matrix
        draw.zoomToFit();
    }

    public void addOrdering(String orderName, Graph graph, int[] ordering) {
        orderings.put(orderName, new Graph(graph, ordering));
        side.refreshOrderingTab();
    }

    public void addRandomOrdering(Graph graph) {
        ArrayList<Integer> randomOrdering = new ArrayList<Integer>();
        for(int i = 0; i < graph.vertices.length; i++) {
            randomOrdering.add(i);
        }
        Collections.shuffle(randomOrdering, new Random(69));
        addOrdering("Random", graph, randomOrdering.stream().mapToInt(i -> i).toArray());
    }

    public void setCurrentOrdering(String orderName) {
        currentOrder = orderName;
        this.graph = orderings.get(orderName);
    }

    public String[] getOrderingNames() {
        return orderings.keySet().toArray(new String[0]);
    }

    public String getCurrentOrderName() {
        return currentOrder;
    }

    public void setCurrentGraph(String graphName) {
        load(graphName);
    }

    public String getCurrentGraphName() {
        return currentGraph;
    }

    public void parameterExperiment() {
        selectedPatternSelector = patternSelectors[0];
        OptimalCliqueSelector selector = (OptimalCliqueSelector) selectedPatternSelector;

        double sigma, tau;
        new File("./figures/parameter_experiment").mkdirs();
        for(int i = 0; i < 6; i++) {
            for( int j = 0; j < 5; j++) {
                Pattern.n = 1;
                tau = BigDecimal.valueOf(0.5 + i * 0.1).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                selector.linePercent = tau;
                sigma = BigDecimal.valueOf(0.1 + j * 0.2).setScale(1, RoundingMode.HALF_UP).doubleValue();
                selector.localThreshold = sigma;

                selectedPatternSelector.preprocess(graph);
                runPatternSelector();

                File f = new File("./figures/parameter_experiment/" + currentGraph + "s" + sigma + "t" + tau + ".ipe");
                draw.writeIPE(f);
            }
        }
    }


    public void startIncrementalLayoutThread() {
        if (incrementer.isAlive() && !incrementer.isInterrupted()) {
            System.out.println("Incremental layout thread already started");
        } else {
            incrementer = new IncrementRunner();
            incrementer.start();
        }
    }

    public void stopIncrementalLayoutThread() {
        incrementer.interrupt();
    }

    public class IncrementRunner extends Thread {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    this.interrupt();
                }
                runLayoutAlgorithmIncremental();
            }
        }
    }
}
