package it.tomfran.test;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import java.io.IOException;

public class AnsGraphTest {


    public static void main(String[] args) {
        ImmutableGraph g;

        try {
            g = BVGraph.load("data/uk-2007-05@100000");


            AnsGraph.store(g, "data/ans-uk");
            AnsGraph ag = AnsGraph.load("data/ans-uk");

            System.out.print("Checking number of nodes: ");
            System.out.println((g.numNodes() == ag.numNodes())? "PASS" : "WRONG");

            System.out.print("Checking all outdegrees: ");
            int wrong = 0;
            for (int i = 0; i < g.numNodes(); i++) {
                if (g.outdegree(i) != ag.outdegree(i))
                    wrong ++;
            }
            System.out.println((wrong == 0)? "PASS" : (wrong + " WRONG"));

            System.out.print("Checking successors: ");

            int[] succ1, succ2;
            wrong = 0;
            for (int i = 0; i < g.numNodes(); i++) {

                succ1 = g.successorArray(i);
                succ2 = ag.successorsArray(i);

                for (int j = 0; j < ag.outdegree(i); j++) {
                    if (succ1[j] != succ2[j]) {
                        wrong++;
                        break;
                    }
                }

            }

            System.out.println((wrong == 0)? "PASS" : (wrong + " WRONG"));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
