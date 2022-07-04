package it.tomfran.thesis.ans;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;

public class SymbolStats {

    public static final int ESCAPE_SYMBOL = -2;
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
    /** Ordered symbol frequencies. */
    public int[] rawFrequencies;
    /** Sum of frequencies. */
    public int total;
    /** Power of two to approximate frequencies. */
    public int precision;
    /** Escape threshold. */
    public int escapeThresholdPercentage;

    public boolean escaping;

    public int escapeIndex;

    /**
     * Build symbols statistics from an int array.
     *
     * @param iterator array to scan.
     * @param length   length of the array.
     * @param d        power of two to approximate probabilities.
     */
    public SymbolStats(int[] iterator, int length, int d, int escapeThresholdPercentage) {
        this.iterator = iterator;
        this.length = length;
        this.escapeThresholdPercentage = escapeThresholdPercentage;
        precision = 1 << d;
        escaping = false;
        escapeIndex = -1;
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


        // cycle through symbols and escape the apax
        int freqThreshold = (int) ((double) totalTmp / 100 * escapeThresholdPercentage);
//        int freqThreshold = 0;
//        freqThreshold = Math.max(freqThreshold, 1);
        int v, escapedTotal = 0;
        int[] keysBeforeCutting = getKeysArray(freqMap);
        for (int k : keysBeforeCutting){
            v = freqMap.get(k);
            if (v <= freqThreshold){
                escaping = true;
                freqMap.remove(k);
                escapedTotal += v;
            }
        }

        if (escaping)
            freqMap.put(ESCAPE_SYMBOL, escapedTotal);

        // sort elements by value
        int n = freqMap.size();
        int[] keys = getKeysArray(freqMap);
        IntArrays.mergeSort(keys, (k1, k2) -> freqMap.get(k2) - freqMap.get(k1));

        // build symbols mappings
        symbolsMapping = new Int2IntOpenHashMap();
        invSymbolsMapping = new Int2IntOpenHashMap();
        // build frequency arrays
        frequencies = new int[n];
        rawFrequencies = new int[n];
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
            rawFrequencies[i] = freqMap.get(k);
            total += frequencies[i];
        }

        if (escaping)
            escapeIndex = symbolsMapping.get(ESCAPE_SYMBOL);
    }

    public static int[] getKeysArray(Int2IntOpenHashMap m) {
        int n = m.size();
        // sort elements by value
        int[] keys = new int[n];
        int pos = 0;
        for (int e : m.keySet())
            keys[pos++] = e;
        return keys;
    }

    public void debugPrint(){
        System.out.println("Total frequency: " + total);
        System.out.println("Symbols: ");
        for (int i = 0; i < frequencies.length; i++) {
            System.out.println("\t- " + invSymbolsMapping.get(i) + " fs: " + frequencies[i]);
        }
        if (escaping)
            System.out.println("Escape index: " + escapeIndex);
    }

}
