package it.tomfran.thesis.ans;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class AnsDecoder {

    /** Model used by the decoder. */
    protected AnsModel model;
    /** Number of states to decode. */
    protected int stateCount;
    /** List of states. */
    protected LongArrayList stateList;
    /** index of the current state. */
    protected int stateIndex;

    /**
     * Build a decoder starting from a model, a state list and a state count.
     *
     * @param m AnsModel to use.
     * @param sl State list.
     * @param sc Number of states.
     */
    public AnsDecoder(AnsModel m, LongArrayList sl, int sc) {
        model = m;
        stateIndex = 0;
        stateList = sl;
        stateCount = sc;
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

        return model.getInvSymbolMapping(symIndex);
    }

    public void debugPrint(){
        System.out.println("STATES: ");
        for (Long e : stateList){
            System.out.println("- " + e);
        }
    }

}
