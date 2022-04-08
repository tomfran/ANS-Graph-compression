package it.tomfran.test;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.EFGraph;

import java.io.IOException;

public class WebGraphTest {

    public static void main(String[] args) {
        EFGraph g;

        {
            try {
                g = EFGraph.load("data/cnr-2000");
                System.out.println(g.numNodes());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
