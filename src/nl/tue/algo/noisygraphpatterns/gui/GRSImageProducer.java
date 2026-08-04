package nl.tue.algo.noisygraphpatterns.gui;

import nl.tue.algo.noisygraphpatterns.io.Loading;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This class provides CLI functionality for the Graphics Replicability Stamp Initiative
 */
public class GRSImageProducer {

    public static void main(String[] args) {
        File f = new File("./data/FlashTap_flashtap_1_graphdat_8.dyjson");
        Data data = new Data(f);
        data.graphCollection = Loading.loadCollection(f);
        data.graph = data.graphCollection.get("flt58");

        data.orderings.clear();
        data.orderings.put("Default", data.graph);
        data.addOptimalOrdering();
        data.setCurrentOrdering("Optimal Moran's I");

        data.selectedPatternSelector.preprocess(data.graph);
        data.selectedPatternSelector.run(data.graph);

        data.startIncrementalLayoutThread();
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        data.stopIncrementalLayoutThread();

        data.drawDefault = true;
        f = new File("./figures/flt58-s0.5t0.85.ipe");
        try {
            f.getParentFile().mkdirs();
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        data.saveIPE(f);
    }
}
