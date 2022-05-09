package it.tomfran.thesis.graph;

import it.unimi.dsi.webgraph.LazyIntIterator;

public class EmptyAnsSuccessorsReader implements LazyIntIterator {

    @Override
    public int nextInt() {
        return -1;
    }

    @Override
    public int skip(int i) {
        return -1;
    }
}
