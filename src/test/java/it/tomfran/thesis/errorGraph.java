package it.tomfran.thesis;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

import java.io.IOException;

public class errorGraph {

    static void genGraph(int n, double e) throws IOException {

        ErdosRenyiGraph g = new ErdosRenyiGraph(n, e);
        String filename = "data/error/ans";
        AnsGraph.store(g, filename);
        String f2 = "data/error/ef";
        EFGraph.store(g, f2);
    }

    static boolean check() throws IOException {
        String f1 = "data/error/ans";
        String f2 = "data/error/ef";

        AnsGraph ans = AnsGraph.load(f1);
        EFGraph ef = EFGraph.load(f2);

        if (ans.numNodes() != ef.numNodes()) {
            System.out.println("Num nodes mismatch");
            return false;
        }
        for (int i = 0; i < 10; i++) {

            LazyIntIterator i1 = ef.successors(i);
            LazyIntIterator i2 = ans.successors(i);

            if (ef.outdegree(i) != ans.outdegree(i)){
                System.out.println("Oudegree mismatch");
                return false;
            }
            for (int j = 0; j < ans.outdegree(i); j++) {
                if( i1.nextInt() != i2.nextInt()){
                    System.out.println("Successors mismatch on node: " +  i);
                    return false;
                }
            }

            if(i2.nextInt() != -1){
                System.out.println("Iterator not returning -1");
                return false;
            }
        }
        return true;
    }


    static void inspect() throws IOException {

        System.out.println("### REAL GRAPH: ");
        String f2 = "data/error/ef";
        EFGraph ef = EFGraph.load(f2);
        System.out.println(ef);

        System.out.println("\n\n### ANS GRAPH: ");
        String f1 = "data/error/ans";
        AnsGraph ag = AnsGraph.load(f1);
        System.out.println(ag);
    }

    static void storeTest() throws IOException {
        String f2 = "data/error/ef";
        EFGraph ef = EFGraph.load(f2);
        System.out.println(ef);
        System.out.println("### Storing with ANS");
        AnsGraph.store(ef, "data/error/ans");
        System.out.println("\n\n REBUILDING \n\n");
        AnsGraph ag = AnsGraph.load("data/error/ans");
        System.out.println(ag);
    }

    public static void main(String[] args) throws IOException {
        final boolean GEN = true;

        boolean check = true;
        if (GEN)
            for (int n = 10; n <= 20 && check; n+=1) {
                System.out.println("GEN:" + n);
                for (int i = 0; i < 1 && check; i++) {
//                    System.out.println("\n\n#######\n\n");
                    genGraph(n, 0.0001);
                    if (!check()) {
                        System.err.println("ERROR FOUND");
                        check = false;
                    }
                }

            }
    }



}
