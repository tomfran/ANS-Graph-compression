package it.tomfran.test;

import it.tomfran.thesis.io.LongInputStream;
import it.tomfran.thesis.io.LongOutputStream;

import java.io.*;

public class LongStreamTest {
    public static void main(String[] args) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LongOutputStream los = new LongOutputStream(os);

        try {
            for (int i = 0; i < 10; i++) {
                los.writeInt(122 + (i %2), 7);
            }
            los.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        LongInputStream lis = new LongInputStream(is);

        int n;
        try {
            for (int i = 0; i < 10; i++) {
                n = lis.readInt(7);
                assert (n == (122 + (i %2)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
