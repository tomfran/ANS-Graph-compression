package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongOutputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map.Entry;

public class AnsEncoder {

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
    /** Stream to write overflows */
    protected LongOutputStream os;
    /** Normalization threshold, 2^32  */
    protected final long NORM_THS = (1L << 24);
    /** Number of normalizations, required to rebuild the encoder */
    protected int normCount;
    /** Arraylist of intermediate states, required to write them reversed*/
    protected ArrayList<Integer> stateParts;

    public AnsEncoder(SymbolStats s, LongOutputStream lis){
        // get mappings and frequencies
        symbolsMapping = s.symbolsMapping;
        invSymbolsMapping = s.invSymbolsMapping;
        frequencies = s.frequencies;

        // cumulative
        N = frequencies.length;
        cumulative = new int[N];
        cumulative[0] = 1;

        for (int i = 1; i < N; i++)
            cumulative[i] = cumulative[i-1] + frequencies[i-1];

        // sym
        M = s.total;
        sym = new int[M+1];
        int pos = 1;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < frequencies[i]; j++)
                sym[pos++] = i;


        }
        state = 0;
        normCount = 0;
        stateParts = new ArrayList<>();
        //out stream
        os = lis;
    }

    public void encode(int s){
        int fs, cs, symIndex;
        long j, r;
        // get freq and cumulative
        symIndex = symbolsMapping.get(s);
        fs = frequencies[symIndex];
        cs = cumulative[symIndex];
        // update the state
        j = Long.divideUnsigned(state, (long)fs);
        // if state exceeds 32 bits
        // write on output stream and reset state
        r = Long.remainderUnsigned(state, (long)fs);
        state = j*M + cs + r;
        if (Long.compareUnsigned(state, NORM_THS) >= 0) {
            normalize();
        }
    }

    public void normalize() {
//        System.out.println("Normalization in progress");
        if(Integer.compareUnsigned((int)state, Integer.MAX_VALUE) >= 0){
            System.out.println("STATE IS BIGGER THAN EXPECTED");
        }
        stateParts.add((int)state);
        state = 0L;
        normCount ++;
    }

    public void flush() {
        try {
            // we write all the required info to rebuild the decoder
            // number of symbols, symbol mappings, frequencies
            // M, cumulative and sym array will be built by the decoder
            os.writeInt(N, 31);
            for (Entry<Integer, Integer> e : symbolsMapping.entrySet()) {
                os.writeInt(e.getKey(), 31);
                os.writeInt(e.getValue(), 31);
            }
            for (int e : frequencies)
                os.writeInt(e, 31);

            // write the number of intermediate states
            // then the state parts in reversed order
            os.writeInt(normCount, 31);
            for (int i = normCount-1; i >= 0; i--) {
//                System.out.println("ENC - writing next state: " + stateParts.get(i) + " -> " + Long.toBinaryString(stateParts.get(i)));
                os.writeInt(stateParts.get(i), 31);
            }
            os.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void encodeAll(Iterable<Integer> l){
        for (Integer e : l)
            encode(e);
        // last normalization required, might be unnecessary
        normalize();
        // flush the state in reverse order on the stream
        flush();
    }



    public void debugPrint(){
        System.out.println("---- Symbol mapping --------");
        for (Entry<Integer, Integer> e : symbolsMapping.entrySet())
            System.out.println(e.getKey() + "->" + e.getValue());

        System.out.println("---- Inv Symbol mapping ----");
        for (Entry<Integer, Integer> e : invSymbolsMapping.entrySet())
            System.out.println(e.getKey() + "->" + e.getValue());

        System.out.println("---- Frequencies -----------");
        for (int i = 0; i < N; i++)
            System.out.print(frequencies[i] + " ");
        System.out.println();

        System.out.println("---- Cumulative ------------");
        for (int i = 0; i < N; i++)
            System.out.print(cumulative[i] + " ");
        System.out.println();

//        System.out.println("---- Sym array -------------");
//        for (int i = 0; i < M+1; i++)
//            System.out.print(sym[i] + " ");
//        System.out.println();
    }

    public void printState(){
        double s = (state == 0)? 0 : Math.ceil(Math.log(state));
        System.out.println("ANS state -> " + state + " " + s + " bits");
    }

}
