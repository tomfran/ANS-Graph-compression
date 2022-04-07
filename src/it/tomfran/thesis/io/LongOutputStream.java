package it.tomfran.thesis.io;

import java.io.IOException;
import java.io.OutputStream;

public class LongOutputStream {

    protected OutputStream os;
    protected long buffer;
    protected int free;

    public LongOutputStream(OutputStream ous) {
        os = ous;
        free = 64;
        buffer = 0L;
    }

    public void writeInt(int n, int len) throws IOException {

//        System.out.println("Received to write: " + n);
        // if free can hold len, extract len bits from n,
        // write them at the top of the buffer
        n = n & ((1 << (len)) - 1);
        long k = n;
//        System.out.println("Binary representation: " + Long.toBinaryString(k));

        if (len <= free) {
            free -= len;
            buffer |= k << (free);
        }
        else {
            // write the upper len-free bits and flush
            len -= free;
            buffer |= k >>> len;
            flushBuffer();
            // write the lower bits
            buffer |= k << (free - len);
            free -= len;
        }
    }

    public void flushBuffer() throws IOException {
        // write the buffer in block of 8 bits
        // debugPrint();
        free = 64;
        for (int i = 56; i >= 0; i -= 8) {
            os.write((byte) (buffer >>> i));
        }
        buffer = 0L;
    }

    public void debugPrint() {
        System.out.println("---- Buffer -----");
        System.out.println(Long.toBinaryString(buffer));
    }

}
