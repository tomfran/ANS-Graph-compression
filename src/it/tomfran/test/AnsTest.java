package it.tomfran.test;

import it.tomfran.thesis.ans.AnsDecoder;
import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.io.LongInputStream;
import it.tomfran.thesis.io.LongOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;


public class AnsTest {

    public static int[] getRandom(int n){
        int[] choices = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] l = new int[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++)
            l[i] = choices[Integer.max(0,(rand.nextInt()%choices.length))];
        return l;
    }

    public static int[] reversed(int[] arr){
        int n = arr.length;
        int[] ret = new int[n];

        for (int i = 0; i < n; i++)
            ret[i] = arr[n-1-i];

        return ret;
    }

    public static void main(String[] args) {

        // generate two random lists
        int[] l1 = getRandom(100);
        int[] l2 = getRandom(200);

        // compute stats for these two
        SymbolStats s1 = new SymbolStats(l1, 100, 10);
        SymbolStats s2 = new SymbolStats(l2, 200, 10);

        // create an output stream for the two decoders
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LongOutputStream los = new LongOutputStream(os);
        AnsEncoder ans1 = new AnsEncoder(s1, los);

        ans1.debugPrint();

        try {
            // encode first list FALSE TO AVOID FLUSH
            System.out.println("First list len " + l1.length);
            ans1.encodeAll(reversed(l1), 100);
            ans1.flush(false);
            System.out.println("Encoded first list");

            // encode second list
            AnsEncoder ans2 = new AnsEncoder(s2, los);
            ans2.encodeAll(reversed(l2), 200);
            ans2.flush(true);
            System.out.println("Encoded second list");


        // build input stream
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        LongInputStream lis = new LongInputStream(is);

        // decode first
        AnsDecoder ansd1 = new AnsDecoder(lis);
        int i = 0;
        for (int e : ansd1.decodeAll()){
            if (e != l1[i])
                System.err.println("List 1 Wrong decode, got " + e + " expected " + l1[i]);
            i++;
        }
        System.out.println("Decoded first list of " + i + " elements");

        AnsDecoder ansd2 = new AnsDecoder(lis);
        i = 0;
        for (int e : ansd2.decodeAll()){
            if (e != l2[i])
                System.err.println("List 2 Wrong decode, got " + e + " expected " + l2[i]);
            i++;
        }
        System.out.println("Decoded second list of " + i + " elements");

        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
