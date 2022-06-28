package it.tomfran.thesis.clustering;


import it.tomfran.thesis.ans.SymbolStats;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;

import static it.tomfran.thesis.ans.SymbolStats.ESCAPE_SYMBOL;
import static it.tomfran.thesis.ans.SymbolStats.getKeysArray;

public class DatapointHistogram {

    static final int DEFAULT_FREQ = 1;
    private static final boolean DEBUG = false;
    private static final boolean PROGRESS = false;
    private static final int PRECISION = 1024;

    public Int2IntOpenHashMap symbolsMapping;
    public Int2IntOpenHashMap invSymbolsMapping;
    public int[] frequencies;
    public int total;
    public int[] cumulative;
    public EliasFanoIndexedMonotoneLongBigList sym;

    public Int2IntOpenHashMap rawFrequencyMap;
    public int escapeIndex;

    public DatapointHistogram(SymbolStats s) {
        this.total = s.total;
        buildRawFrequencyMap(s.rawFrequencies, s.symbolsMapping);
    }

    private void buildRawFrequencyMap(int[] frequencies, Int2IntOpenHashMap symMapping) {
        this.rawFrequencyMap = new Int2IntOpenHashMap();
        for (Int2IntMap.Entry e : symMapping.int2IntEntrySet())
            rawFrequencyMap.put(e.getIntKey(), frequencies[e.getIntValue()]);
    }

    public DatapointHistogram(Int2IntOpenHashMap rawFrequencyMap, int total) {
        this.rawFrequencyMap = rawFrequencyMap;
        this.total = total;
    }

    static DatapointHistogram buildCentroidFromCluster(DatapointHistogram[] points, int precision) {
        if (PROGRESS)
            System.out.println("Build from centroid, received " + points.length + " points.");

        // for each point, update the frequency of it's symbols,
        // or add a new one if required
        Int2IntOpenHashMap rawFrequencyMap = new Int2IntOpenHashMap();
        int total = 0, k, v;
        for (DatapointHistogram p : points)
            for (Int2IntMap.Entry e : p.rawFrequencyMap.int2IntEntrySet()){
                k = e.getIntKey();
                v = e.getIntValue();
                rawFrequencyMap.put(k, v + rawFrequencyMap.getOrDefault(k, 0));
                total += v;
            }

        if (PROGRESS)
            System.out.println("Computed points intersection, total sym: " + rawFrequencyMap.size());

        return new DatapointHistogram(rawFrequencyMap, total);
    }

    private int getSymFrequency(int sym) {
        return rawFrequencyMap.getOrDefault(sym, 1);
    }

    private float getSymProbability(int sym) {
        return (float) getSymFrequency(sym) / total;
    }

    public float KLDivergence(DatapointHistogram h) {
        float ret = 0, p1, p2;
        for (int sym : rawFrequencyMap.keySet()) {
            p1 = getSymProbability(sym);
            p2 = h.getSymProbability(sym);
            ret += p1 * Math.log(p1 / p2);
        }
        return ret;
    }

    public void buildAnsStructures() {

        // escape the apax for now
        int freqThreshold = Math.max(1, (int)((double)total/100 * 0.01));
//        int freqThreshold = 1;
        int v, escapedTotal = 0;
        boolean escaping = false;
        int[] keysBeforeCutting = getKeysArray(rawFrequencyMap);
        for (int k : keysBeforeCutting) {
            v = rawFrequencyMap.get(k);
            if (v <= freqThreshold) {
                escaping = true;
                rawFrequencyMap.remove(k);
                escapedTotal += v;
            }
        }
        // if escaping, add escape sym to freq map
        if (escaping)
            rawFrequencyMap.put(ESCAPE_SYMBOL, escapedTotal);

        // sort keys by descending frequency
        int n = rawFrequencyMap.size();
        int[] keys = getKeysArray(rawFrequencyMap);
        IntArrays.mergeSort(keys, (k1, k2) -> rawFrequencyMap.get(k2) - rawFrequencyMap.get(k1));

        symbolsMapping = new Int2IntOpenHashMap();
        invSymbolsMapping = new Int2IntOpenHashMap();
        frequencies = new int[n];
        int normFreq, k, totalTmp = total;
        total = 0;
        // for each sym, rescale frequency
        for (int i = 0; i < n; i++) {
            k = keys[i];
            symbolsMapping.put(k, i);
            invSymbolsMapping.put(i, k);
            normFreq = (int) ((double) rawFrequencyMap.get(k) / totalTmp * PRECISION);
            // make sure that there are no zero frequencies
            frequencies[i] = Integer.max(1, normFreq);
            total += frequencies[i];
        }

        escapeIndex = symbolsMapping.getOrDefault(ESCAPE_SYMBOL, -1);

        // build cumulative
        cumulative = new int[n];
        cumulative[0] = 1;
        for (int i = 1; i < n; i++)
            cumulative[i] = cumulative[i - 1] + frequencies[i - 1];

        sym = new EliasFanoIndexedMonotoneLongBigList(new IntArrayList(cumulative));
    }

    public double distance(DatapointHistogram h) {
//        return (KLDivergence(h) + h.KLDivergence(this)) / 2;
        return KLDivergence(h);

    }

    public DatapointHistogram copy() {
        return new DatapointHistogram(rawFrequencyMap.clone(), total);
    }
}
