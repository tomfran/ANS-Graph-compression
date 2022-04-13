package it.tomfran.test;

import it.tomfran.thesis.io.LongInputStream;
import it.tomfran.thesis.io.LongOutputStream;

import java.io.*;

public class LongStreamTest {
    public static void main(String[] args) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LongOutputStream los = new LongOutputStream(os);
        try {
            for (int i = 0; i < 100; i++) {
                los.writeInt(73421893 + (i % 2), 31);
            }
            los.flushBuffer();

            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            LongInputStream lis = new LongInputStream(is);

            int n;
            for (int i = 0; i < 100; i++) {
                n = lis.readInt(31);
                if (n != (73421893 + (i % 2))) {
                    System.out.println("READ: " + n + " EXPECTED " + (73421893 + (i % 2)));
                }
            }
            System.out.println("all okay");
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
