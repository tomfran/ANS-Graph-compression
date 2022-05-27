package it.tomfran.thesis;

import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AnsGraphTest {

    static CharSequence METHOD = "orderStatistic";

    @Test
    public void randomGraphIntegrityCheck() throws IOException {
        int n = 500000;
        double e = 0.00001;

        ErdosRenyiGraph g = new ErdosRenyiGraph(n, e);
        String f1 = "data/random/ANS";
        AnsGraph.store(g, f1, METHOD);
        String f2 = "data/random/EF";
        EFGraph.store(g, f2);
        assert integrityCheck(EFGraph.load(f2), AnsGraph.load(f1));
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

    public static boolean integrityCheck(ImmutableGraph g, AnsGraph ans){
        if (ans.numNodes() != g.numNodes()) return false;

        int interval = 10000;
        for (int i = 0; i < ans.numNodes(); i++) {
            if ((i % interval) == 0) System.out.println("Node: " + i);
            LazyIntIterator i1 = g.successors(i);
            LazyIntIterator i2 = ans.successors(i);

            if (g.outdegree(i) != ans.outdegree(i)) return false;
            for (int j = 0; j < ans.outdegree(i); j++)
                if (i1.nextInt() != i2.nextInt()) return false;

            if (i2.nextInt() != -1) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            ImmutableGraph g = BVGraph.load("data/wiki/enwiki-2013");
//            AnsGraph.store(g, "data/wiki/ans/"+ METHOD + "_enwiki-2013", METHOD);
            AnsGraph ans = AnsGraph.load("data/wiki/ans/"+ METHOD + "_enwiki-2013");
//
//            ImmutableGraph g = EFGraph.load("data/uk100/EF_UK");
//            AnsGraph.store(g, "data/uk100/ans/"+ METHOD + "_uk100", METHOD);
//            AnsGraph ans = AnsGraph.load("data/uk100/ans/"+ METHOD + "_uk100");
//
            System.out.println("Integrity check for the two graphs: " +
                    integrityCheck(g, ans));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
