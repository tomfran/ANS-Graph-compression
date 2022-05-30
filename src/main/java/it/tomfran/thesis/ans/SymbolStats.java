package it.tomfran.thesis.ans;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;

public class SymbolStats {

    /** Array length. */
    private final int length;
    /** Array to scan. */
    protected int[] iterator;
    /** Symbol to index mapping. */
    public Int2IntOpenHashMap symbolsMapping;
    /** Index to symbol mapping. */
    protected Int2IntOpenHashMap invSymbolsMapping;
    /** Ordered symbol frequencies. */
    public int[] frequencies;
    /** Sum of frequencies. */
    protected int total;
    /** Power of two to approximate frequencies. */
    public int precision;

    /**
     * Build symbols statistics from an int array.
     *
     * @param iterator array to scan.
     * @param length   length of the array.
     * @param d        power of two to approximate probabilities.
     */
    public SymbolStats(int[] iterator, int length, int d) {
        this.iterator = iterator;
        this.length = length;
        precision = 1 << d;
        buildFrequencies();
    }

    private void buildFrequencies() {
        // count element frequencies
        int totalTmp = 0;
        Int2IntOpenHashMap freqMap = new Int2IntOpenHashMap();
        for (int i = 0; i < length; i++) {
            freqMap.put(iterator[i], freqMap.getOrDefault(iterator[i], 0) + 1);
            totalTmp++;
        }
        int n = freqMap.size();
        // sort elements by value
        int[] keys = new int[n];
        int pos = 0;
        for ( Int2IntMap.Entry e: freqMap.int2IntEntrySet())
            keys[pos++] = e.getIntKey();

        IntArrays.mergeSort(keys, (k1, k2) -> freqMap.get(k2) - freqMap.get(k1));

        // build symbols mappings
        symbolsMapping = new Int2IntOpenHashMap();
        invSymbolsMapping = new Int2IntOpenHashMap();
        // build frequency arrays
        frequencies = new int[n];
        total = 0;
        int normFreq;
        int k;
        for (int i = 0; i < n; i++) {
            k = keys[i];
            symbolsMapping.put(k, i);
            invSymbolsMapping.put(i, k);
            normFreq = (int) ((double) freqMap.get(k) / totalTmp * precision);
            // make sure that there are no zero frequencies
            frequencies[i] = Integer.max(1, normFreq);
            total += frequencies[i];

        }
    }

}
