package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.io.IOException;

public class AnsEncoder {

    private static final boolean DEBUG = false;
    public static final int NORM_POW = 50;
    /** Normalization threshold. */
    public static final long NORM_THS = (1L << (long) NORM_POW);
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
        if (DEBUG) System.out.println("ANSEncoder: encoding " + s);
        int fs, cs, symIndex;
        long j, r, stateTmp;
        // get freq and cumulative
        symIndex = model.symbolsMapping.get(s);
        fs = model.frequencies[symIndex];
        cs = model.cumulative[symIndex];
        // update the state
        j = Long.divideUnsigned(state, fs);
        r = Long.remainderUnsigned(state, fs);
        stateTmp = j * model.M + cs + r;
        // if the current state overflows, normalize previous and re-encode,
        // else assign the new state
        if (Long.compareUnsigned(stateTmp, NORM_THS) >= 0) {
            // this resets the state\
            normalize();
            j = Long.divideUnsigned(state, fs);
            r = Long.remainderUnsigned(state, fs);
            state = j * model.M + cs + r;
        } else {
            state = stateTmp;
        }
    }

    /**
     * Normalize the state to prevent overflows.
     */
    public void normalize() {
        // System.out.println("Normalization in progress");
        if (DEBUG) {
            if (Long.compareUnsigned(state, Long.MAX_VALUE) >= 0) {
                System.out.println("STATE IS BIGGER THAN EXPECTED");
            }
        }
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
        for (int i = normCount - 1; i >= 0; i--) {
            written += os.append(stateList.getLong(i), NORM_POW);
        }
        return written;
    }

    public void debugPrint(){
        System.out.println("AnsEncoder: state list: ");
        for (Long e : stateList){
            System.out.println("\t- " + e);
        }
    }

}
