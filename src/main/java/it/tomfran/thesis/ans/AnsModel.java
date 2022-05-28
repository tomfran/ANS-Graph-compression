package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList;

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
    protected EliasFanoIndexedMonotoneLongBigList sym;

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


    public void buildCumulativeSymbols() {
        cumulative = new int[N];
        cumulative[0] = 1;

        for (int i = 1; i < N; i++)
            cumulative[i] = cumulative[i - 1] + frequencies[i - 1];

        // sym
        sym = new EliasFanoIndexedMonotoneLongBigList(new IntArrayList(cumulative));
    }

    public int getSymbolMapping (int sym) {
        return symbolsMapping.get(sym);
    }

    public int getInvSymbolMapping (int sym) {
        return invSymbolsMapping.get(sym);
    }

    public int getFrequency(int symIndex) {
        return frequencies[symIndex];
    }

    public int getCumulative(int symIndex) {
        return cumulative[symIndex];
    }

    public int getRemainderSym(int r) {
        return (int) sym.weakPredecessorIndex(r);
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

        System.out.println("---- SYM ------------");
        System.out.print(sym);
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

        // write N, and N symbols
        written += modelStream.writeGamma(N);
        for (int i = 0; i < N; i++) {
            written += modelStream.writeGamma(invSymbolsMapping.get(i));
        }
        // write frequencies in reverse order by gap
        int prev = 0;
        for (int i = frequencies.length-1; i >= 0; i--) {
            written += modelStream.writeGamma(frequencies[i] - prev);
            prev = frequencies[i];
        }

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

        int a;
        for (int i = 0; i < m.N; i++) {
            a = (int) br.readGamma();
            m.symbolsMapping.put(a, i);
            m.invSymbolsMapping.put(i, a);
        }
        // read frequencies, reverse by gap
        m.frequencies = new int[m.N];
        m.M = 0;
        int prev = 0;
        for (int i = m.N-1; i >= 0; i--) {
            m.frequencies[i] = (int) br.readGamma() + prev;
            prev = m.frequencies[i];
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
        // TODO: check clone in ELIASfano..
        m.sym = sym;
        return m;
    }

}
