package it.tomfran.thesis;

import it.tomfran.thesis.ans.*;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.junit.jupiter.api.Test;

import java.util.Random;


public class AnsTest {


    static int len = 100000;
    static int[] numList = getRandom(len);
    static int maxNum = 50;

    public static int[] getRandom(int n) {
        int[] choices = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 20, 22, 50};
        int[] l = new int[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++)
            l[i] = choices[Integer.max(0, (rand.nextInt() % choices.length))];
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

    public static AnsDecoder buildDecoder(LongArrayList a, int len, AnsModel m1) {
        LongArrayList a1 = new LongArrayList();
        for (int i = len - 1; i >= 0; i--)
            a1.add(a.getLong(i));

        return new AnsDecoder(m1, a1, len);
    }

    public static AnsEncoder buildEncoder(AnsModel m, int[] l, int len) {

        AnsEncoder ans = new AnsEncoder(m);
        ans.encodeAll(reversed(l), len);

        System.out.println("Encoded " + len + " symbols in " + ans.normCount + " states, " + 64 * ans.normCount / (float) len + " bits per element.");
        return ans;
    }

    @Test
    public void equiprobableModel() {
        System.out.println("Equiprobable model test");

        AnsModelEquiprobable m = new AnsModelEquiprobable(maxNum);

        AnsEncoder ans = buildEncoder(m, numList, len);
        AnsDecoder dec1 = buildDecoder(ans.stateList, ans.normCount, m);
        decodeCheck(dec1, numList, len);
    }

    @Test
    public void optimalModel() {
        System.out.println("Optimal model test");

        SymbolStats s = new SymbolStats(numList, len, 10);
        AnsModel m = new AnsModel(s);

        AnsEncoder ans = buildEncoder(m, numList, len);
        AnsDecoder dec = buildDecoder(ans.stateList, ans.normCount, m);
        decodeCheck(dec, numList, len);
    }

}
