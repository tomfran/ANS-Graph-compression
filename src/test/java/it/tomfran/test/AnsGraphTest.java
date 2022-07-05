package it.tomfran.test;

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

    public static boolean integrityCheck(ImmutableGraph g, AnsGraph ans) {
        if (ans.numNodes() != g.numNodes()) return false;

        int interval = 10000;
        for (int i = 0; i < ans.numNodes(); i++) {
//            if ((i % interval) == 0) System.out.println("Node: " + i);
            LazyIntIterator i1 = g.successors(i);
            LazyIntIterator i2 = ans.successors(i);

            if (g.outdegree(i) != ans.outdegree(i)) {
                System.out.println("Oudegree error for node " + i);
                return false;
            }
            for (int j = 0; j < ans.outdegree(i); j++)
                if (i1.nextInt() != i2.nextInt()) {
                    System.out.println("Successors error for node " + i);
                    int s1[], s2[];
                    s1 = g.successorArray(i);
                    s2 = ans.successorArray(i);
                    for (int k = 0; k < g.outdegree(i); k++) System.out.print(s1[k] + ", "); System.out.println("\n\n");
                    for (int k = 0; k < g.outdegree(i); k++) System.out.print(s2[k] + ", "); System.out.println();
                    return false;
                }


            if (i2.nextInt() != -1) {
                System.out.println("Expected -1 for node  " + i);
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        try {

            String graphDir = "it-wiki";
            String graphName = "itwiki-2013";
            String bvPath = "data/" + graphDir + "/bv/" + graphName;
            String efPath = "data/" + graphDir + "/ef/" + graphName;
            String ansPath;
            ImmutableGraph g = BVGraph.load(bvPath);
            ansPath = "data/" + graphDir + "/optimal_ans/" + graphName;
            AnsGraph.store(g, ansPath);

            for (int i = 100; i <= 100000; i*=10) {
                int k = g.numNodes()/i;
                int iter = 0;
                int prior = 0;
                System.out.println(iter + " "  + k);
                ansPath = "data/" + graphDir + "/clustered/" + k + "_K_" + iter + "_iter_" + prior + "_prior_" + graphName;
                AnsGraph.storeCluster(g, ansPath, k, iter, prior);
                System.out.println(integrityCheck(g, AnsGraph.load(ansPath)));
            }

//            k = 1;
//            System.out.println(integrityCheck(g, AnsGraph.load(ansPath)));

//            for (int i = 5; i <= 30; i += 5) {
//                System.out.println("i: " + i);
//                ansPath = "data/" + graphDir + "/escaped_opt_for_symchange_mathmax/" + String.format("%03d", i) + "_" + graphName;
//                ansPath = "data/" + graphDir + "/escape_apax/" + graphName;
//                AnsGraph.storeEscape(g, ansPath, i, i);
//                System.out.println(integrityCheck(g, AnsGraph.load(ansPath)));
//            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    public void randomGraphIntegrityCheck() throws IOException {
        int n = 500000;
        double e = 0.00001;

        ErdosRenyiGraph g = new ErdosRenyiGraph(n, e);
        String f1 = "data/random/ANS";
        AnsGraph.store(g, f1);
        String f2 = "data/random/EF";
        EFGraph.store(g, f2);
        assert integrityCheck(EFGraph.load(f2), AnsGraph.load(f1));
    }

    @Test
    public void components() throws IOException {
        String base = "data/random/";

        AnsGraph ans = AnsGraph.load(base + "ANS");
        EFGraph ef = EFGraph.load(base + "EF");

        StronglyConnectedComponents s1 = StronglyConnectedComponents.compute(ans, false, null);
        StronglyConnectedComponents s2 = StronglyConnectedComponents.compute(ef, false, null);

        for (int i = 0; i < ans.numNodes(); i++) {
            assert s1.component[i] == s2.component[i];
        }

    }

}
