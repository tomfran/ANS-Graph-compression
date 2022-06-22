package it.tomfran.test;

import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import static it.unimi.dsi.webgraph.EFGraph.loadLongBigList;

public class PforTest {

    static int reqBits(int n){
        return (int) (Math.log(n)/Math.log(2)+1);
    }
    @Test
    public void pforWrite() throws IOException {
        CharSequence filename = "data/test/test.stream";
        final FileOutputStream os = new FileOutputStream((String) filename);
        final FileChannel streamChannel = os.getChannel();
        final LongWordOutputBitStream stream = new LongWordOutputBitStream(streamChannel, ByteOrder.nativeOrder());

        int[] nums = {119653, 273581, 48064, 12130, 23125, 3960, 8, 1, 2084, 59, 575, 559, 34463, 3283, 10, 10, 22,
                1055, 1, 25, 16, 3, 2, 262, 1307, 45, 20, 58, 180, 57, 20, 45, 20, 5491, 14057, 9927, 2357, 9699,
                45769, 56, 1411, 172517, 68326, 65557, 2, 251, 392801321, 0};

        int[] sortedNums = nums.clone();
        IntArrays.mergeSort(sortedNums);

        int maxBits = 0;
        for (int e : nums) maxBits = Math.max(maxBits, reqBits(e));
        System.out.println("Max bits: " + maxBits);
//
        int writtenBits = 0;
//        writtenBits += stream.writeGamma(nums.length);
//        writtenBits += stream.writeGamma(maxBits);
//        for (int e : nums) writtenBits += stream.append(e, maxBits);
//        System.out.println("NAIVE\nWritten: " + writtenBits + " bitsperel: " + (double)writtenBits / nums.length);

        int perc = 90;
        int len_limit = (int)((double) nums.length * perc/100);
        int limit_bit = reqBits(nums[Math.min(len_limit, nums.length-1)] + 1);
        System.out.println("Nums len: " + nums.length + " len_limit: " + limit_bit);

        writtenBits = 0;
        writtenBits += stream.writeGamma(nums.length);
        writtenBits += stream.writeGamma(limit_bit);
        writtenBits += stream.writeGamma(maxBits);
        IntArrayList l = new IntArrayList();
        for (int e : nums){
            if (reqBits(e) > limit_bit) {
                l.add(e);
                writtenBits += stream.append(1, 1);
                writtenBits += stream.append(e & ((1 << limit_bit) - 1), limit_bit);
            } else {
                writtenBits += stream.append(0, 1);
                writtenBits += stream.append(e, limit_bit);
            }
        }
        for (int e : l)
            writtenBits += stream.append(e>>>limit_bit, maxBits-limit_bit);

        System.out.println("\nPFOR\nWritten: " + writtenBits + " bitsperel: " + (double)writtenBits / nums.length);
        stream.close();
        streamChannel.close();
        os.close();

        LongBigArrayBigList modelsLongList = loadLongBigList(filename, ByteOrder.nativeOrder());
        int K = 0; // TODO: check this
        LongWordBitReader reader = new LongWordBitReader(modelsLongList, K);

        int listSize = (int) reader.readGamma();
        int limitBitR = (int) reader.readGamma();
        int maxBitR = (int) reader.readGamma();
        int rl[] = new int[listSize];
        IntArrayList toFill = new IntArrayList();

        for (int i = 0; i < listSize; i++) {
            if ((int) reader.readState(1) == 1)
                toFill.add(i);
            rl[i] = (int) reader.readState(limitBitR);
        }

        int overflowBits = maxBitR - limitBitR;
        for (int i = 0; i < toFill.size(); i++) {
            int index = toFill.getInt(i);
            int excess = (int) reader.readState(overflowBits);
            rl[index] |= excess << limit_bit;
        }

        assert rl.length == nums.length;
        for (int i = 0; i < rl.length; i++) {
            assert rl[i] == nums[i];
        }
    }

}
