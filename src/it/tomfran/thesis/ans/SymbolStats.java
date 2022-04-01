package it.tomfran.thesis.ans;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SymbolStats {

    protected Iterable<Integer> iterator;
    protected HashMap<Integer, Integer> symbolsMapping;
    protected HashMap<Integer, Integer> invSymbolsMapping;
    protected int[] frequencies;
    protected int total;

    public SymbolStats(Iterable<Integer> iterator) {
        this.iterator = iterator;
        buildFrequencies();
    }

    private void buildFrequencies(){
        // count element frequencies
        HashMap<Integer, Integer> freqMap = new HashMap<>();
        for (int element : iterator)
            freqMap.put(element, freqMap.getOrDefault(element, 0) + 1);

        // sort elements by value
        List<Entry<Integer, Integer>> sorted = freqMap.entrySet().stream().sorted(
                (Entry<Integer, Integer> a, Entry<Integer, Integer> b) -> Integer.compare(b.getValue(),
                        a.getValue())).toList();

        // build symbols mappings
        symbolsMapping = new HashMap<Integer, Integer>();
        invSymbolsMapping = new HashMap<Integer, Integer>();
        // build frequency arrays
        int n = freqMap.size();
        frequencies = new int[n];
        total = 0;
        for (int i = 0; i < n; i++) {
            symbolsMapping.put(sorted.get(i).getKey(), i);
            invSymbolsMapping.put(i, sorted.get(i).getKey());
            frequencies[i] = sorted.get(i).getValue();
            total += frequencies[i];
        }
    }

}
