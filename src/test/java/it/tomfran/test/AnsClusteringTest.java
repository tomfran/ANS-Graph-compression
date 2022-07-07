package it.tomfran.test;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import java.io.IOException;

import static it.tomfran.thesis.graph.AnsGraphMain.integrityCheck;

public class AnsClusteringTest {


    public static void main(String[] args) throws IOException {
        int n = 20;
        double e = 0.5;
        ErdosRenyiGraph g = new ErdosRenyiGraph(n, e);
        BVGraph.store(g, "data/erdos/bvrandom");

        AnsGraph.storeCluster(g, "data/erdos/random", 3, 0, 10);
        System.out.println(integrityCheck(BVGraph.load("data/erdos/bvrandom"), AnsGraph.load("data/erdos/random")));
    }

}
