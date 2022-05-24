package it.tomfran.thesis.ans;

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
}


