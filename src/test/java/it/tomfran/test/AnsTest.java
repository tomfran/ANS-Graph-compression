package it.tomfran.test;

import it.tomfran.thesis.ans.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;


public class AnsTest {


    static int len = 100000;
    static int[] numList = getRandom(len);
    static int maxNum = 50;
    static int runPerModel = 10;

    public static int[] getRandom(int n) {
        int[] choices = {1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 20, 22, 50};
        int[] l = new int[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++)
            l[i] = choices[Integer.max(0, (rand.nextInt() % choices.length))];
        Arrays.sort(l);
        return l;
    }

    public static int[] reversed(int[] arr) {
        int n = arr.length;
        int[] ret = new int[n];

        for (int i = 0; i < n; i++)
            ret[i] = arr[n - 1 - i];

        return ret;
    }

    public static void decodeCheck(AnsDecoder dec, int[] l, int len) {
        int e, i;
        i = 0;
        do {
            e = dec.decode();
            assert e == -1 || (e == l[i]);
            i++;
        } while (e != -1);
        assert i == len + 1;
    }

    public static AnsDecoder buildDecoder(LongArrayList a, int len, IntArrayList escapedSymbol, int size, AnsModel m1) {
        LongArrayList a1 = new LongArrayList();
        for (int i = len - 1; i >= 0; i--)
            a1.add(a.getLong(i));

        IntArrayList a2 = new IntArrayList();
        for (int i = size - 1; i >= 0; i--)
            a2.add(escapedSymbol.getInt(i));

        return new AnsDecoder(m1, a1, len, a2, m1.escapeIndex);
    }

    public static AnsEncoder buildEncoder(AnsModel m, int[] l, int len) {

        AnsEncoder ans = new AnsEncoder(m);
        ans.encodeAll(reversed(l), len);
        return ans;
    }

    public static double getBits(AnsEncoder e) {
        return 64 * e.normCount / (double) len;
    }

    @Test
    public void optimalModel() {
        System.out.println("Optimal model test");
        double avgBits = 0;
        for (int i = 0; i < runPerModel; i++) {
            SymbolStats s = new SymbolStats(numList, len, 10, 2);
            AnsModel m = new AnsModel(s);
            AnsEncoder ans = buildEncoder(m, numList, len);
            AnsDecoder dec = buildDecoder(ans.stateList, ans.normCount, ans.escapedSymbolList, ans.escapedSymbolList.size(), m);
            decodeCheck(dec, numList, len);
            avgBits += getBits(ans);
        }
        avgBits /= runPerModel;
        System.out.println("Average bits per element: " + avgBits);
    }

}
