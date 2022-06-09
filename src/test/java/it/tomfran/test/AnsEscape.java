package it.tomfran.test;

import it.tomfran.thesis.ans.AnsEncoder;
import it.tomfran.thesis.ans.AnsModel;
import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static it.unimi.dsi.webgraph.EFGraph.loadLongBigList;

public class AnsEscape {

    @Test
    public void symbolStatsEscape() {
        int[] numList = AnsTest.getRandom(100);
        for (int i = 0; i <= 5; i++) {
            System.out.println("\nCutting: " + 5*i + " percentage");
            new SymbolStats(numList, numList.length, 10, 5*i, 5*i).debugPrint();
        }
    }

    @Test
    public void encoderTest() {
        int[] numList = AnsTest.getRandom(1000);
//        for (int e : numList) System.out.print(e + ", "); System.out.println();
        for (int i = 1; i < 19; i++) {
            SymbolStats s = new SymbolStats(numList, numList.length, 10, 2, 5*i);
            System.out.println("ENCODING: escape frequency: " + 5*i + " percent");
            AnsEncoder e = new AnsEncoder(new AnsModel(s));
            e.encodeAll(numList, numList.length);
            e.debugPrint();

        }
    }

    @Test
    public void dumpRebuild() throws IOException {

        CharSequence filename = "data/test/test.stream";
        final FileOutputStream modelOs = new FileOutputStream((String) filename);
        final FileChannel modelChannel = modelOs.getChannel();
        final LongWordOutputBitStream modelStream = new LongWordOutputBitStream(modelChannel, ByteOrder.nativeOrder());

        int[] numList = AnsTest.getRandom(1000);
        SymbolStats s = new SymbolStats(numList, numList.length, 10, 0, 10);
//        s.debugPrint();
        AnsModel m = new AnsModel(s);
        m.debugPrint();
        m.dump(modelStream);

        modelStream.close();
        modelChannel.close();
        modelOs.close();

        System.out.println("############\nREBUILDING");
        LongBigArrayBigList modelsLongList = loadLongBigList(filename, ByteOrder.nativeOrder());
        int l = 0; // TODO: check this
        LongWordBitReader modelInStream = new LongWordBitReader(modelsLongList, l);
        AnsModel.rebuildModel(modelInStream).debugPrint();
    }



}
