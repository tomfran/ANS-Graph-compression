package it.tomfran.test;

import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.SymbolStats;

import java.util.ArrayList;
import java.util.List;

public class AnsTest {

    public static void main(String[] args) {
        ArrayList<Integer> l = new ArrayList<Integer>(List.of(1, 3, 1, 2, 2, 1, 1, 1,
                                                                2, 3, 1, 1, 1, 1, 1, 1,
                                                                1, 1, 1, 1, 1, 1, 1, 1,
                                                                4, 5, 6, 7, 8, 8, 7, 1, 10,
                                                                1, 1, 1, 1, 1, 1, 1, 4));

        SymbolStats s = new SymbolStats(l);
        AnsEncoder ans = new AnsEncoder(s);
        System.out.println("ANS initialization: ");
        ans.debugPrint();

        System.out.print("\nEncoding...");
        ans.encodeAll(l);
        System.out.println("DONE");
        System.out.println("After " + l.size() + " encodings");
        ans.printState();

        System.out.print("\nDecoding...");
        List<Integer> r = ans.decodeAll();

        int n = r.size();
        int m = l.size();
        assert n == m;
        for (int i = 0; i <= n; i++)
            assert r.get(n-1-i) == l.get(i);

        System.out.println("DONE");
        ans.printState();
    }

}
