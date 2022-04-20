package it.tomfran.thesis.ans;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SymbolStats {

    /** Array length. */
    private int length;
    /** Array to scan. */
    protected int[] iterator;
    /** Symbol to index mapping. */
    protected HashMap<Integer, Integer> symbolsMapping;
    /** Index to symbol mapping. */
    protected HashMap<Integer, Integer> invSymbolsMapping;
    /** Ordered symbol frequencies. */
    protected int[] frequencies;
    /** Sum of frequencies. */
    protected int total;
    /** Power of two to approximate frequencies. */
    protected int precision;

    /**
     * Build symbols statistics from an int array.
     *
     * @param iterator array to scan.
     * @param length length of the array.
     * @param d power of two to approximate probabilities.
     */
    public SymbolStats(int[] iterator, int length, int d) {
        this.iterator = iterator;
        this.length = length;
        precision = 2 << d;
        buildFrequencies();
    }

    private void buildFrequencies(){
        // count element frequencies
        int totalTmp = 0;
        HashMap<Integer, Integer> freqMap = new HashMap<>();
        for (int i = 0; i < length; i++) {
            freqMap.put(iterator[i], freqMap.getOrDefault(iterator[i], 0) + 1);
            totalTmp ++;
        }
        // sort elements by value
        List<Entry<Integer, Integer>> sorted = freqMap.entrySet().stream().sorted(
                (Entry<Integer, Integer> a, Entry<Integer, Integer> b) -> Integer.compare(b.getValue(),
                        a.getValue())).collect(Collectors.toList());

        // build symbols mappings
        symbolsMapping = new HashMap<Integer, Integer>();
        invSymbolsMapping = new HashMap<Integer, Integer>();
        // build frequency arrays
        int n = freqMap.size();
        frequencies = new int[n];
        total = 0;
        int normFreq;
        for (int i = 0; i < n; i++) {
            symbolsMapping.put(sorted.get(i).getKey(), i);
            invSymbolsMapping.put(i, sorted.get(i).getKey());
            normFreq = (int)((double)sorted.get(i).getValue() / totalTmp * precision);
            // make sure that there are no zero frequencies
            frequencies[i] = Integer.max(1, normFreq);
            total += frequencies[i];
        }
    }

}
