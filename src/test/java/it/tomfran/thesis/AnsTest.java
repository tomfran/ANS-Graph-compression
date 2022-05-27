package it.tomfran.thesis;

import com.sun.security.jgss.GSSUtil;
import it.tomfran.thesis.ans.*;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
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

    public static AnsDecoder buildDecoder(LongArrayList a, int len, AnsModel m1) {
        LongArrayList a1 = new LongArrayList();
        for (int i = len - 1; i >= 0; i--)
            a1.add(a.getLong(i));

        return new AnsDecoder(m1, a1, len);
    }

    public static AnsEncoder buildEncoder(AnsModel m, int[] l, int len) {

        AnsEncoder ans = new AnsEncoder(m);
        ans.encodeAll(reversed(l), len);
        return ans;
    }

    public static double getBits (AnsEncoder e) {
        return 64 * e.normCount / (double) len;
    }

    @Test
    public void equiprobableModel() {
        System.out.println("Equiprobable model test");
        double avgBits = 0;
        for (int i = 0; i < runPerModel; i++) {
            AnsModelEquiprobable m = new AnsModelEquiprobable(maxNum);

            AnsEncoder ans = buildEncoder(m, numList, len);
            AnsDecoder dec1 = buildDecoder(ans.stateList, ans.normCount, m);
            avgBits += getBits(ans);
            decodeCheck(dec1, numList, len);
        }
        avgBits /= runPerModel;
        System.out.println("Average bits per element: " + avgBits);
    }

    @Test
    public void optimalModel() {
        System.out.println("Optimal model test");
        double avgBits = 0;
        for (int i = 0; i < runPerModel; i++) {
            SymbolStats s = new SymbolStats(numList, len, 10);
            AnsModel m = new AnsModel(s);
            AnsEncoder ans = buildEncoder(m, numList, len);
            AnsDecoder dec = buildDecoder(ans.stateList, ans.normCount, m);
            decodeCheck(dec, numList, len);
            avgBits += getBits(ans);
        }
        avgBits /= runPerModel;
        System.out.println("Average bits per element: " + avgBits);
    }

    @Test
    public void orderStatisticModelDebug() {
        System.out.println("Order statistics model test");

        int list[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
//        int list[] = {3, 3, 3, 3, 5, 5, 5};
        int len = list.length;
        int median, thirdquartile, max;
        median = list[len/2];
        thirdquartile = list[(int) (len * 0.75)];
        max = list[len-1];
        System.out.println("List stats: \nmed: " + median + " tq: " + thirdquartile + " max: " + max);
//        AnsModelOrderStatistic m = new AnsModelOrderStatistic(median, thirdquartile, max, 1024);
        AnsModelOrderStatistic m = new AnsModelOrderStatistic(1, 2, 5, 1024);

        for (int i = 0; i <= 5; i++) {
            System.out.println("\nSym: " + i);
            System.out.println("fs: " + m.getFrequency(i) + " cs: " + m.getCumulative(i));
        }


    }

    @Test
    public void orderStatisticModel() {
        System.out.println("Order statistics model test");
        int median, thirdquartile, max;
        double avgBits = 0;
        for (int i = 0; i < runPerModel; i++) {
            median = numList[len/2];
            thirdquartile = numList[(int) (len * 0.75)];
            max = numList[len-1];

            AnsModelOrderStatistic m = new AnsModelOrderStatistic(median, thirdquartile, max, 1024);

            AnsEncoder ans = buildEncoder(m, numList, len);
            AnsDecoder dec = buildDecoder(ans.stateList, ans.normCount, m);
            avgBits += getBits(ans);
            decodeCheck(dec, numList, len);
        }
        avgBits /= runPerModel;
        System.out.println("Average bits per element: " + avgBits);
    }


}
