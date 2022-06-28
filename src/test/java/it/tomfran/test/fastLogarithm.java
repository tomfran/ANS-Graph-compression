package it.tomfran.test;

import org.junit.jupiter.api.Test;

public class fastLogarithm {


    public static double trivial(float x){
        return (Math.log(x) / Math.log(2));
    }

    static int q, qm1;
    static float[] data;

    /**
     * Compute lookup table for a given base table size.
     *
     * @param n The number of bits to keep from the mantissa. Table storage =
     *          2^(n+1) * 4 bytes, e.g. 64Kb for n=13. Must be in the range
     *          0<=n<=23
     */
    public static void populateLUT(int n) {

        final int size = 1 << (n + 1);

        q = 23 - n;
        qm1 = q - 1;
        data = new float[size];

        for (int i = 0; i < size; i++) {
            data[i] = (float) (Math.log(i << q) / Math.log(2)) - 150;
        }
    }

    /**
     * Calculate the logarithm using base 2. Requires the argument be finite and
     * positive.
     */
    public static float fastLog2(float x) {
        final int bits = Float.floatToRawIntBits(x);
        final int e = (bits >> 23) & 0xff;
        final int m = (bits & 0x7fffff);
        return (e == 0 ? data[m >>> qm1] : e + data[((m | 0x00800000) >>> q)]);
    }

    @Test
    public void testAcc(){

        populateLUT(12);

        for (int i = 1; i < 1023; i++) {
            System.out.println(trivial((float) i / 1024) - fastLog2((float) i / 1024));
        }
    }


}
