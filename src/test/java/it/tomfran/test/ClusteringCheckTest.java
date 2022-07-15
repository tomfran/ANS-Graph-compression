package it.tomfran.test;

import it.tomfran.thesis.clustering.DatapointHistogram;
import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

import java.io.IOException;

public class ClusteringCheckTest {

    public static void main(String[] args) throws IOException {

//        String bvPath = "data/wa-small/bv/wa";
//        String ansPath = "data/wa-small/clustering_integrity/wa";
//        String bvPath = "data/enron/bv/enron";
//        String ansPath = "data/enron/clustering_integrity/enron";
        String bvPath = "data/uk100/bv/uk100";
        int it = 100;
        String ansPath = "data/uk100/clustering_iterations/" + it + "_uk100";
        ImmutableGraph g = BVGraph.load(bvPath);
        AnsGraph.storeCluster(g, ansPath, 5, it, 5);
    }
}
