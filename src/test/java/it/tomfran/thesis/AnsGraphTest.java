package it.tomfran.thesis;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AnsGraphTest {


    @Test
    public void successorsArrayTest() {
        ImmutableGraph g;

        try {
            g = BVGraph.load("data/uk-2007-05@100000");


            AnsGraph.store(g, "data/ans-uk_2");
            AnsGraph ag = AnsGraph.load("data/ans-uk");

            assert (g.numNodes() == ag.numNodes());

            for (int i = 0; i < g.numNodes(); i++)
                assert (g.outdegree(i) == ag.outdegree(i));

            int[] succ1, succ2;
            for (int i = 0; i < g.numNodes(); i++) {

                succ1 = g.successorArray(i);
                succ2 = ag.successorsArray(i);

                for (int j = 0; j < ag.outdegree(i); j++)
                    assert (succ1[j] == succ2[j]);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void successorsIteratorTest(){

        try {
            ImmutableGraph g = BVGraph.load("data/uk-2007-05@100000");

//            AnsGraph.store(g, "data/ans-uk");
            AnsGraph ag = AnsGraph.load("data/ans-uk");

            LazyIntIterator i1 = ag.successors(10);
            for (int i = 0; i < ag.outdegree(10); i++) {
                System.out.print(i1.nextInt() + " ");
            }
            System.out.println();

//
//            assert (g.numNodes() == ag.numNodes());
//
//            for (int i = 0; i < g.numNodes(); i++)
//                assert (g.outdegree(i) == ag.outdegree(i));
//
//            int[] succ1, succ2;
//            for (int i = 0; i < g.numNodes(); i++) {
//
//                LazyIntIterator i1 = g.successors(i);
//                LazyIntIterator i2 = ag.successors(i);
//
//                for (int j = 0; j < ag.outdegree(i); j++) {
//                    assert i1.nextInt() == i2.nextInt();
//                }
//                assert i2.nextInt() == -1;
//            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
