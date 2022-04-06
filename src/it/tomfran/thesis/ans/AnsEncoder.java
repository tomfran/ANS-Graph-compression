package it.tomfran.thesis.ans;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnsEncoder {

    /* sum of frequencies: might change it to a power of two and approximate */
    protected int M;
    /* number of symbols */
    protected int N;
    /* sym to int and inverted */
    protected HashMap<Integer, Integer> symbolsMapping;
    protected HashMap<Integer, Integer> invSymbolsMapping;
    /* sym frequencies, cumulative and sym array to encode and decode */
    protected int[] frequencies;
    protected int[] cumulative;
    protected int[] sym;
    /* encoder state */
    protected long state;

    public AnsEncoder(SymbolStats s){
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
    }

    public void encode(int s){
        int fs, cs, symIndex;
        long j, r;
        // get freq and cumulative
        symIndex = symbolsMapping.get(s);
        fs = frequencies[symIndex];
        cs = cumulative[symIndex];
        // update the state
        j = state / (long)fs;
        r = state % (long)fs;
        state = j*M + cs + r;
    }

    public int decode(){
        int fs, cs, r, symIndex;
        long j;
        // remainder to identity symbol
        r = (int) (1 + ((state-1) % M));
        // get freq and cumulative
        symIndex = sym[r];
        fs = frequencies[symIndex];
        cs = cumulative[symIndex];
        // update the state
        j = (state - r) / M;
        state = j * fs - cs + r;

        return invSymbolsMapping.get(symIndex);
    }

    public void encodeAll(Iterable<Integer> l){
        for (Integer e : l)
            encode(e);
    }

    public List<Integer> decodeAll(){
        ArrayList<Integer> ret = new ArrayList<>();
        while(state > 0)
            ret.add(decode());
        return ret;
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
