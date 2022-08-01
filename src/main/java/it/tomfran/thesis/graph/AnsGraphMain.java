package it.tomfran.thesis.graph;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.algo.ConnectedComponents;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;

import java.io.IOException;

import static it.unimi.dsi.webgraph.Transform.*;

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
//            BVGraph.store(random, bvPath + "_random");
//            ImmutableGraph m2 = map(g, lexicographicalPermutation(g));
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
                    System.out.println("Insert K, PRIOR ESCAPE, 1 or 0 CLUSTER ESCAPE after mode");
                    System.exit(0);
                }
                int k = Integer.parseInt(args[3]);
                int prior = Integer.parseInt(args[4]);
                boolean clusterEscape = Integer.parseInt(args[5]) == 1;
                if (k > g.numNodes() || prior > 100) {
                    System.out.println("K must be smaller than num nodes, prior must be under 100");
                    System.exit(0);
                }
                System.out.println("\n- Partitions: " + k + ", prior: " + prior + ", cluster escape: " + clusterEscape);
                ansPath = "data/" + graphDir + "/clustered_ans/" + k + "_" + String.format("%d", prior) + "_" + args[5] + "_" + graphName;

                System.out.println("Storing original graph");
                AnsGraph.storeCluster(g, ansPath, k, prior, clusterEscape);
                System.out.print("Integrity check: ");
                System.out.println(integrityCheck(g, AnsGraph.load(ansPath)));
//                System.out.println("Storing random permutation graph");
//                ImmutableGraph random = map(g, randomPermutation(g, 0));
//                AnsGraph.storeCluster(random, ansPath + "_random", k, prior, clusterEscape);
//                try {
//                    System.out.println("Storing gray permutation graph");
//                    ImmutableGraph m1 = map(g, grayCodePermutation(g));
//                    AnsGraph.storeCluster(m1, ansPath + "_gray", k, prior, clusterEscape);
//                } catch (Exception e){
//                    System.out.println("ERROR in gray permutation");
//                }
//                try {
//                    System.out.println("Storing gray lexicographic graph");
//                    AnsGraph.storeCluster(m2, ansPath + "_lex", k, prior, clusterEscape);
//                } catch (Exception e){
//                    System.out.println("ERROR in lex permutation");
//                    e.printStackTrace();
//                }
//                System.out.println("Integrity check: " + integrityCheck(g, AnsGraph.load(ansPath)));
//
            }

        } catch (IOException e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }

    }
}
