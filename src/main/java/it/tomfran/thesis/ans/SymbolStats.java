package it.tomfran.thesis.ans;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;

public class SymbolStats {

    public static final int ESCAPE_SYMBOL = -2;
    /** Symbol frequencies observed in the input. */
    public Int2IntOpenHashMap rawMap;
    /** Symbol to index mapping. */
    public Int2IntOpenHashMap symbolsMapping;
    /** Index to symbol mapping. */
    protected Int2IntOpenHashMap invSymbolsMapping;
    /** Ordered symbol frequencies. */
    public int[] frequencies;
    /** Ordered symbol frequencies. */
    public int[] rawFrequencies;
    /** Sum of frequencies. */
    public int total;
    /** Power of two to approximate frequencies. */
    public int precision;
    public boolean escaping;
    public int escapeIndex;

    public int[] cumulative;
    public EliasFanoIndexedMonotoneLongBigList sym;

    /**
     * Build symbols statistics from an int array.
     *
     * @param iterator array to scan.
     * @param length   length of the array.
     * @param d        power of two to approximate probabilities.
     */
    public SymbolStats(int[] iterator, int length, int d, int escapeThresholdPercentage) {
        precision = 1 << d;
        escaping = false;
        escapeIndex = -1;
        rawMap = computeRawFrequencies(iterator, length, escapeThresholdPercentage);
        buildFrequencies();
    }

    public SymbolStats(Int2IntOpenHashMap rawMap, int d){
        precision = 1<<d;
        escaping = rawMap.containsKey(ESCAPE_SYMBOL);
        escapeIndex = -1;
        this.rawMap = rawMap;
        buildFrequencies();
    }

    private Int2IntOpenHashMap computeRawFrequencies(int[] iterator, int length, int escapeThresholdPercentage){
        // count element frequencies
        int totalTmp = 0;
        Int2IntOpenHashMap freqMap = new Int2IntOpenHashMap();
        for (int i = 0; i < length; i++) {
            freqMap.put(iterator[i], freqMap.getOrDefault(iterator[i], 0) + 1);
            totalTmp++;
        }

        // sort keys by frequency and value
        int v, escapedTotal = 0, k;
        int[] keysBeforeCutting = getKeysArray(freqMap);
        IntArrays.parallelQuickSort(keysBeforeCutting,
                ((IntComparator) (k1, k2) -> freqMap.get(k1) - freqMap.get(k2))
                        .thenComparing((k1, k2) -> k2-k1));

        // cut all keys that comes before the threshold
        int threshold = (int)((double)freqMap.size()/100*escapeThresholdPercentage);
        escaping = (threshold>0);
        for (int i = 0; i < threshold; i++) {
            k = keysBeforeCutting[i];
            v = freqMap.get(k);
            freqMap.remove(k);
            escapedTotal += v;
        }

        if (escaping)
            freqMap.put(ESCAPE_SYMBOL, escapedTotal);

        return freqMap;
    }

    private void buildFrequencies() {

        // sort elements by frequency and inverse value
        int n = rawMap.size();
        int[] keys = getKeysArray(rawMap);
        IntArrays.parallelQuickSort(keys,
                ((IntComparator) (k1, k2) -> rawMap.get(k2) - rawMap.get(k1))
                        .thenComparing((k1, k2) -> k1-k2));

        // symbols mappings
        symbolsMapping = new Int2IntOpenHashMap();
        invSymbolsMapping = new Int2IntOpenHashMap();
        // frequency arrays
        frequencies = new int[n];
        total = 0;
        int normFreq, k, totalTmp = 0;

        for (int v : rawMap.values())
            totalTmp += v;

        for (int i = 0; i < n; i++) {
            k = keys[i];
            symbolsMapping.put(k, i);
            invSymbolsMapping.put(i, k);
            normFreq = (int) ((double) rawMap.get(k) / totalTmp * precision);
            // make sure that there are no zero frequencies
            frequencies[i] = Integer.max(1, normFreq);
            total += frequencies[i];
        }
        // add escape sym
        if (escaping)
            escapeIndex = symbolsMapping.get(ESCAPE_SYMBOL);
    }

    public void precomputeSymCumulative(){
        int N = frequencies.length;
        cumulative = new int[N];
        cumulative[0] = 1;

        for (int i = 1; i < N; i++)
            cumulative[i] = cumulative[i - 1] + frequencies[i - 1];

        // sym
        sym = new EliasFanoIndexedMonotoneLongBigList(new IntArrayList(cumulative));
    }

    /**
     * Returns keys of a Int2IntOpenHashMap as an array
     * @param m Int2IntOpenHashMap
     * @return array with the keys.
     */
    public static int[] getKeysArray(Int2IntOpenHashMap m) {
        int n = m.size();
        // sort elements by value
        int[] keys = new int[n];
        int pos = 0;
        for (int e : m.keySet())
            keys[pos++] = e;
        return keys;
    }

}
