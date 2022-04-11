package it.tomfran.thesis.graph;

import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.io.LongOutputStream;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AnsGraph extends ImmutableGraph {

    protected int numNodes;
    protected int[] outdegree;
    protected AnsEncoder[] nodeEncoder;

    public static void store(ImmutableGraph g) {

        // for each node, get his successors array,
        // compute stats, compute encode with ans, flush and to buffer

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LongOutputStream los = new LongOutputStream(os);

        // for each node, get the outdegree and put it on the stream
        // get his successors, order (?),
        // encode the list reversed, by gap,
        // flush the encoder to the stream with encodeAll(list, false),
        // apart from the last.
        int nodes = g.numNodes();

        try {
            los.writeInt(nodes, 31);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < nodes; i++) {
            int[] succ = g.successorArray(i);
            if (succ.length == 0)
                continue;
            computeGaps(succ);
            SymbolStats s = new SymbolStats(succ, 3);
            AnsEncoder ans = new AnsEncoder(s, los);
            ans.encodeAll(succ);
            ans.flush((i == (nodes-1)));
        }

        System.out.println("Encoded");

    }

    private static void computeGaps(int[] arr){
        int n = arr.length;
        int tmp;
        // compute gaps
        for (int i = n-1; i >= 1; i--) {
            arr[i] -= arr[i-1];
        }
        // reverse order
        for (int i = 0; i < n/2; i++){
            tmp = arr[i];
            arr[i] = arr[n-i-1];
            arr[n-i-1] = tmp;
        }
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
