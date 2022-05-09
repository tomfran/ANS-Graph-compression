package it.tomfran.thesis;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AnsGraphTest {

    @Test
    public void randomGraphStoreLoad() throws IOException {
        int n = 10000;
        double e = 0.001;

        ErdosRenyiGraph g = new ErdosRenyiGraph(n, e);
        String filename = "data/random/ANS";
        AnsGraph.store(g, filename);
        String f2 = "data/random/EF";
        EFGraph.store(g, f2);

    }

    @Test
    public void integrityTest() throws IOException {
        String f1 = "data/random/ANS";
        String f2 = "data/random/EF";

        AnsGraph ans = AnsGraph.load(f1);
        EFGraph ef = EFGraph.load(f2);

        assert ans.numNodes() == ef.numNodes();

        for (int i = 0; i < 10; i++) {

            LazyIntIterator i1 = ef.successors(i);
            LazyIntIterator i2 = ans.successors(i);

            assert ef.outdegree(i) == ans.outdegree(i);
            for (int j = 0; j < ans.outdegree(i); j++)
                assert i1.nextInt() == i2.nextInt();

            assert i1.nextInt() == -1;
            assert i2.nextInt() == -1;
        }

    }

    @Test
    public void components() throws IOException {
        String base= "data/random/";

        AnsGraph ans = AnsGraph.load(base + "ANS");
        EFGraph ef = EFGraph.load(base + "EF");

        StronglyConnectedComponents s1 = StronglyConnectedComponents.compute(ans, false, null);
        StronglyConnectedComponents s2 = StronglyConnectedComponents.compute(ef, false, null);

        for (int i = 0; i < ans.numNodes(); i++) {
            assert s1.component[i] == s2.component[i];
        }

    }

}
