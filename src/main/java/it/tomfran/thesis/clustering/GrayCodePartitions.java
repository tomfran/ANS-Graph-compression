package it.tomfran.thesis.clustering;

import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.webgraph.LazyIntIterator;

import static it.tomfran.thesis.ans.SymbolStats.ESCAPE_SYMBOL;
import static it.tomfran.thesis.ans.SymbolStats.getKeysArray;
import static java.lang.Math.*;

public class GrayCodePartitions {

    final int PRECISION = 1 << AnsGraph.P_RANGE;
    final double ALPHA = 0.9;
    public int[] assignment;
    public SymbolStats[] partitionSymbolStats;
    public int n;
    public boolean heuristicEscape;

    public GrayCodePartitions(Int2IntOpenHashMap[] datapoints, double partitionsPercentage, boolean heuristicEscape) {
        n = datapoints.length;
        this.heuristicEscape = heuristicEscape;
        split(datapoints, partitionsPercentage);
    }

    private int[][] buildSortedKeys(Int2IntOpenHashMap[] datapoints) {
        int[][] keys = new int[datapoints.length][];

        for (int i = 0; i < datapoints.length; i++) {
            keys[i] = getKeysArray(datapoints[i]);
            IntArrays.parallelQuickSort(keys[i]);
        }

        return keys;
    }

    private void split(Int2IntOpenHashMap[] datapoints, double partitionsPercentage) {

        int[][] keys = buildSortedKeys(datapoints);

        // initialize permutation
        final int[] perm = new int[n];
        int i = n;
        while (i-- != 0) perm[i] = i;

        // grey comparator
        final IntComparator grayComparator = (x, y) -> {
            final LazyIntIterator i1 = new KeysIterator(keys[x]), j = new KeysIterator(keys[y]);
            int a, b;
            boolean parity = false; // Keeps track of the parity of number of arcs before the current ones.
            for (; ; ) {
                a = i1.nextInt();
                b = j.nextInt();
                if (a == -1 && b == -1) return 0;
                if (a == -1) return parity ? 1 : -1;
                if (b == -1) return parity ? -1 : 1;
                if (a != b) return parity ^ (a < b) ? 1 : -1;
                parity = !parity;
            }
        };
        // sort hashmaps by gray code on symbols
        IntArrays.parallelQuickSort(perm, 0, n, grayComparator);

        // TODO: aggiungi scelta partizioni intelligente
        // build assignment
//        int clusterSize = (int) Math.ceil((double) n / numPartitions);
//        int totalSyms = 0;
//        for (Int2IntOpenHashMap e : datapoints)
//            totalSyms += e.size();
//        System.out.println("####\nTotal symbols: " + totalSyms + " avergage syms: " + (double)totalSyms/ datapoints.length);
//        assignment = new int[n];
//
//        int ind = 0;
//        for (i = 0; i < n; i += clusterSize) {
//            for (int j = i; j < min(n, i + clusterSize); j++)
//                assignment[perm[j]] = ind;
//            ind++;
//        }
        assignment = findSplits(datapoints, partitionsPercentage);
        int ind = assignment[assignment.length-1]+1;
        // build the final maps as the union of the cluster symbols
        Int2IntOpenHashMap[] finalMaps = new Int2IntOpenHashMap[ind];
        i = ind;
        while (i-- != 0) finalMaps[i] = new Int2IntOpenHashMap();

        for (i = 0; i < n; i++)
            for (Int2IntMap.Entry e : datapoints[i].int2IntEntrySet())
                finalMaps[assignment[i]].addTo(e.getIntKey(), e.getIntValue());

        // add escaping with space heuristic
        if (heuristicEscape)
            for (i = 0; i < ind; i++)
                finalMaps[i] = heuristicEscaping(finalMaps[i]);

        partitionSymbolStats = new SymbolStats[ind];
        for (i = 0; i < ind; i++) {
            partitionSymbolStats[i] = new SymbolStats(finalMaps[i], AnsGraph.P_RANGE);
            partitionSymbolStats[i].precomputeSymCumulative();
        }
    }

    private int[] findSplits(Int2IntOpenHashMap[] datapoints, double partitionsPercentage) {
        int[] assignment = new int[datapoints.length];
        int total = 0;
        for (Int2IntOpenHashMap m : datapoints) total += m.size();

        // define the max number of symbols in a model as the mean of models
        int maxSymbols = (int) ceil((double)total/ datapoints.length);
//        System.out.println("Initial max symbols: " + maxSymbols);

        int totalSplits = datapoints.length;
        // define the limit to the splits, that is a percentage of the total points
        int limit = (int)ceil(datapoints.length*partitionsPercentage);
        // while I still need to find a valid split
        // double the max symbols, and check is this leads to a valid split,
        // if not, double again...
        System.out.println("Limit: " + limit);
        while (totalSplits > limit) {
            int ind = 0;
            IntOpenHashSet dummy = new IntOpenHashSet();
            for (int i = 0; i < datapoints.length; i++) {
                if (dummy.size() > maxSymbols) {
                    ind++;
                    dummy.clear();
                }
                assignment[i] = ind;
                dummy.addAll(datapoints[i].keySet());
            }
            totalSplits = ind + 1;
            maxSymbols *= 2;
            System.out.println("Total clusters: " + ind);
        }
        return assignment;
    }

    private Int2IntOpenHashMap heuristicEscaping(Int2IntOpenHashMap map) {

        // check if the map already has escape, this can happen
        // if prior escaping is selected, if so, remove the escape and
        // initialize the escapedTotal variable accordingly
        int escapedTotal, mapSize, freqSum = 0;
        escapedTotal = map.getOrDefault(ESCAPE_SYMBOL, 0);
        boolean escaping = escapedTotal > 0;
        map.remove(ESCAPE_SYMBOL);
        mapSize = map.size();

        for (int v : map.values())
            freqSum += v;

        // sort the map keys by frequency
        int[] keys = getKeysArray(map);
        IntArrays.parallelQuickSort(keys, (k1, k2) -> map.get(k2) - map.get(k1));

        // start the heuristc, at each step, add the least frequent symbol
        // to the escape set, computing overall space taken
        // Keep the threshold minimizing this metric

        // entropy of the encoding
        double symsEntropy = 0;
        // raw and normalized frequency and symbol probability
        int symFreq, normSymFreq;
        double symProb;
        // binary magnitute of the symbol
        int symBinLen = 0;
        // bits required by the symbols mapping and frequency
        int ansModelBits = 0, ansFreqBits = 0;
        int prevSymFreq = 0;

        // initialization phase
        for (int k : keys) {
            symFreq = map.get(k);
            symProb = (double) symFreq / freqSum;
            normSymFreq = max(1, (int) (symProb * PRECISION));
            symBinLen = max(symBinLen, Fast.ceilLog2(k));
            // update entropy of encode
            symsEntropy -= symFreq * Fast.log2(symProb);
            // store how many bits this key and its frequency
            // takes in gamma
            ansModelBits += gammaLen(k);
            ansFreqBits += normSymFreq - prevSymFreq;
            prevSymFreq = normSymFreq;
        }
        // heuristic step: find the threshold that minimizes
        // the overall space estimation
        double escapeEntropy = 0, escapeProb;
        int escapeBits = (escaping) ? escapedTotal * symBinLen : 0;
        double minOverall, currOverall;

        if (escaping) {
            escapeProb = Fast.log2((double) escapedTotal / freqSum);
            escapeEntropy = -(escapedTotal * escapeProb);
        }
        minOverall = symsEntropy + escapeEntropy + escapeBits +
                ansModelBits + ansFreqBits + gammaLen(escapedTotal);
        int k;
        int escapeThreshold = mapSize + 1;
        for (int i = mapSize - 1; i >= 0; i--) {
            k = keys[i];
            symFreq = map.get(k);
            symProb = (double) symFreq / freqSum;
            normSymFreq = max(1, (int) (symProb * PRECISION));

            // update entropy of ans and escape and escape bits
            symsEntropy += symFreq * Fast.log2(symProb);
            escapedTotal += symFreq;
            escapeEntropy = -(escapedTotal * Fast.log2((double) escapedTotal / PRECISION));
            escapeBits = (int) (escapedTotal * symBinLen * ALPHA);

            // update ans structures
            ansModelBits -= gammaLen(k);
            ansFreqBits -= gammaLen(normSymFreq);

            // update overall
            currOverall = symsEntropy + escapeEntropy + escapeBits
                    + ansModelBits + ansFreqBits + gammaLen(escapedTotal);
            if (currOverall < minOverall) {
                escapeThreshold = i;
                minOverall = currOverall;
            }
        }
        // once the escapeThreshold has been set, remove all the
        // unnecessary keys from the map, recount the escape total
        if (!escaping)
            escaping = escapeThreshold < mapSize;
        escapedTotal = 0;
        for (int i = escapeThreshold; i < mapSize; i++) {
            k = keys[i];
            escapedTotal += map.get(k);
            map.remove(k);
        }

        if (escaping)
            map.put(ESCAPE_SYMBOL, escapedTotal);

        return map;
    }

    private int gammaLen(int k) {
        final int msb = Fast.mostSignificantBit(k);
        return 2 * msb + 1;
    }

    public int getPartitionIndex(int i) {
        return assignment[i];
    }

    public SymbolStats getPartition(int i) {
        return partitionSymbolStats[assignment[i]];
    }

    private final static class KeysIterator implements LazyIntIterator {

        int[] v;
        int n, i;

        public KeysIterator(int[] keys) {
            this.v = keys;
            this.n = keys.length;
            this.i = 0;
        }

        @Override
        public int nextInt() {
            if (i == n)
                return -1;
            return v[i++];
        }

        @Override
        public int skip(int i) {
            return 0;
        }
    }

}
