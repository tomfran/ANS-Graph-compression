package it.tomfran.thesis.ans;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class AnsDecoder {

    /** Escaped symbols list. */
    public IntArrayList escapedSymbols;
    /** Position of the decoded escape in escapedSymbols. */
    public int escapedSymbolPos;
    /** Escape index in the frequency map. */
    public int escapeIndex;
    /** Model used by this decoder. */
    protected AnsModel model;
    /** Number of states to decode. */
    protected int stateCount;
    /** List of states. */
    protected LongArrayList stateList;
    /** index of the current state. */
    protected int stateIndex;


    /**
     * Build an And decoder.
     *
     * @param m  AnsModel to use
     * @param sl Stale list
     * @param sc State count
     * @param es Escape list
     * @param ei Escape index
     */
    public AnsDecoder(AnsModel m, LongArrayList sl, int sc, IntArrayList es, int ei) {
        model = m;
        stateIndex = 0;
        stateList = sl;
        stateCount = sc;
        escapedSymbols = es;
        escapedSymbolPos = 0;
        escapeIndex = ei;
    }

    /**
     * Decode all encoded ints.
     *
     * @return IntArrayList with the decoded sequence.
     */
    public IntArrayList decodeAll() {
        IntArrayList ret = new IntArrayList();
        int e;
        while ((e = decode()) != -1)
            ret.add(e);

        return ret;
    }

    /**
     * Decode a single int from the state list.
     *
     * @return Decoded int.
     */
    public int decode() {

        if (stateIndex == stateCount) return -1;

        int fs, cs, r, symIndex;
        long state, j;
        state = stateList.getLong(stateIndex);
        // remainder to identity symbol
        r = (int) (1 + (Long.remainderUnsigned(state - 1, model.M)));
        // get freq and cumulative
        symIndex = model.getRemainderSym(r);
        fs = model.getFrequency(symIndex);
        cs = model.getCumulative(symIndex);
        // update the state
        j = Long.divideUnsigned(state - r, model.M);
        stateList.set(stateIndex, j * fs - cs + r);

        // if current state is over, change to next one
        if (Long.compareUnsigned(stateList.getLong(stateIndex), 0L) == 0)
            stateIndex++;

        if (symIndex == escapeIndex) return escapedSymbols.getInt(escapedSymbolPos++);

        return model.getInvSymbolMapping(symIndex);
    }

}
