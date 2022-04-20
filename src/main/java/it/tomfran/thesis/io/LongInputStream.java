package it.tomfran.thesis.io;

import java.io.IOException;
import java.io.InputStream;

public class LongInputStream {

    InputStream is;
    long buffer;
    int read;
    int rem;

    public LongInputStream(InputStream ins) throws IOException {
        is = ins;
        read = 0;
        rem = 64;
        fillBuffer();
    }

    public int readInt(int len) throws IOException {
        if (rem > len)
            return (int) getNBits(len);
        else {
            // get the upper bits and get the other from the next buffer
            int toAdd = len - rem;
            // get n bits changes rem
            int tmp = (int) getNBits(rem);
            //
            fillBuffer();
            return (int) ((tmp << toAdd) | getNBits(toAdd));
        }
    }

    private long getNBits(int n){
        rem -= n;
        return (buffer >>> rem) & ((1 << n) - 1);
    }

    private void fillBuffer() throws IOException {
        int b;
        buffer = 0L;
        for (long i = 56; i >= 0; i -= 8) {
            long v = is.read();
            v = v &  ((1 << 8) - 1);
            buffer |= v << i;
        }
        read = 0;
        rem = 64;
    }

    public void debugPrint(){
        System.out.println("---- Buffer -----");
        System.out.println(buffer);
    }

}
