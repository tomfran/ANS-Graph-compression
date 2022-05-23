package it.tomfran.thesis;

import it.tomfran.thesis.ans.AnsDecoder;
import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.AnsModel;
import it.tomfran.thesis.ans.SymbolStats;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.junit.jupiter.api.Test;

import java.util.Random;


public class AnsTest {

    public static int[] getRandom(int n) {
        int[] choices = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10};
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

    @Test
    public void encodeDecode() {

        // generate two random lists
        int[] l1 = getRandom(100);

        // compute stats for these two
        SymbolStats s1 = new SymbolStats(l1, 100, 3);
        AnsModel m1 = new AnsModel(s1);

        AnsEncoder ans1 = new AnsEncoder(m1);

        // encode first list FALSE TO AVOID FLUSH
        System.out.println("First list len " + l1.length);
        ans1.encodeAll(reversed(l1), 100);
//            ans1.flush(false);
        System.out.println("Encoded first list");
        System.out.println(ans1.stateList);
        LongArrayList a1 = new LongArrayList();
        for (int i = ans1.normCount - 1; i >= 0; i--) {
            a1.add(ans1.stateList.getLong(i));
        }
        System.out.println(a1);
        AnsDecoder dec1 = new AnsDecoder(m1, a1, ans1.normCount);

        int e, i;
        i = 0;
        do {
            e = dec1.decode();
            assert e == -1 || (e == l1[i]);
            i++;
        } while (e != -1);
        assert i == 101;
    }

}
