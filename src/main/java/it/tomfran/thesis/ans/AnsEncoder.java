package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.io.IOException;

public class AnsEncoder {

    private static final boolean DEBUG = false;
    private static final boolean DUMPDEBUG = false;
//    public static final int NORM_POW = 63;
    /** Normalization threshold. */
//    public static final long NORM_THS = (1L << (long) NORM_POW);
    /** Ans model used for encoding. */
    protected AnsModel model;
    /** Normalization count. */
    public int normCount;
    /** Current state. */
    protected long state;
    /** List of states. */
    public LongArrayList stateList;


    /**
     * Build an encoder using a given AnsModel.
     * @param m
     */
    public AnsEncoder(AnsModel m) {
        model = m;
        state = 0;
        normCount = 0;
        stateList = new LongArrayList();
    }

    /**
     * Encode an int, this updates the state and normalize accordingly.
     * @param s The int to encode.
     */
    public void encode(int s) {
        int fs, cs, symIndex;
        long j, r, stateTmp;
        // get freq and cumulative
        symIndex = model.getSymbolMapping(s);
        fs = model.getFrequency(symIndex);
        cs = model.getCumulative(symIndex);
        // update the state
        j = Long.divideUnsigned(state, fs);
        r = Long.remainderUnsigned(state, fs);

        try {
            long res = Math.multiplyExact(j, model.M);
            res = Math.addExact(res, (cs+r));
            state = res;
        } catch (ArithmeticException e) {
//            System.out.println("OVERFLOW PREVENTED");
            normalize();
            j = Long.divideUnsigned(state, fs);
            r = Long.remainderUnsigned(state, fs);
            state = j * model.M + cs + r;
        }
    }

    /**
     * Normalize the state to prevent overflows.
     */
    public void normalize() {
        stateList.add(state);
        state = 0L;
        normCount++;
    }

    /**
     * Encode an array of ints.
     * @param l The array to encode
     * @param length lenght of the list.
     */
    public void encodeAll(int[] l, int length) {
        for (int i = 0; i < length; i++)
            encode(l[i]);
        // last normalization to append the last state
        normalize();
    }

    /**
     * Write the encoder info and states on a LongWordOutputBitStream.
     * @param os LongWordOutputBitStream to write.
     * @param modelId The model id corresponding to this encoder.
     * @return Number of bits written.
     * @throws IOException
     */
    public long dump(LongWordOutputBitStream os, int modelId) throws IOException {
        long written = 0;
        written += os.writeGamma(modelId);
        written += os.writeGamma(normCount);
        if (DUMPDEBUG) debugPrint();
        for (int i = normCount - 1; i >= 0; i--) {
            written += os.append(stateList.getLong(i), 63);
        }
        // this could prevent the erorr while reading the state
        written += os.writeGamma(0);
        return written;
    }

    public void debugPrint(){
        System.out.println("AnsEncoder: state list: ");
        for (Long e : stateList){
            System.out.println("\t- " + e);
        }
    }

}
