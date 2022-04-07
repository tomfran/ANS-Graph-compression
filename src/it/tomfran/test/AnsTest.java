package it.tomfran.test;

import it.tomfran.thesis.ans.AnsDecoder;
import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.io.LongInputStream;
import it.tomfran.thesis.io.LongOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnsTest {

    public static void main(String[] args) {
        ArrayList<Integer> l = new ArrayList<Integer>(List.of(1, 3, 1, 2, 2, 1, 1, 1,
                                                                2, 3, 1, 1, 1, 1, 1, 1,
                                                                1, 1, 1, 1, 1, 1, 1, 1,
                                                                4, 5, 6, 7, 8, 8, 7, 1, 10,1, 1, 1, 1, 1, 1, 1, 1,
                4, 5, 6, 7, 8, 8, 7, 1, 10,1, 1, 1, 1, 1, 1, 1, 1,
                4, 5, 6, 7, 8, 8, 7, 1, 10,1, 1, 1, 1, 1, 1, 1, 1,
                4, 5, 6, 7, 8, 8, 7, 1, 10,1, 1, 1, 1, 1, 1, 1, 1,
                4, 5, 6, 7, 8, 8, 7, 1, 10,
                                                                1, 1, 1, 1, 1, 1, 1, 4));
        System.out.println("### ENCODER ###");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LongOutputStream los = new LongOutputStream(os);

        SymbolStats s = new SymbolStats(l);
        AnsEncoder ans = new AnsEncoder(s, los);
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
