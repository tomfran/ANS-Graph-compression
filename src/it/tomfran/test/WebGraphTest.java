package it.tomfran.test;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

import java.io.IOException;

public class WebGraphTest {

    public static void main(String[] args) {
        ImmutableGraph g;

        try {
            g = BVGraph.load("data/uk-2007-05@100000");


            AnsGraph.store(g);
//
//
//            EFGraph.store(g, "data/EF_UK");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
