package nl.tue.algo.noisygraphpatterns.io;

import java.io.*;
import java.util.*;

import nl.tue.algo.noisygraphpatterns.data.graph.Graph;
import nl.tue.algo.noisygraphpatterns.data.graph.Pattern;
import nl.tue.geometrycore.geometry.linear.Rectangle;
import nl.tue.geometrycore.io.ReadItem;
import nl.tue.geometrycore.io.ipe.IPEReader;

public class Loading {

    public static Graph generate(int n, int m, int seed) {
        Random R = new Random(seed);

        Pattern.n = 1;
        Graph g = new Graph("Random-"+seed, n, 0);

        while (m > 0) {
            int from = R.nextInt(n);
            int to = R.nextInt(n);
            if (!g.edges[from][to].exists) {
                g.edges[from][to].exists = true;
                m--;
            }
        }
        return g;
    }

    public static Graph loadFile(File f) {
        if (f.getName().endsWith(".ipe")) {
            return loadIPEFile(f);
        } else if (f.getName().endsWith(".json")) {
            return loadJSONFile(f);
        } else {
            return null;
        }
    }

    public static Map<String, Graph> loadCollection(File f) {
        if (f.getName().endsWith(".csv") ) {
            return loadSCHFile(f);
        } else if (f.getName().endsWith(".dyjson")) {
            return loadFLTFile(f);
        } else {
            return null;
        }
    }

    public static Graph loadJSONFile(File f) {
        try {
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line;
            int n = 0;

            while (!(read.readLine().trim()).startsWith("\"nodes\"")) {}

            while (!(line = read.readLine().trim()).startsWith("\"links\"")) {
                if (line.startsWith("\"id\"")) {
                    n++;
                }
            }

            Graph g = new Graph(f.getName().split("\\.")[0], n, 0);
            int c, r;
            while (!(line = read.readLine().trim()).startsWith("\"layout\"")) {
                if (line.trim().startsWith("\"source\"")) {
                    c = Integer.parseInt(line.replaceAll("\"source\": ", "").replaceAll(",", ""));
                    line = read.readLine().trim();
                    r = Integer.parseInt(line.replaceAll("\"target\": ", ""));

//                    System.out.println("Read edge (" + c + "," + r + ")" );
                    g.addEdge(c, r);
                }
            }

            g.postProcess();

            read.close();
            return g;
        } catch (IOException ex) {
            System.err.println("??");
            return null;
        }
    }

    public static Graph loadIPEFile(File f) {
        try {
            IPEReader read = IPEReader.fileReader(f);
            List<ReadItem> items = read.read();
            List<ReadItem> patterns = new ArrayList();
            for (ReadItem i : items) {
                if (i.getLayer().equals("patterns")) {
                    patterns.add(i);
                }
            }
            items.removeAll(patterns);

            int n = (int) Math.sqrt(items.size());
            Graph g = new Graph(f.getName().split("\\.")[0], n, patterns.size());

            Rectangle box = Rectangle.byBoundingBox(items);
            double w = 0;
            for (ReadItem i : items) {
                Rectangle cell = Rectangle.byBoundingBox(i);
                w = cell.width();

                int c = (int) ((cell.center().getX() - box.getLeft()) / w);
                int r = n - (int) ((cell.center().getY() - box.getBottom()) / w) - 1;

                if (c != r && i.getFill().getRed() < 20) {
                    // its dark..
                    g.addEdge(c, r);
                }
            }

            for (ReadItem i : patterns) {
                Rectangle container = Rectangle.byBoundingBox(i);

                int left = (int) ((container.getLeft() + 0.5 * w - box.getLeft()) / w);
                int right = (int) ((container.getRight() - 0.5 * w - box.getLeft()) / w);
                int top = n - (int) ((container.getTop() - 0.5 * w - box.getBottom()) / w) - 1;
                int bottom = n - (int) ((container.getBottom() + 0.5 * w - box.getBottom()) / w) - 1;



                if (left == top && right == bottom) {
                    g.patterns.add(Pattern.clique(g, left, right));
                } else {
                    if (left < top) {
                        // skip it, we assume double shown
                        //g.patterns.add(Pattern.biclique(top, bottom, left, right));
                    } else {
                        g.patterns.add(Pattern.biclique(g, left, right, top, bottom));
                    }
                }
            }
            
            g.postProcess();

            read.close();
            return g;
        } catch (IOException ex) {
            System.err.println("??");
            return null;
        }
    }

    // literal translation of Nathan's reader (https://github.com/tue-alga/reorder.js/blob/master/examples/dynamic/data_sch.js)
    public static Map<String, Graph> loadSCHFile(File f) {
        try {
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line;

            ArrayList<int[]> rows = new ArrayList();
            HashMap<Integer, Integer> indices = new HashMap();
            String[] row;
            int[] values;
            int count = 0;

            read.readLine(); // read header
            // read file into rows and rename indices
            while ((line = read.readLine()) != null) {
                row = line.split(",");
                values = new int[row.length];
                for (int i = 0; i < row.length; i++) {
                    values[i] = Integer.parseInt(row[i]);
                }
                rows.add(values);

                // rename indices
                if(!indices.containsKey(values[1])) {
                    indices.put(values[1], count);
                    count++;
                }
                if(!indices.containsKey(values[2])) {
                    indices.put(values[2], count);
                    count++;
                }
            }

            read.close();

            int T = 31220;
            count = 1;
            Graph g = new Graph("sch0"+count, indices.size(), 0);
            Map<String, Graph> collection = new HashMap<>();

            for(int i = 0; i < rows.size(); i++) {
                values = rows.get(i);
                if (values[0] > T + 3600) {
                    // save current graph
                    g.postProcess();
                    collection.put(g.name, g);
                    // set up for next graph
                    count++;
                    if (count < 10) {
                        g = new Graph("sch0"+count, indices.size(), 0);
                    } else {
                        g = new Graph("sch" + count, indices.size(), 0);
                    }
                    T = values[0];
                }
                g.addEdge(indices.get(values[1]), indices.get(values[2]));
            }

            return collection;

        } catch (IOException ex) {
            System.err.println("??");
            return null;
        }
    }

    // literal translation of Nathan's reader (https://github.com/tue-alga/reorder.js/blob/master/examples/dynamic/data_flt.js)
    public static Map<String, Graph> loadFLTFile(File f) {
        try {
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line;

            ArrayList<ArrayList<ArrayList<Double>>> matrices = new ArrayList<>();
            ArrayList<ArrayList<Double>> matrix = new ArrayList<>();
            ArrayList<Double> row;


            // skip to line starting with "times"
            while (!(line = read.readLine().trim()).startsWith("\"times\"")) {
//                System.out.println(line);
            }
            // discard next line with only {
            read.readLine();

            // read file into collection of matrices consisting of rows
            while ((line = read.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\"matrix\"")) {
                    matrix = new ArrayList<>();
                    matrices.add(matrix);
                } else if (line.startsWith("[")) {
                    row = new ArrayList<>();
                    while (!(line = read.readLine().trim()).startsWith("]")) {
                        // read in row
                        row.add(Double.parseDouble(line.replace(",", "")));
                    }
                    matrix.add(row);
                }
            }

            read.close();

            int count = 1;
            Graph g;
            Map<String, Graph> collection = new HashMap<>();
            // loop over matrices
            for(int k = 0; k < matrices.size(); k++) {
                matrix = matrices.get(k);
                if (count < 10) {
                    g = new Graph("flt0"+count, matrix.size(), 0);
                } else {
                    g = new Graph("flt" + count, matrix.size(), 0);
                }
                // add edges to graph
                for(int i = 0; i < matrix.size(); i++) {
                    for(int j = i+1; j < matrix.get(i).size(); j++) {
                        if (matrix.get(i).get(j) > 0.5) {
                                g.addEdge(i, j);
                        }
                    }
                }
                g.postProcess();
                // add graph to collection
                collection.put(g.name, g);

                count++;
            }

            return collection;

        } catch (IOException ex) {
            System.err.println("??");
            return null;
        }
    }

    public static int[] readNEOSOrdering(String neosOutput) {
        BufferedReader br = new BufferedReader(new StringReader(neosOutput));
        ArrayList<Integer> tspTour = new ArrayList<>();

        String line;
        try {
            line = br.readLine();
            // find end of file where tsp tour starts
            while(!line.endsWith("Solution file output:")) {
                line = br.readLine();
            }
            // read header of tsp tour
            br.readLine();
            // start reading the tsp tour
            System.out.println("Reached start of ordering in NEOS file");
            line = br.readLine();
            while(line != null && line.length() > 2) {
                // remove everything after space
                line = line.replaceAll(" .*", "");
                // parse the (remaining) first number of line
                tspTour.add(Integer.parseInt(line));
                line = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Reached end of NEOS file");

        return removeExtraNode(tspTour);
    }

    private static int[] removeExtraNode(ArrayList<Integer> tspTour) {
        // find index of extra node
        int extraNode = -1;
        for (int i = 0; i < tspTour.size(); i++) {
            if (tspTour.get(i) == 0) {
                extraNode = i;
            }
        }
        // unwrap ordering
        int[] result = new int[tspTour.size()-1];
        // start with suffix after extraNode
        int offset = extraNode + 1;
        for (int i = offset; i < tspTour.size(); i++) {
            // subtract 1 from index, because we added 1 to real indices add extraNode at index 0
            result[i - offset] = tspTour.get(i) - 1;
        }
        // concatenate prefix before extraNode
        offset = result.length - (extraNode + 1);
        for (int i = 0; i < extraNode; i++) {
            // subtract 1 from index, because we added 1 to real indices to add extraNode at index 0
            result[offset] = tspTour.get(i) - 1;
        }

        System.out.println("Optimal TSP ordering extracted");
        System.out.println("------------------------------");
        return result;
    }
}
