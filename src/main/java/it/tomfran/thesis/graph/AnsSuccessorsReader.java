package it.tomfran.thesis.graph;

import it.tomfran.thesis.ans.AnsDecoder;
import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.AnsModel;
import it.tomfran.thesis.io.LongWordBitReader;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongBigList;
import it.unimi.dsi.webgraph.LazyIntIterator;

public class AnsSuccessorsReader implements LazyIntIterator {

    private static final boolean DEBUG = false;
    /** Successors to read. */
    protected int n;
    /** AnsModel to use. */
    protected AnsModel model;
    /** Decoded to unpack the sequence. */
    protected AnsDecoder decoder;
    /** Graph to load the required states. */
    protected LongBigList graph;
    /** Node offset in the graph stream. */
    protected long offset;
    /** LongWordBitReader to access the grpah stream. */
    protected LongWordBitReader graphLongWordBitReader;

    private int base;
    private int consumed;


    /**
     * Build a new successors reader.
     *
     * @param n Outdegree of the node.
     * @param model AnsModel to use.
     * @param graph Graph stream loaded as a long list.
     * @param offset Offset of the node in the graph.
     */
    public AnsSuccessorsReader(int n, AnsModel model, LongBigList graph, long offset) {
        this.n = n;
        this.model = model;
        this.graph = graph;
        this.offset = offset;
        base = 0;
        consumed = 0;
        graphLongWordBitReader = new LongWordBitReader(graph, 0);
        buildDecoder();
    }

    private void buildDecoder(){
        if (DEBUG) System.out.println("AnsSuccessorsReader: offset: " + offset);
        graphLongWordBitReader.position(offset);
            // read the outdegree and the model id, throw them away
        long a = graphLongWordBitReader.readGamma();
        long b = graphLongWordBitReader.readGamma();
        if (DEBUG) System.out.println("AnsSuccessorsReader: outdegree: " + a + " modelid: " + b);
        // read the number of states
        int numStates = (int) graphLongWordBitReader.readGamma();

        // create the states list, fill with numStates longs
        LongArrayList sl = new LongArrayList(numStates);
        if (DEBUG) System.out.println("AnsSuccessorsReader: rebuilding decoder, numstates: " + numStates);
        for (int i = 0; i < numStates; i++) {
            sl.add(i, graphLongWordBitReader.readState(AnsEncoder.NORM_POW));
//            sl.add(i, graphLongWordBitReader.readGamma());

            if (DEBUG) System.out.println("\t- " + sl.getLong(i));
        }

        decoder = new AnsDecoder(model, sl, numStates);
    }

    @Override
    public int nextInt() {

        if (consumed == n)
            return -1;

        int d = decoder.decode();
        consumed++;

        if (DEBUG)
            if (d == -1)
                System.out.println("ANS ITERATOR GOT UNEXPECTED -1");

        base += d;
        return base;
    }

    @Override
    public int skip(int i) {
        for (int j = 0; j < (i - 1); j++)
            nextInt();

        return nextInt();
    }

    @Override
    public String toString() {
        return "AnsSuccessorsReader{" +
                "n=" + n +
                ", dec=" + model +
                ", base=" + base +
                ", consumed=" + consumed +
                '}';
    }
}
