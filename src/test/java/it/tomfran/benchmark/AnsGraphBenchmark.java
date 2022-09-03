package it.tomfran.benchmark;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 100, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class AnsGraphBenchmark {

    CharSequence graph_name = "enwiki-2013";
    CharSequence basename = "data/" + graph_name + "/nat/";
    int seed = 0;
    int n;
    ImmutableGraph g;
    XoRoShiRo128PlusRandom r;

    @Setup
    public void setup() {
        try {
            g = AnsGraph.load(basename + "clustered_ans/ans");
//            g = EFGraph.load(basename + "ef/" + graph_name);
//            g = BVGraph.load(basename + "bv/" + graph_name);
            n = g.numNodes();
            r = new XoRoShiRo128PlusRandom();
            r.setSeed(seed);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
//
//    @Benchmark
//    public void stronglyConnectedComponents(){
//        StronglyConnectedComponents sc = StronglyConnectedComponents.compute(g, false, null);
//        int total = 0;
//        for (int e : sc.component){
//            total += e;
//        }
//        System.out.println(total);
//    }

    @Benchmark
    public void successors(){
        long z = 0;
        for(final LazyIntIterator links = g.successors(r.nextInt(n)); links.nextInt() != - 1;) z++;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AnsGraphBenchmark.class.getSimpleName()).build();

        new Runner(opt).run();
    }


}
