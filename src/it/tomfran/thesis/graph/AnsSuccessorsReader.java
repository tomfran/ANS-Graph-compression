package it.tomfran.thesis.graph;

import it.tomfran.thesis.ans.AnsDecoder;
import it.unimi.dsi.webgraph.LazyIntIterator;

public class AnsSuccessorsReader implements LazyIntIterator {

    protected int n;
    protected AnsDecoder dec;
    private int base;
    private int consumed;

    public AnsSuccessorsReader(int n, AnsDecoder dec) {
        this.n = n;
        this.dec = dec;
        base = 0;
        consumed = 0;
    }


    @Override
    public int nextInt() {

        if (consumed == n)
            return -1;

        int d = dec.decode();
        consumed++;

        if (d == -1)
            System.out.println("ANS ITERATOR GOT UNEXPECTED -1");

        base += d;
        return base;
    }

    @Override
    public int skip(int i) {
        for (int j = 0; j < (i-1); j++)
            nextInt();

        return nextInt();
    }

    @Override
    public String toString() {
        return "AnsSuccessorsReader{" +
                "n=" + n +
                ", dec=" + dec +
                ", base=" + base +
                ", consumed=" + consumed +
                '}';
    }
}
