package it.tomfran.thesis.ans;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class AnsDecoder {

    protected AnsModel model;
    protected int stateCount;
    protected LongArrayList stateList;
    protected int stateIndex;

    public AnsDecoder(AnsModel m, LongArrayList sl, int sc) {
        model = m;
        stateIndex = 0;
        stateList = sl;
        stateCount = sc;
    }

    public IntArrayList decodeAll() {
        IntArrayList ret = new IntArrayList();
        int e;
        while ((e = decode()) != -1)
            ret.add(e);

        return ret;
    }

    public int decode() {

        if (stateIndex == stateCount) return -1;

        int fs, cs, r, symIndex;
        long state, j;
        state = stateList.getLong(stateIndex);
        // remainder to identity symbol
        r = (int) (1 + (Long.remainderUnsigned(state - 1, model.M)));
        // get freq and cumulative
        symIndex = model.sym[r];
        fs = model.frequencies[symIndex];
        cs = model.cumulative[symIndex];
        // update the state
        j = Long.divideUnsigned(state - r, model.M);
        stateList.set(stateIndex, j * fs - cs + r);

        // if current state is over, change to next one
        if (Long.compareUnsigned(stateList.getLong(stateIndex), 0L) == 0)
            stateIndex++;

        return model.invSymbolsMapping.get(symIndex);
    }

    public void debugPrint(){
        System.out.println("STATES: ");
        for (Long e : stateList){
            System.out.println("- " + e);
        }
    }

}
