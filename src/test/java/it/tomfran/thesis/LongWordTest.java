package it.tomfran.thesis;

import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.io.InputBitStream;
import it.unimi.dsi.io.OutputBitStream;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static it.unimi.dsi.webgraph.EFGraph.loadLongBigList;
import static java.lang.Math.max;

public class LongWordTest {

    @Test
    public void readWriteBitStream() throws IOException {

        String filename = "data/test/bitstream.stream";
        final OutputBitStream offsets = new OutputBitStream(filename);

        for (int i = 0; i < 100; i++) {
            offsets.writeLongDelta(i);
        }
        offsets.close();

        final InputBitStream ioffsets = new InputBitStream(filename);

        for (int i = 0; i < 100; i++) {
            assert (i == ioffsets.readLongDelta());
        }
    }

    @Test
    public void readWriteLongWordTest() throws IOException {

        CharSequence filename = "data/test/test.stream";
        final FileOutputStream graphOs = new FileOutputStream((String) filename);
        final FileChannel graphChannel = graphOs.getChannel();
        final LongWordOutputBitStream graphStream = new LongWordOutputBitStream(graphChannel, ByteOrder.nativeOrder());

        for (int i = 0; i < 100; i++) {
            graphStream.writeGamma(i);
        }

        for (int i = 1000; i <1010; i++) {
            graphStream.append(i, Long.SIZE);
        }

        for (int i = 0; i < 100; i++) {
            graphStream.writeGamma(i);
        }

        graphStream.close();
        graphChannel.close();
        graphOs.close();

        LongBigArrayBigList modelsLongList = loadLongBigList(filename, ByteOrder.nativeOrder());
        int l = 32; // TODO: check this
        LongWordBitReader modelLongWordReader = new LongWordBitReader(modelsLongList, l);

        for (int i = 0; i < 100; i++) {
            long e = modelLongWordReader.readGamma();
            assert (i == e);
        }

        for (int i = 1000; i <1010; i++) {
            long e = modelLongWordReader.readLong();
            assert (i == e);
            System.out.println(e);
        }

        for (int i = 0; i < 100; i++) {
            long e = modelLongWordReader.readGamma();
            assert (i == e);
        }

    }

    @Test
    public void offsetsTest() throws IOException {

        CharSequence filename = "data/test/test.stream";
        final FileOutputStream graphOs = new FileOutputStream((String) filename);
        final FileChannel graphChannel = graphOs.getChannel();
        final LongWordOutputBitStream graphStream = new LongWordOutputBitStream(graphChannel, ByteOrder.nativeOrder());

        int[] off = new int[1000];
        off[0] = 0;
        for (int i = 0; i < 1000; i++) {
            off[i] = graphStream.writeGamma(i) + off[max(0, i - 1)];
        }

        graphStream.close();
        graphChannel.close();
        graphOs.close();


        LongBigArrayBigList modelsLongList = loadLongBigList(filename, ByteOrder.nativeOrder());
        int l = 32; // TODO: check this
        LongWordBitReader modelLongWordReader = new LongWordBitReader(modelsLongList, l);

        System.out.println("Position to " + off[560] + " and read of 20 ints");
        modelLongWordReader.position(off[560]);


        for (int i = 0; i < 20; i++) {
            long e = modelLongWordReader.readGamma();
            System.out.println(e);
        }
    }

}
