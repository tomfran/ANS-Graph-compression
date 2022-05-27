package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;

import java.io.IOException;

public class AnsModelOrderStatistic extends AnsModel {

    private static final boolean DEBUG = false;
    private static final boolean REMAINDERDEBUG = false;
    private final int medFs;
    private final int tqFs;
    private final int otherFs;
    private final int medM;
    private final int tqM;
    protected int median;
    protected int thirdquartile;
    protected int maxValue;

    public AnsModelOrderStatistic(int median, int thirdquartile, int maxValue, int prec) {
        this.median = median;
        this.thirdquartile = thirdquartile;
        this.maxValue = maxValue;
        // this ensures that M can contain the max
        this.M = Math.max(prec, 8 * maxValue);

        assert maxValue <= (Integer.MAX_VALUE / 8);

        // usually, numbers before the median have a frequency sum of 0.5,
        // the ones laying after the median and before the third quartile 0.25,
        // the same for the ones after the third quartile.
        // A few corner cases appears if median, third quartile, and max are equals
        // in some way.
        if (median == thirdquartile && median == maxValue) {
            medFs = (int) (M / (double) (median + 1)); // give all M to median
            tqFs = 0;
            otherFs = 0;
            medM = M;
            tqM = 0;
        } else if (median == thirdquartile) {
            medFs = (int) (0.75 * M / (double) (median + 1)); // give 0.75 M to median
            tqFs = 0;
            otherFs = (int) (0.25 * M / (double) (maxValue - thirdquartile)); // give 0.25 to others
            medM = (int) (0.75 * M);
            tqM = medM;
        } else if (thirdquartile == maxValue) {
            medFs = (int) (0.5 * M / (double) (median + 1)); // give 0.5 M to median
            tqFs = (int) (0.5 * M / (double) (thirdquartile - median)); // give 0.5 to third quartile
            otherFs = 0;
            medM = (int) (0.5 * M);
            tqM = M;
        } else {
            medFs = (int) (0.5 * M / (double) (median + 1));
            tqFs = (int) (0.25 * M / (double) (thirdquartile - median));
            otherFs = (int) (0.25 * M / (double) (maxValue - thirdquartile));
            medM = (int) (0.5 * M);
            tqM = (int) (0.75 * M);
        }
    }

    public static AnsModel rebuildModel(LongWordBitReader br) {

        int median, thirdquartile, max;
        median = (int) br.readGamma();
        thirdquartile = median + (int) br.readGamma();
        max = thirdquartile + (int) br.readGamma();

        return new AnsModelOrderStatistic(median, thirdquartile, max, 1024);
    }

    @Override
    public int getFrequency(int sym) {
        if (sym <= median)
            return medFs;

        if (sym <= thirdquartile)
            return tqFs;

        return otherFs;
    }

    @Override
    public int getCumulative(int sym) {

        if (sym <= median)
            return medFs * (sym) + 1;

        if (sym <= thirdquartile)
            return medM + tqFs * (sym - median - 1) + 1;

        return tqM + otherFs * (sym - thirdquartile - 1) + 1;
    }

    @Override
    public int getSymbolMapping(int sym) {
        return sym;
    }

    @Override
    public int getInvSymbolMapping(int sym) {
        return sym;
    }

    @Override
    public int getRemainderSym(int r) {
        if (r <= medM)
            return (r - 1) / medFs;

        if (r <= tqM)
            return median + (((r - 1) - medM) / tqFs) + 1;

        return thirdquartile + (((r - 1) - tqM) / otherFs) + 1;

    }

    @Override
    public long dump(LongWordOutputBitStream modelStream) throws IOException {
        long written = 0;
        written += modelStream.writeGamma(median);
        written += modelStream.writeGamma(thirdquartile - median);
        written += modelStream.writeGamma(maxValue - thirdquartile);
        return written;
    }

    @Override
    public AnsModel copy() {
        return new AnsModelOrderStatistic(median, thirdquartile, maxValue, M);
    }
}
