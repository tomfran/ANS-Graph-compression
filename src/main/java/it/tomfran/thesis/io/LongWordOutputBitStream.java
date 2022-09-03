package it.tomfran.thesis.io;

import it.unimi.dsi.bits.Fast;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.longs.LongBigList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;

public class LongWordOutputBitStream {
    private static final int BUFFER_SIZE = 64 * 1024;
    /** The Java nio buffer used to write with prescribed endianness. */
    private final ByteBuffer byteBuffer;
    /** The output channel. */
    private final WritableByteChannel writableByteChannel;
    /** The 64-bit buffer, whose upper {@link #free} bits do not contain data. */
    private long buffer;
    /** The number of upper free bits in {@link #buffer} (strictly positive). */
    private int free;

    public LongWordOutputBitStream(final WritableByteChannel writableByteChannel, final ByteOrder byteOrder) {
        this.writableByteChannel = writableByteChannel;
        byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(byteOrder);
        free = Long.SIZE;
    }

    public int append(final long value, final int width) throws IOException {
        assert width == Long.SIZE || (-1L << width & value) == 0;
        buffer |= value << -free;

        if (width < free) free -= width;
        else {
            byteBuffer.putLong(buffer); // filled
            if (!byteBuffer.hasRemaining()) {
                byteBuffer.flip();
                writableByteChannel.write(byteBuffer);
                byteBuffer.clear();
            }

            if (width == free) {
                buffer = 0;
                free = Long.SIZE;
            } else {
                // free < Long.SIZE
                buffer = value >>> free;
                free = Long.SIZE - width + free; // width > free
            }
        }
        return width;
    }

    public long append(final long[] value, final long length) throws IOException {
        long l = length;
        for (int i = 0; l > 0; i++) {
            final int width = (int) Math.min(l, Long.SIZE);
            append(value[i], width);
            l -= width;
        }

        return length;
    }

    public long append(final LongBigList value, final long length) throws IOException {
        long l = length;
        for (long i = 0; l > 0; i++) {
            final int width = (int) Math.min(l, Long.SIZE);
            append(value.getLong(i), width);
            l -= width;
        }

        return length;
    }

    public long append(final LongArrayBitVector bv) throws IOException {
        return append(bv.bits(), bv.length());
    }

//    public long append(final LongWordCache cache) throws IOException {
//        long l = cache.length();
//        cache.rewind();
//        while (l > 0) {
//            final int width = (int)Math.min(l, Long.SIZE);
//            append(cache.readLong(), width);
//            l -= width;
//        }
//
//        return cache.length();
//    }

    public int align() throws IOException {
        if (free != Long.SIZE) {
            byteBuffer.putLong(buffer); // partially filled
            if (!byteBuffer.hasRemaining()) {
                byteBuffer.flip();
                writableByteChannel.write(byteBuffer);
                byteBuffer.clear();
            }

            final int result = free;
            buffer = 0;
            free = Long.SIZE;
            return result;
        }

        return 0;
    }

    public int writeNonZeroGamma(final long value) throws IOException {
        if (value <= 0) throw new IllegalArgumentException("The argument " + value + " is not strictly positive.");
        final int msb = Fast.mostSignificantBit(value);
        final long unary = 1L << msb;
        append(unary, msb + 1);
        append(value ^ unary, msb);
        return 2 * msb + 1;
    }

    public int writeGamma(final long value) throws IOException {
        if (value < 0) throw new IllegalArgumentException("The argument " + value + " is negative.");
        return writeNonZeroGamma(value + 1);
    }

    public void close() throws IOException {
        byteBuffer.putLong(buffer);
        byteBuffer.flip();
        writableByteChannel.write(byteBuffer);
        writableByteChannel.close();
    }
}