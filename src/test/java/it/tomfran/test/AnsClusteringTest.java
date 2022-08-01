package it.tomfran.test;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.ASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import java.io.IOException;

import static it.tomfran.thesis.graph.AnsGraphMain.integrityCheck;
import static it.unimi.dsi.webgraph.Transform.map;
import static it.unimi.dsi.webgraph.Transform.randomPermutation;

public class AnsClusteringTest {


    public static void main(String[] args) throws IOException {

        ImmutableGraph g = ASCIIGraph.load("data/l");
        BVGraph.store(g, "data/bvlight");
//        AnsGraph.store(g, "data/anslight");
        AnsGraph.storeCluster(g, "data/anslight", 1000, 0, true);
        EFGraph.store(g, "data/ef");
//        int n = 100;
//        double e = 0.2;
//        ErdosRenyiGraph g = new ErdosRenyiGraph(n, e);
//        AnsGraph.storeCluster(g, "data/erdos/random", 5, 0);
//        BVGraph.store(g, "data/erdos/bvrandom");
//        ImmutableGraph g1 = BVGraph.load("data/erdos/bvrandom");
//        System.out.println(g1);
//        System.out.println("$$$$$$$$$$$$$$");
//        System.out.println(map(g1, randomPermutation(g1, 0)));
//        BVGraph g = BVGraph.load("data/en-wiki-2013/bv/enwiki-2013");
//        System.out.println(integrityCheck(BVGraph.load("data/erdos/bvrandom"), AnsGraph.load("data/erdos/random")));
    }

}
