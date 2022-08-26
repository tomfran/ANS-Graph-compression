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

        ImmutableGraph g = BVGraph.load("data/uk100/bv/uk100");
        AnsGraph.storeCluster(g, "data/test/uk100", 1000, 0, true);
        System.out.println(integrityCheck(g, AnsGraph.load("data/test/uk100")));
    }

}
