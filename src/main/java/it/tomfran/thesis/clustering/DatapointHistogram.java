package it.tomfran.thesis.clustering;


import it.tomfran.thesis.ans.SymbolStats;
import it.unimi.dsi.fastutil.ints.*;

public class DatapointHistogram {

    static final int DEFAULT_FREQ = 1;
    private static final boolean DEBUG = false;

    protected Int2IntOpenHashMap symbolsMapping;
    protected int[] frequencies;
    protected int precision;


    public DatapointHistogram(SymbolStats s) {
        this.symbolsMapping = s.symbolsMapping;
        this.frequencies = s.frequencies;
        this.precision = s.precision;
    }

    public DatapointHistogram(Int2IntOpenHashMap symbolsMapping, int[] frequencies, int precision) {
        this.symbolsMapping = symbolsMapping;
        this.frequencies = frequencies;
        this.precision = precision;
    }

    static DatapointHistogram buildCentroidFromCluster(DatapointHistogram[] points, int precision) {

        if (DEBUG)
            System.out.println("Build from centroid, received " + points.length + " points.");

        // get all the symbols in the points
        IntSet mergedKeys = new IntOpenHashSet();

        for (DatapointHistogram p : points)
            mergedKeys.addAll(p.symbolsMapping.keySet());

        // build the centroid probability distribution
        // as the average of all the distributions
        Int2IntOpenHashMap symbolsMapping = new Int2IntOpenHashMap();

        int pos = 0, n = points.length, k = mergedKeys.size();
        double[] prob = new double[k];
        for (int e : mergedKeys) {
            for (DatapointHistogram p : points)
                prob[pos] += p.getSymProbability(e);
            prob[pos] /= n;
            symbolsMapping.put(e, pos);
            pos++;
        }
        if (DEBUG) {
            System.out.println("New probability: ");
            for (double p : prob)
                System.out.print(p + " ");
        }
        // build frequency, scaling probability to precision
        // need to compute a new precision as the total might change for the Math.max
        int[] frequencies = new int[k];
        int newPrecision = 0;
        for (int i = 0; i < k; i++) {
            frequencies[i] = Math.max(1, (int) (prob[i] * precision));
            newPrecision += frequencies[i];
        }
        if (DEBUG) {
            System.out.println("\nNew frequencies: ");
            for (int p : frequencies)
                System.out.print(p + " ");
            System.out.println();
        }
        // return the new computed centroid
        return new DatapointHistogram(symbolsMapping, frequencies, newPrecision);
    }

    private double getSymProbability(int sym) {
        int pos = symbolsMapping.getOrDefault(sym, -1);
        int f = (pos == -1) ? DEFAULT_FREQ : frequencies[pos];
        return (double) f / precision;
    }

    public double KLDivergence(DatapointHistogram h) {
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

    public double distance(DatapointHistogram h) {
        return (KLDivergence(h) + h.KLDivergence(this)) / 2;
    }

    public DatapointHistogram copy() {
        return new DatapointHistogram(symbolsMapping.clone(), frequencies.clone(), precision);
    }

    @Override
    public String toString() {
        String s = "Datapoint, symlist: \n";

        for (int e : symbolsMapping.keySet())
            s += "\t -" + e + ": " + getSymProbability(e) + "\n";
        return s;
    }
}
