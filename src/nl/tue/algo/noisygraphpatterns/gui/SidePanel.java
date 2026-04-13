/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nl.tue.algo.noisygraphpatterns.gui;

import java.util.ArrayList;
import java.util.Collections;

import nl.tue.algo.noisygraphpatterns.algorithms.ordering.NEOSJob;
import nl.tue.algo.noisygraphpatterns.algorithms.pattern.OptimalCliqueSelector;
import nl.tue.geometrycore.gui.sidepanel.SideTab;
import nl.tue.geometrycore.gui.sidepanel.TabbedSidePanel;

import javax.swing.*;

/**
 *
 * @author wmeulema
 */
public class SidePanel extends TabbedSidePanel {

    private final Data data;
    private SideTab layoutTab, patternTab, orderingTab, graphsTab;
    JButton neosButton;

    private NEOSJob neos;
    private boolean neosUsable = true, neosBusy = false;
    private NEOSRunner neosRunner;

    public SidePanel(Data data) {
        this.data = data;
        addIOTab();
        addGraphsTab();
        addVIStab();
        addOrderingTab();
        addPatternTab();
        addLayoutTab();

        neos = new NEOSJob(data);
    }

    private void addVIStab() {
        SideTab tab = addTab("Visual");

        tab.addCheckbox("Draw Default Matrix", data.drawDefault, (e, v) -> data.setDrawDefault(v));
        tab.addCheckbox("Draw Random Matrix", data.drawDefault, (e, v) -> data.setDrawRandom(v));
        tab.addCheckbox("Show Pattern Precision bar", data.precisionBar, (e, v) -> data.setPrecisionBar(v));
        tab.addCheckbox("Horizontal bar", data.horizontalBar, (e, v) -> data.setHorizontalBar(v));

        tab.addCheckbox("Show BioFabric", data.show_biofabric, (e, v) -> data.setShowBiofabric(v));
        tab.addCheckbox("Show BioFabric Motifs", data.show_biomotifs, (e, v) -> data.setShowBioMotifs(v));

        tab.addCheckbox("Show vertex ordering", data.show_ordering, (e, v) -> data.setShowOrdering(v));
    }

    private void addGraphsTab() {
        graphsTab = addTab("Graphs");
    }

    public void refreshGraphsTab() {
        graphsTab.clearTab();
        if(data.graphCollection == null) {
            graphsTab.addLabel("No collection is currently loaded in.");
        } else {
            ArrayList<String> graphNames = new ArrayList<>();
            for (String graphName : data.graphCollection.keySet()) {
                graphNames.add(graphName);
            }
            Collections.sort(graphNames);
            graphsTab.addComboBox(graphNames.toArray(new String[0]), data.getCurrentGraphName(), (e, v) -> {
                data.setCurrentGraph(v);
            });
        }
    }

    private void addLayoutTab() {
        layoutTab = addTab("Layout");
        refreshLayoutTab();
    }

    private void refreshLayoutTab() {
        layoutTab.clearTab();
//        layoutTab.addComboBox(data.layoutAlgorithms, data.selectedLayoutAlgorithm, (e, v) -> {
//            data.selectedLayoutAlgorithm = v;
//            refreshLayoutTab();
//        });
        data.selectedLayoutAlgorithm.initParameterGUI(layoutTab);
        layoutTab.addSeparator(1);
        layoutTab.addLabel("Algorithm controls");
        layoutTab.addLabel("Manual");
        layoutTab.addButton("Run", (e) -> {
            data.runLayoutAlgorithm();
        });
        if (data.selectedLayoutAlgorithm.allowIncremental()) {
            layoutTab.addButton("Run increment", (e) -> {
                data.runLayoutAlgorithmIncremental();
            });
            layoutTab.addSpace(2);
            layoutTab.addLabel("Automatic");
            layoutTab.addButton("Start incrementing", (e) -> {
                data.startIncrementalLayoutThread();
            });
            layoutTab.addButton("Stop incrementing", (e) -> {
                data.stopIncrementalLayoutThread();
            });
        }
    }

    private void addIOTab() {
        SideTab tab = addTab("IO");

        tab.addButton("Load JSON", (e) -> {
            data.openJSONFile();
        });

        tab.addButton("Load Graph Collection", (e) -> {
            data.openGraphCollection();
        });

        tab.addButton("Load IPE", (e) -> {
            data.openIPEFile();
        });

        tab.addButton("Save IPE", (e) -> {
            data.saveIPE();
        });

        tab.addButton("Parameter experiment", (e) -> {
            data.parameterExperiment();
        });
    }

    private void addPatternTab() {
        patternTab = addTab("Patterns");
        refreshPatternTab();
    }

    public void refreshPatternTab() {
        patternTab.clearTab();
        patternTab.addComboBox(data.patternSelectors, data.selectedPatternSelector, (e, v) -> {
            data.selectedPatternSelector = v;
            refreshPatternTab();
        });
        data.selectedPatternSelector.initParameterGUI(this);
        if (data.selectedPatternSelector.filters()) {
            patternTab.addComboBox(new OptimalCliqueSelector.FilterOptions[]{OptimalCliqueSelector.FilterOptions.LessThan5, OptimalCliqueSelector.FilterOptions.Relative1Percent, OptimalCliqueSelector.FilterOptions.Relative5Percent},
                    data.selectedPatternSelector.selectedFilter, (e, v) -> {
                        data.selectedPatternSelector.selectedFilter = v;
                    });
        }
        if (data.selectedPatternSelector.isPreprocessed()) {
            patternTab.addButton("Find patterns", (e) -> {
                data.runPatternSelector();
            });
        } else {
            patternTab.addButton("Preprocess", (e) -> {
                data.selectedPatternSelector.preprocess(data.graph);
                refreshPatternTab();
            });
        }
    }

    public void patternSelectorNeedsPreprocessing() {
        data.selectedPatternSelector.needsPreprocessing();
        refreshPatternTab();
    }

    private void addOrderingTab() {
        orderingTab = addTab("Ordering");
    }

    protected void refreshOrderingTab() {
        orderingTab.clearTab();
        orderingTab.addComboBox(data.getOrderingNames(), data.getCurrentOrderName(), (e, v) -> {
            data.setCurrentOrdering(v);
            data.reload();
            refreshOrderingTab();
        });
        if (neosUsable) {
            neosButton = orderingTab.addButton("Compute Optimal Ordering", (e) -> {
                // configure neos
                configureTSPNEOS();
                // submit request
                neosRunner = new NEOSRunner();
                neosRunner.start();
            });
        } else {
            orderingTab.addLabel("Optimal Ordering Computed");
        }
        orderingTab.addLabel("Current Moran\'s I: " + data.graph.getMoransI());
    }

    protected void resetNEOS() {
        neosUsable = true;
    }

    private void configureTSPNEOS() {
        neos.category = "co";
        neos.solver = "concorde";
        neos.inputMethod = "TSP";
        neos.inputType = "tsplib";
        neos.tsp = graphToNeos();
        neos.algType = "cplex";
        neos.fixed = "yes";
        neos.plType = "no";
    }

    private String graphToNeos() {
        int size = data.graph.vertexCount();

        StringBuilder output = new StringBuilder();

        output.append("NAME: tsplib from matrix \n");
        output.append("TYPE: TSP \n");
        output.append("COMMENT: \n");
        output.append("DIMENSION: " + (size+1) + "\n");
        output.append("EDGE_WEIGHT_TYPE: EXPLICIT \n");
        output.append("EDGE_WEIGHT_FORMAT: FULL_MATRIX \n");
        output.append("EDGE_WEIGHT_SECTION \n");

        System.out.println("Start TSP adjacency matrix computation");
        data.graph.computeMoransI();
        for (int i = 0; i < size+1; i++)
        {
            for (int j = 0; j < size+1; j++)
            {
                if (i == 0 || j == 0) {
                    output.append(0 + " ");
                } else {
                    output.append(((int) (100000 * data.graph.moransITSPCost(i-1, j-1))) + " ");
                }
            }
            output.append("\n");
        }
        System.out.println("TSP adjacency matrix computation completed");
//        System.out.println(output);
        return output.toString();
    }

    public class NEOSRunner extends Thread {

        @Override
        public void run() {
            neosButton.setEnabled(false);
            neosButton.setText("NEOS is serving...");
            int[] result = neos.submit();
            if (result.length == 0) {
                neosButton.setEnabled(true);
            } else {
                neosUsable = false;
            }
            refreshOrderingTab();
        }
    }
}
