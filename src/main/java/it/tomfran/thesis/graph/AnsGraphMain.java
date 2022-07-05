package it.tomfran.thesis.graph;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.IOException;

public class AnsGraphMain {


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
        if (args.length < 2){
            System.out.println("Enter graph directory and graph name, e.g. it-wiki itwiki-2013");
            System.exit(0);
        }
        try {
            String graphDir = args[0];
            String graphName = args[1];
            String bvPath = "data/" + graphDir + "/bv/" + graphName;
            String efPath = "data/" + graphDir + "/ef/" + graphName;
            String ansPath;
            ImmutableGraph g = BVGraph.load(bvPath);
            EFGraph.store(g, efPath);

            System.out.println("Graph: " + graphName);
            System.out.println("\t- nodes: " + g.numNodes());
            System.out.println("\t- arcs: " + g.numArcs());

//            System.out.println("\n\n### Optimal store ###");
//            ansPath = "data/" + graphDir + "/optimal_ans/" + graphName;
//            AnsGraph.store(g, ansPath);
////            System.out.println("Integrity check: " + integrityCheck(g, AnsGraph.load(ansPath)));
//
//            System.out.println("\n\n### Escaping ########");
//            for (int i = 1; i <= 30; i+= (i < 5)? 1 : 5) {
//                System.out.println("- " + i);
//                ansPath = "data/" + graphDir + "/escaped_ans/" + String.format("%03d_", i) + graphName;
//                AnsGraph.storeEscape(g, ansPath, i);
//            }

            System.out.println("\n\n### Clustering ######");

            for (int i = 1000; i <= 1000000; i*=10) {
                if (i > g.numNodes()) continue;
                int k = g.numNodes()/i;
                int iter = 0;
                System.out.println("- K: " + k + ", iter: "  + 0);
                ansPath = "data/" + graphDir + "/clustered_ans/" + k + "_K_" + iter + "_iter_" + graphName;
                AnsGraph.storeCluster(g, ansPath, k, iter, 0);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
