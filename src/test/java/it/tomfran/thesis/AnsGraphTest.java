package it.tomfran.thesis;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AnsGraphTest {

    @Test
    public void randomGraphStoreLoad() throws IOException {
        int n = 30;
        double e = 0.8;

        ErdosRenyiGraph g = new ErdosRenyiGraph(n, e);
        System.out.println(g);
        String s = "";
        String filename = "data/random/" + n + "-" + e;
        AnsGraph.store(g, filename);

        AnsGraph ansg = AnsGraph.load(filename);

        for (int i = 0; i < n; i++) {
            System.out.print(String.format("Node: %d, outdegree: %d, modelId: %d, successors: ", i, ansg.outdegree(i), ansg.modelId(i)));
            LazyIntIterator succ = ansg.successors(i);
            int edge;
            while( (edge = succ.nextInt()) != -1){
                System.out.print(edge + " ");
            }
            System.out.println();
        }

    }
}
