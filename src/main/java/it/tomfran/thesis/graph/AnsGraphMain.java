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
        if (args.length < 3){
            System.out.println("Enter graph directory, graph name and mode, with req parameters");
            System.out.println("Mode: \n" +
                            "\t- 1: optimal, no params\n" +
                            "\t- 2: escaping, params: ESCAPE-PERC\n" +
                            "\t- 3: clustering, params: K ITER PRIOR\n");
            System.exit(0);
        }
        try {
            String graphDir = args[0];
            String graphName = args[1];
            int mode = Integer.parseInt(args[2]);

            String bvPath = "data/" + graphDir + "/bv/" + graphName;
            String efPath = "data/" + graphDir + "/ef/" + graphName;
            String ansPath;
            ImmutableGraph g = BVGraph.load(bvPath);
            EFGraph.store(g, efPath);

            System.out.println("Graph: " + graphName);
            System.out.println("\t- nodes: " + g.numNodes());
            System.out.println("\t- arcs: " + g.numArcs());

            if (mode == 1) {
                System.out.println("\n\n### Optimal store ###");
                ansPath = "data/" + graphDir + "/optimal_ans/" + graphName;
                AnsGraph.store(g, ansPath);
//                System.out.println("Integrity check: " + integrityCheck(g, AnsGraph.load(ansPath)));
            }

            if (mode == 2) {
                System.out.println("\n\n### Escaping ########");
                if (args.length < 4) {
                    System.out.println("Insert ESCAPE percentage after mode");
                    System.exit(0);
                }
                int esc = Integer.parseInt(args[3]);
                System.out.println("\nEscape: " + esc);
                ansPath = "data/" + graphDir + "/escaped_ans/" + String.format("%03d_", esc) + graphName;
                AnsGraph.storeEscape(g, ansPath, esc);
//                System.out.println("Integrity check: " + integrityCheck(g, AnsGraph.load(ansPath)));
            }

            if (mode == 3) {
                System.out.println("\n\n### Clustering ######");
                if (args.length < 6) {
                    System.out.println("Insert K, ITER, PRIOR after mode");
                    System.exit(0);
                }
                int k = Integer.parseInt(args[3]);
                int iter = Integer.parseInt(args[4]);
                int prior = Integer.parseInt(args[5]);
                if (k > g.numNodes() || prior > 100) {
                    System.out.println("K must be smaller than num nodes, prior must be under 100");
                    System.exit(0);
                }
                System.out.println("\n- K: " + k + ", iter: " + iter + ", prior: " + prior);
                ansPath = "data/" + graphDir + "/clustered_ans/" + k + "_K_" + String.format("%03d_", iter) + "_iter_" + String.format("%03d_", prior) + "_prior_" + graphName;
                AnsGraph.storeCluster(g, ansPath, k, iter, prior);
//                System.out.println("Integrity check: " + integrityCheck(g, AnsGraph.load(ansPath)));
            }

        } catch (IOException e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }

    }
}
