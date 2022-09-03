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
                    int[] s1, s2;
                    s1 = g.successorArray(i);
                    s2 = ans.successorArray(i);
                    for (int k = 0; k < g.outdegree(i); k++) System.out.print(s1[k] + ", ");
                    System.out.println("\n\n");
                    for (int k = 0; k < g.outdegree(i); k++) System.out.print(s2[k] + ", ");
                    System.out.println();
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
        if (args.length < 3) {
            System.out.println("Enter graph directory, graph name and mode, with req parameters");
            System.out.println("Mode: \n" +
                    "\t- 1: optimal, no params\n" +
                    "\t- 2: escaping, params: ESCAPE-PERC\n" +
                    "\t- 3: clustering, params: 0.0-1 partition percentage, 0.0-1 partition percentage 0-1 HE]\n");
            System.exit(0);
        }
        try {
            String graphDir = args[0];
            String graphName = args[1];
            int mode = Integer.parseInt(args[2]);

            String bvPath = "data/" + graphDir + "/bv/" + graphName;
            String efPath = "data/" + graphDir + "/ef/" + graphName;
            String ansPath;
            ImmutableGraph g = BVGraph.load(bvPath, 0);
            System.out.println("Graph: " + graphName);
            System.out.println("\t- nodes: " + g.numNodes());
            System.out.println("\t- arcs: " + g.numArcs());
            if (mode == 4) {
                EFGraph.store(g, efPath);
            }
            if (mode == 1) {
                System.out.println("\n\n### Optimal store ###");
                ansPath = "data/" + graphDir + "/optimal_ans/" + graphName;
                AnsGraph.store(g, ansPath);
            }

            if (mode == 2) {
                System.out.println("\n\n### Escaping ########");
                if (args.length < 4) {
                    System.out.println("Insert ESCAPE percentage after mode");
                    System.exit(0);
                }
                int esc = Integer.parseInt(args[3]);
                System.out.println("\nEscape: " + esc);
                ansPath = "data/" + graphDir + "/escaped_ans/" + graphName;
                AnsGraph.storeEscape(g, ansPath, esc);
            }

            if (mode == 3) {
                System.out.println("\n\n### Clustering ######");
                if (args.length < 6) {
                    System.out.println("Insert partition perc, prior escape, 1 or 0 cluster escape after mode");
                    System.exit(0);
                }
                double k = Double.parseDouble(args[3]);
                int prior = Integer.parseInt(args[4]);
                boolean clusterEscape = Integer.parseInt(args[5]) == 1;

                if (k < 0 || k > 1 || prior > 100) {
                    System.out.println("ERROR");
                    System.exit(0);
                }
                System.out.println("\n- Partitions: " + k + ", prior: " + prior + ", cluster escape: " + clusterEscape);
                ansPath = "data/" + graphDir + "/clustered_ans/ans";
                AnsGraph.storeCluster(g, ansPath, k, prior, clusterEscape);
//                System.out.print("Integrity check: ");
//                System.out.println(integrityCheck(g, AnsGraph.load(ansPath)));
            }

        } catch (IOException e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }

    }
}
