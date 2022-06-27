package it.tomfran.thesis.clustering;


import it.tomfran.thesis.ans.SymbolStats;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;

public class DatapointHistogram {

    static final int DEFAULT_FREQ = 1;
    private static final boolean DEBUG = false;
    private static final boolean PROGRESS = false;

    public Int2IntOpenHashMap symbolsMapping;
    public Int2IntOpenHashMap invSymbolsMapping;
    public int[] frequencies;
    public int total;
    public int[] cumulative;
    public int precision;
    public EliasFanoIndexedMonotoneLongBigList sym;


    public DatapointHistogram(SymbolStats s) {
        this.symbolsMapping = s.symbolsMapping;
        this.frequencies = s.rawFrequencies;
        this.total = s.total;
    }

    public DatapointHistogram(Int2IntOpenHashMap symbolsMapping, int[] frequencies, int total) {
        this.symbolsMapping = symbolsMapping;
        this.frequencies = frequencies;
        this.total = total;
    }

    static DatapointHistogram buildCentroidFromCluster(DatapointHistogram[] points, int precision) {
        if (PROGRESS)
            System.out.println("Build from centroid, received " + points.length + " points.");

        // for each point, update the frequency of it's symbols,
        // or add a new one if required

        Int2IntOpenHashMap symbolsFrequencies = new Int2IntOpenHashMap();
        int total = 0;
        int freq;
        for (DatapointHistogram p : points){
            for (int sym : p.symbolsMapping.keySet()){
                freq = p.getSymFrequency(sym);
                symbolsFrequencies.put(sym, symbolsFrequencies.getOrDefault(sym, 0) + freq);
                total += freq;
            }
        }
        int[] frequencies = new int[symbolsFrequencies.size()];
        int pos = 0;
        Int2IntOpenHashMap symbolsMapping = new Int2IntOpenHashMap();
        for (Int2IntMap.Entry e : symbolsFrequencies.int2IntEntrySet()){
            frequencies[pos] = e.getIntValue();
            symbolsMapping.put(e.getIntKey(), pos);
            pos++;
        }

        return new DatapointHistogram(symbolsMapping, frequencies, total);
    }

    private int getSymFrequency(int sym) {
        int pos = symbolsMapping.getOrDefault(sym, -1);
        return (pos == -1) ? 1 : frequencies[pos];
    }

    private double getSymProbability(int sym) {
        return (double) getSymFrequency(sym) / total;
    }

    public double KLDivergence(DatapointHistogram h) {
        double ret = 0, p1, p2;
        for (int sym : symbolsMapping.keySet()) {
            p1 = getSymProbability(sym);
            p2 = h.getSymProbability(sym);
            ret += p1 * Math.log(p1 / p2);
        }
        return ret;
    }

    public void buildAnsStructures() {

        int N = frequencies.length;

        int[] keys = new int[N];
        int pos = 0;
        for (int e : symbolsMapping.keySet())
            keys[pos++] = e;
        // sort keys by frequency
        IntArrays.mergeSort(keys, (k1, k2) -> frequencies[symbolsMapping.get(k2)] - frequencies[symbolsMapping.get(k1)]);
        // sort frequencies
        IntArrays.mergeSort(frequencies, (k1, k2) -> k2 - k1);

        // rebuild mapping anmd inverse mapping
        invSymbolsMapping = new Int2IntOpenHashMap();
        for (int i = 0; i < N; i++) {
            symbolsMapping.put(keys[i], i);
            invSymbolsMapping.put(i, keys[i]);
        }

        cumulative = new int[N];
        cumulative[0] = 1;

        for (int i = 1; i < N; i++)
            cumulative[i] = cumulative[i - 1] + frequencies[i - 1];

        // sym
        sym = new EliasFanoIndexedMonotoneLongBigList(new IntArrayList(cumulative));

    }

    public double distance(DatapointHistogram h) {
        return (KLDivergence(h) + h.KLDivergence(this)) / 2;
    }

    public DatapointHistogram copy() {
        return new DatapointHistogram(symbolsMapping.clone(), frequencies.clone(), total);
    }
}
