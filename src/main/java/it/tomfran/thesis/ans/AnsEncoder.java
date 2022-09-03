package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.io.IOException;

public class AnsEncoder {

    /** Normalization count. */
    public int normCount;
    /** List of states. */
    public LongArrayList stateList;
    /** Minimum escape sym. */
    public int escapeIndex;
    /** List of escaped symbolds. */
    public IntArrayList escapedSymbolList;
    /** Ans model used for encoding. */
    protected AnsModel model;
    /** Current state. */
    protected long state;
    /** Maximum seen binary magnitute. */
    private int maxBitsEscape;


    /**
     * Build an encoder using a given AnsModel.
     *
     * @param m
     */
    public AnsEncoder(AnsModel m) {
        model = m;
        state = 0;
        normCount = 0;
        stateList = new LongArrayList();
        escapedSymbolList = new IntArrayList();
        escapeIndex = m.escapeIndex;
        maxBitsEscape = 0;
    }

    /**
     * Encode an int, this updates the state and normalize accordingly.
     *
     * @param s The int to encode.
     */
    public void encode(int s) {
        int fs, cs, symIndex;
        long j, r;
        // get freq and cumulative
        symIndex = model.getSymbolMapping(s);

        if (symIndex == escapeIndex) {
            escapedSymbolList.add(s);
            maxBitsEscape = Math.max(maxBitsEscape, getRequiredBits(s));
        }

        fs = model.getFrequency(symIndex);
        cs = model.getCumulative(symIndex);
        // update the state
        j = Long.divideUnsigned(state, fs);
        r = Long.remainderUnsigned(state, fs);

        long newState = j * model.M;
        long upperBits = Math.multiplyHigh(j, model.M);
        // if the multiplication or the sum overflows, normalize

        if (upperBits == 0 && (Long.MAX_VALUE - newState) > (cs + r)) {
            state = newState + cs + r;
        } else {
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
     *
     * @param l      The array to encode
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
     *
     * @param os      LongWordOutputBitStream to write.
     * @param modelId The model id corresponding to this encoder.
     * @return Number of bits written.
     * @throws IOException
     */
    public long dump(LongWordOutputBitStream os, int modelId) throws IOException {
        // write model id and states
        long written = 0;

        written += os.writeGamma(modelId);
        written += os.writeGamma(normCount);
        for (int i = normCount - 1; i >= 0; i--) written += os.append(stateList.getLong(i), 63);

        // escapes
        // compute required bits by each escape
        if (escapedSymbolList.size() == 0) {
            written += os.writeGamma(0);
            return written;
        }

        int limitBit = findBestLimit();
        // write size, limitBits, max-limit
        written += os.writeGamma(escapedSymbolList.size());
        written += os.writeGamma(limitBit);
        written += os.writeGamma(maxBitsEscape - limitBit);

        // write each escape
        IntArrayList overflow = new IntArrayList();
        int num;
        for (int i = escapedSymbolList.size() - 1; i >= 0; i--) {
            // if overflow, write a one and the lower bits
            num = escapedSymbolList.getInt(i);
            if ((Math.max(1, (int) (Math.log(num) / Math.log(2) + 1))) > limitBit) {
                overflow.add(num);
                written += os.append(1, 1);
//                written += os.append(num & ((1L << limitBit) - 1), limitBit);
                written += os.append(num & ((1L << maxBitsEscape) - 1), maxBitsEscape);
            } else {
                written += os.append(0, 1);
                written += os.append(num, limitBit);
            }
        }
//        // write upper bits of overflow
//        for (int e : overflow)
//            written += os.append(e >>> limitBit, maxBitsEscape - limitBit);
        return written;
    }

    private int getRequiredBits(int n) {
        return Math.max(1, (int) (Math.log(n) / Math.log(2) + 1));
    }

    private int findBestLimit() {

        if (escapedSymbolList.size() == 1)
            return getRequiredBits(escapedSymbolList.getInt(0));

        int[] reqBits = new int[escapedSymbolList.size()];
        int i = 0;
        for (int e : escapedSymbolList) {
            reqBits[i] = getRequiredBits(e);
            i++;
        }
        // chose bit that encodes perc nums
        // this could be faster
        IntArrays.mergeSort(reqBits);
        int bestScore = maxBitsEscape * escapedSymbolList.size();
        int bestLimit = maxBitsEscape;
        int score = 0;
        for (i = 0; i < reqBits.length; i++) {
            if (i > 1 && (reqBits[i] == reqBits[i - 1]))
                continue;
            score = escapedSymbolList.size() * reqBits[i] + escapedSymbolList.size();
            for (int j = 0; j < reqBits.length; j++) {
                if (reqBits[j] > reqBits[i]) {
                    score += maxBitsEscape - reqBits[i];
                }
            }
            if (score < bestScore) {
                bestScore = score;
                bestLimit = reqBits[i];
            }
        }
        return bestLimit;
    }

}
