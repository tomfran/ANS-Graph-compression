package it.tomfran.test;

import it.tomfran.thesis.ans.AnsDecoder;
import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.io.LongInputStream;
import it.tomfran.thesis.io.LongOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class AnsTest {

    public static void main(String[] args) {

        int[] choices = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10};

        ArrayList<Integer> l = new ArrayList<>();

        Random rand = new Random();
        int NUM_INT = 100000;
        while ((NUM_INT --) != 0)
            l.add(choices[Integer.max(0,(rand.nextInt()%choices.length))]);

        System.out.println(l.size());

        System.out.println("### ENCODER ###");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LongOutputStream los = new LongOutputStream(os);

        SymbolStats s = new SymbolStats(l, 10);
        AnsEncoder ans = new AnsEncoder(s, los);
        ans.debugPrint();
        Collections.reverse(l);
        ans.encodeAll(l);
        Collections.reverse(l);
        System.out.println("OK");


        System.out.println("\n\n### DECODER ###");
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        LongInputStream lis = new LongInputStream(is);

        AnsDecoder ansd = new AnsDecoder(lis);
        int i = 0;
        for (int e : ansd.decodeAll()) {
            if (e != l.get(i)) {
                System.out.println("\tWRONG: " + i + " GOT: " + e + " EXP: " + l.get(i));
            }
            i++;
        }
        System.out.println();

        System.out.println("OK");

    }

}
