package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnsDecoder {
    /** Sum of frequencies: might change it to a power of two and approximate */
    protected int M;
    /** Number of symbols */
    protected int N;
    /** Symbols to index mapping */
    protected HashMap<Integer, Integer> symbolsMapping;
    /** Index to symbols mapping */
    protected HashMap<Integer, Integer> invSymbolsMapping;
    /** Symbol frequencies */
    protected int[] frequencies;
    /** Cumulative array */
    protected int[] cumulative;
    /** Symbol array */
    protected int[] sym;
    /* encoder state */
    protected long state;
    /** Stream to read encoder info */
    protected LongInputStream is;
    /** Number of normalizations, required to rebuild the encoder */
    protected int normCount;

    public AnsDecoder(LongInputStream lis) {
        is = lis;
        initializeVariables();
    }

    private void initializeVariables() {
        try {
            // we read in order:
            // N
            // 2N int representing mappings,
            // N frequencies
            // number of normalizations
            // initial state

            N = is.readInt(31);
            symbolsMapping = new HashMap<>();
            invSymbolsMapping = new HashMap<>();
            int symbol, index;
            for (int i = 0; i < N; i++) {
                symbol = is.readInt(31);
                index = is.readInt(31);
                symbolsMapping.put(symbol, index);
                invSymbolsMapping.put(index, symbol);
            }
            // build frequencies and cumulative array
            M = 0;
            frequencies = new int[N];
            cumulative = new int[N];
            cumulative[0] = 1;
            for (int i = 0; i < N; i++) {
                frequencies[i] = is.readInt(31);
                M += frequencies[i];
                if (i > 0)
                    cumulative[i] = cumulative[i-1] + frequencies[i-1];
            }
            // build sym array
            sym = new int[M+1];
            int pos = 1;
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < frequencies[i]; j++)
                    sym[pos++] = i;
            }
            // initialize normalization count and initial state
            normCount = is.readInt(31) - 1;
            readNextState();

        } catch (IOException e) {
            System.out.println("Could not initialize variables in decoder");
        }
    }

    public List<Integer> decodeAll(){
        ArrayList<Integer> ret = new ArrayList<>();
        for (int i = 0; i < normCount; i++) {
            while(Long.compareUnsigned(state, 0L) > 0)
                ret.add(decode());
            readNextState();
        }
        return ret;
    }

    private void readNextState() {
        try {
            state = is.readInt(31);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("DEC - Reading next state: " + state + " -> " + Long.toBinaryString(state));

    }

    public int decode(){
        int fs, cs, r, symIndex;
        long j;
        // remainder to identity symbol
        r = (int) (1 + (Long.remainderUnsigned(state-1, M)));
        // get freq and cumulative
        symIndex = sym[r];
        fs = frequencies[symIndex];
        cs = cumulative[symIndex];
        // update the state
        j = Long.divideUnsigned(state-r, M);
//        j = (state - r) / M;
        state = j * fs - cs + r;

        return invSymbolsMapping.get(symIndex);
    }

    public void debugPrint(){
        System.out.println("---- Symbol mapping --------");
        for (Map.Entry<Integer, Integer> e : symbolsMapping.entrySet())
            System.out.println(e.getKey() + "->" + e.getValue());

        System.out.println("---- Inv Symbol mapping ----");
        for (Map.Entry<Integer, Integer> e : invSymbolsMapping.entrySet())
            System.out.println(e.getKey() + "->" + e.getValue());

        System.out.println("---- Frequencies -----------");
        for (int i = 0; i < N; i++)
            System.out.print(frequencies[i] + " ");
        System.out.println();

        System.out.println("---- Cumulative ------------");
        for (int i = 0; i < N; i++)
            System.out.print(cumulative[i] + " ");
        System.out.println();

        System.out.println("---- Sym array -------------");
        for (int i = 0; i < M+1; i++)
            System.out.print(sym[i] + " ");
        System.out.println();
    }

    public void printState(){
        double s = (state == 0)? 0 : Math.ceil(Math.log(state));
        System.out.println("ANS state -> " + state + " " + s + " bits");
    }
}
