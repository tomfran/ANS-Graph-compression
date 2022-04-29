package it.tomfran.thesis.ans;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SymbolStats {

    /**
     * Array length.
     */
    private final int length;
    /**
     * Array to scan.
     */
    protected int[] iterator;
    /**
     * Symbol to index mapping.
     */
    protected Int2IntOpenHashMap symbolsMapping;
    /**
     * Index to symbol mapping.
     */
    protected Int2IntOpenHashMap invSymbolsMapping;
    /**
     * Ordered symbol frequencies.
     */
    protected int[] frequencies;
    /**
     * Sum of frequencies.
     */
    protected int total;
    /**
     * Power of two to approximate frequencies.
     */
    protected int precision;

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
        precision = 2 << d;
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
        // sort elements by value
        List<Entry<Integer, Integer>> sorted = freqMap.entrySet().stream().sorted(
                (Entry<Integer, Integer> a, Entry<Integer, Integer> b) -> Integer.compare(b.getValue(),
                        a.getValue())).collect(Collectors.toList());

        // build symbols mappings
        symbolsMapping = new Int2IntOpenHashMap();
        invSymbolsMapping = new Int2IntOpenHashMap();
        // build frequency arrays
        int n = freqMap.size();
        frequencies = new int[n];
        total = 0;
        int normFreq;
        for (int i = 0; i < n; i++) {
            symbolsMapping.put((int) (sorted.get(i).getKey()), i);
            invSymbolsMapping.put(i, (int) (sorted.get(i).getKey()));
            normFreq = (int) ((double) sorted.get(i).getValue() / totalTmp * precision);
            // make sure that there are no zero frequencies
            frequencies[i] = Integer.max(1, normFreq);
            total += frequencies[i];
        }
    }

}
