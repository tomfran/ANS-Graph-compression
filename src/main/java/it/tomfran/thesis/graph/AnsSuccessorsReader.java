package it.tomfran.thesis.graph;

import it.tomfran.thesis.ans.AnsDecoder;
import it.tomfran.thesis.ans.AnsModel;
import it.tomfran.thesis.io.LongWordBitReader;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.webgraph.LazyIntIterator;

public class AnsSuccessorsReader implements LazyIntIterator {

    private static final boolean DEBUG = false;

    protected int n;
    protected AnsModel model;
    protected AnsDecoder decoder;
    protected LongBigArrayBigList graph;
    protected long offset;
    protected LongWordBitReader graphLongWordBitReader;
    private int base;
    private int consumed;

    public AnsSuccessorsReader(int n, AnsModel model, LongBigArrayBigList graph, long offset) {

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

        graphLongWordBitReader.position(offset);
        // read the outdegree and the model id, throw them away
        graphLongWordBitReader.readGamma();
        graphLongWordBitReader.readGamma();
        // read the number of states
        int numStates = (int) graphLongWordBitReader.readGamma();
        // create the states list, fill with numStates longs
        LongArrayList sl = new LongArrayList();
        for (int i = 0; i < numStates; i++)
            sl.add(graphLongWordBitReader.readLong());


        decoder = new AnsDecoder(model, sl, numStates);

        if (DEBUG){
            decoder.debugPrint();
        }
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
