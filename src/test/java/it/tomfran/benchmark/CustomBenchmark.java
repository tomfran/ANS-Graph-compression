package it.tomfran.benchmark;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

import java.io.IOException;

public class CustomBenchmark {

    public static void main(String[] args) throws IOException {

        int WARMUP = 1000;
        int REPEAT = 5000;
        int samples = 5000;

        CharSequence graph_name = "dblp-2011";
        CharSequence basename = "data/" + graph_name + "/nat/";
//        ImmutableGraph graph = AnsGraph.load(basename + "clustered_ans/ans");
//        ImmutableGraph graph = EFGraph.load(basename + "ef/" + graph_name);
        ImmutableGraph graph = BVGraph.load(basename + "bv/" + graph_name);

        XoRoShiRo128PlusRandom r = new XoRoShiRo128PlusRandom(0);
        final int n = graph.numNodes();
        int seed = 0;
        long z = -1;
        long cumulativeTime = 0;

        int totLinks = 0;
        r.setSeed(seed);
        for(long i = samples; i-- != 0;) totLinks += graph.outdegree(r.nextInt(n));

        for(int k = WARMUP + REPEAT; k-- != 0;) {
            r.setSeed(seed);
            long time = -System.nanoTime();
            for(long i = samples; i-- != 0;)
                for(final LazyIntIterator links = graph.successors(r.nextInt(n)); links.nextInt() != - 1;) z++;

            time += System.nanoTime();
            if (k < REPEAT) cumulativeTime += time;
        }
        final double averageTime = cumulativeTime / (double)REPEAT;
        System.out.printf("Time: %.3fs nodes: %d; arcs %d; nodes/s: %.3f arcs/s: %.3f ns/node: %3f, ns/link: %.3f\n",
                averageTime / 1E9, samples, totLinks, (samples * 1E9) / averageTime, (totLinks * 1E9) / averageTime, averageTime / samples, averageTime / totLinks);
    }

}
