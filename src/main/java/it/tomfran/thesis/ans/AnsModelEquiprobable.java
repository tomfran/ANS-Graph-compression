package it.tomfran.thesis.ans;

import it.tomfran.thesis.io.LongWordBitReader;
import it.tomfran.thesis.io.LongWordOutputBitStream;

import java.io.IOException;

public class AnsModelEquiprobable extends AnsModel {

    public AnsModelEquiprobable(int max){
        this.M = max;
    }

    @Override
    public int getCumulative(int symIndex) {
        return symIndex;
    }

    @Override
    public int getFrequency(int symIndex) {
        return 1;
    }

    @Override
    public int getRemainderSym(int r) {
        return r;
    }

    @Override
    public int getSymbolMapping(int sym) {
        return sym;
    }

    @Override
    public int getInvSymbolMapping(int sym) {
        return sym;
    }

    @Override
    public void debugPrint() {
        System.out.println("M: " + this.M);
    }

    @Override
    public long dump(LongWordOutputBitStream modelStream) throws IOException {
        return modelStream.writeGamma(M);
    }

    public static AnsModel rebuildModel(LongWordBitReader br) {
        return new AnsModelEquiprobable((int) br.readGamma());
    }

    @Override
    public AnsModel copy() {
        return new AnsModelEquiprobable(M);
    }
}


