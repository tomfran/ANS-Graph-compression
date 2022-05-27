package it.tomfran.benchmark;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.algo.StronglyConnectedComponents;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 1000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class AnsGraphBenchmark {

    CharSequence basename = "data/random/";
    CharSequence METHOD = "optimal";
    AnsGraph ag;
    EFGraph ef;
    int node = 10;

    @Setup
    public void setup() {
        try {
            ag = AnsGraph.load(basename + "ANS");
            ef = EFGraph.load(basename + "EF");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public int ansSuccessor(){
        LazyIntIterator i = ag.successors(node);
        int e, prev = 0;
        while((e = i.nextInt()) != -1) {
            prev = e;
        }
        return prev;
    }


    @Benchmark
    public int efSuccessor(){
        LazyIntIterator i = ef.successors(node);
        int e, prev = 0;
        while((e = i.nextInt()) != -1){
            prev = e;
        }
        return prev;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AnsGraphBenchmark.class.getSimpleName()).build();

        new Runner(opt).run();
    }


}
