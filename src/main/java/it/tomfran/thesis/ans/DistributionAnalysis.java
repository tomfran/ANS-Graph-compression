package it.tomfran.thesis.ans;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.FileWriter;
import java.io.IOException;

import static it.tomfran.thesis.graph.AnsGraph.computeGaps;

public class DistributionAnalysis {

    public static void main(String[] args) throws IOException {

        String filename = "data/wiki/enwiki-2013";
        String filepath = "data/gaps/enwiki-2013.gaps";


        BVGraph graph = BVGraph.load(filename);
        System.out.println("Graph loaded");

        FileWriter myFile = new FileWriter(filepath);

        int i = 0;
        for (final NodeIterator nodeIterator = graph.nodeIterator(); nodeIterator.hasNext(); ) {
            nodeIterator.nextInt();
            int out = nodeIterator.outdegree();
            if (out > 0) {
                int[] succ = computeGaps(nodeIterator.successorArray(), out);
                for (int e : succ)
                    myFile.write(e + ",");

                myFile.write("\n");
            }
            i++;
            if ((i % 100000) == 0)
                System.out.println("NODE NUM: " + i);
        }
        System.out.println("Finished with gaps");

        myFile.flush();
        myFile.close();
    }

}
