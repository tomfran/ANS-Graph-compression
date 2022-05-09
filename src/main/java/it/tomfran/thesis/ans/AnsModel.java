package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.io.IOException;
import java.util.Map;

public class AnsModel {

    /** Sum of frequencies, usually a power of two. */
    protected int M;
    /** Number of symbols. */
    protected int N;
    /** Symbols to index mapping. */
    protected Int2IntOpenHashMap symbolsMapping;
    /** Index to symbols mapping. */
    protected Int2IntOpenHashMap invSymbolsMapping;
    /** Symbol frequencies. */
    protected int[] frequencies;
    /** Cumulative array. */
    protected int[] cumulative;
    /** Symbols array. */
    protected int[] sym;

    public AnsModel() {
    }

    /**
     * Build an Ans model from a symbol stats object.
     * @param s SymbolStats instance
     */
    public AnsModel(SymbolStats s) {
        // get mappings and frequencies
        symbolsMapping = s.symbolsMapping;
        invSymbolsMapping = s.invSymbolsMapping;
        frequencies = s.frequencies;

        // cumulative
        N = frequencies.length;
        M = s.total;
        buildCumulativeSymbols();
    }


    private void buildCumulativeSymbols() {
        cumulative = new int[N];
        cumulative[0] = 1;

        for (int i = 1; i < N; i++)
            cumulative[i] = cumulative[i - 1] + frequencies[i - 1];

        // sym
        sym = new int[M + 1];
        int pos = 1;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < frequencies[i]; j++)
                sym[pos++] = i;
        }
    }

    public void debugPrint() {
        System.out.println("---- Symbol mapping --------");
        for (Map.Entry<Integer, Integer> e : symbolsMapping.int2IntEntrySet())
            System.out.println(e.getKey() + "->" + e.getValue());

        System.out.println("---- Inv Symbol mapping ----");
        for (Map.Entry<Integer, Integer> e : invSymbolsMapping.int2IntEntrySet())
            System.out.println(e.getKey() + "->" + e.getValue());

        System.out.println("---- Frequencies -----------");
        for (int i = 0; i < N; i++)
            System.out.print(frequencies[i] + " ");
        System.out.println();

        System.out.println("---- Cumulative ------------");
        for (int i = 0; i < N; i++)
            System.out.print(cumulative[i] + " ");
        System.out.println();
    }

    /**
     * Write the model info on a LongWordOutputBitStream.
     *
     * @param modelStream stream to write info.
     * @return number of bits written.
     * @throws IOException
     */
    public long dump(LongWordOutputBitStream modelStream) throws IOException {
        long written = 0;

        // write N, and 2N values for the mapping
        written += modelStream.writeGamma(N);
        for (Int2IntMap.Entry e : symbolsMapping.int2IntEntrySet()) {
            written += modelStream.writeGamma(e.getIntKey());
            written += modelStream.writeGamma(e.getIntValue());
        }
        // write frequencies
        for (int e : frequencies)
            written += modelStream.writeGamma(e);


        return written;
    }

    /**
     * Rebuild an Ans model reading info from a LongWordBitReader
     * @param br LongWordBitReader with model info.
     * @return AnsModel instantiated reading from the reader.
     */
    public static AnsModel rebuildModel(LongWordBitReader br) {

        AnsModel m = new AnsModel();
        // read N
        m.N = (int) br.readGamma();
        // read symbols mappings
        m.symbolsMapping = new Int2IntOpenHashMap();
        m.invSymbolsMapping = new Int2IntOpenHashMap();

        int a, b;
        for (int i = 0; i < m.N; i++) {
            a = (int) br.readGamma();
            b = (int) br.readGamma();
            m.symbolsMapping.put(a, b);
            m.invSymbolsMapping.put(b, a);
        }
        // read frequencies
        m.frequencies = new int[m.N];
        m.M = 0;
        for (int i = 0; i < m.N; i++) {
            m.frequencies[i] = (int) br.readGamma();
            m.M += m.frequencies[i];
        }

        m.buildCumulativeSymbols();

        return m;
    }

    /**
     * Build a copy of the current model.
     * @return Copy of the current model.
     */
    public AnsModel copy() {

        AnsModel m = new AnsModel();
        m.N = N;
        m.M = M;
        m.symbolsMapping = symbolsMapping.clone();
        m.invSymbolsMapping = invSymbolsMapping.clone();
        m.frequencies = frequencies.clone();
        m.cumulative = cumulative.clone();
        m.sym = sym.clone();
        return m;

    }
}
