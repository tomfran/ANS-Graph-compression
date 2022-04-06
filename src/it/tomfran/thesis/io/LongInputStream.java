package it.tomfran.thesis.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LongInputStream {

    InputStream is;
    long buffer;
    int read;
    int rem;

    public LongInputStream(InputStream ins) {
        is = ins;
        read = 0;
        rem = 64;
        try {
            readBuffer();
        } catch (IOException e) {
            System.out.println("Could not initialize buffer");
            e.printStackTrace();
        }
    }

    public int readInt(int len) throws IOException {
        // if its in the current buffer:
        // shift down to remove all lower bits, & to get only len bits;
        if (rem > len)
            return (int) getNBits(len);
        else {
            // get the lower bits and get the other from the next buffer
            int toAdd = len - rem;
            int tmp = (int) getNBits(rem);
            readBuffer();
            return (int) ((tmp << toAdd) | getNBits(toAdd));
        }
    }

    private long getNBits(int n){
        rem -= n;
        return (buffer >>> rem) & ((1 << n) - 1);
    }

    private void readBuffer() throws IOException {
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
