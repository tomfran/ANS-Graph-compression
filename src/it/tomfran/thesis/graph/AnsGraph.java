package it.tomfran.thesis.graph;

import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.io.LongOutputStream;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.ByteArrayOutputStream;

public class AnsGraph extends ImmutableGraph {

    public static void store(ImmutableGraph g) {

        // for each node, get his successors array,
        // compute stats, compute encode with ans, flush and to buffer

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LongOutputStream los = new LongOutputStream(os);



//        SymbolStats s = new SymbolStats();
//        AnsEncoder ans = new AnsEncoder(s, los);


        // flush the buffer to disk

    }

    public LazyIntIterator successors(int node) {
        return null;
    }

    public int[] successorsArray(int node) {
        return null;
    }

    @Override
    public int numNodes() {
        return 0;
    }

    @Override
    public boolean randomAccess() {
        return false;
    }

    @Override
    public int outdegree(int i) {
        return 0;
    }

    @Override
    public ImmutableGraph copy() {
        return null;
    }
}
