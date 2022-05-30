package it.tomfran.thesis.clustering;


import it.tomfran.thesis.ans.SymbolStats;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.Arrays;

public class DatapointHistogram {

    static final int DEFAULT_FREQ = 1;

    protected Int2IntOpenHashMap symbolsMapping;
    protected int[] frequencies;
    protected int precision;


    public DatapointHistogram (SymbolStats s){
        this.symbolsMapping = s.symbolsMapping;
        this.frequencies = s.frequencies;
        this.precision = s.precision;
    }

    private double getSymProbability (int sym) {
        int pos = symbolsMapping.getOrDefault(sym, -1);
        int f = (pos == -1)? DEFAULT_FREQ : frequencies[pos];
        return (double) f / precision;
    }

    public double KLDivergence (DatapointHistogram h) {
        double ret = 0, p1, p2;
        int sym;
        for (Int2IntMap.Entry e : symbolsMapping.int2IntEntrySet()) {
            sym = e.getIntKey();
            p1 = getSymProbability(sym);
            p2 = h.getSymProbability(sym);
            ret += p1 * Math.log(p1 / p2);
        }
        return ret;
    }

    public double distance (DatapointHistogram h) {
        return (KLDivergence(h) + h.KLDivergence(this))/2;
    }

    @Override
    public String toString() {
        String s = "DatapointHistogram, symbols, prob: ";

    }
}
