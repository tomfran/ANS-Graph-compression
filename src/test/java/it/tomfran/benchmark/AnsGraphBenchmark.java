package it.tomfran.benchmark;

import it.tomfran.thesis.graph.AnsGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
@BenchmarkMode(org.openjdk.jmh.annotations.Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class AnsGraphBenchmark {

    CharSequence basename = "data/ans-uk";
    AnsGraph ag;

    @Setup
    public void setup(){
        try {
            ag = AnsGraph.load(basename);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public int outdegree() {
        return ag.outdegree(10);
    }

//    @Benchmark
    public int successors() {
        int node = 10;
        int a = 0;
        LazyIntIterator it = ag.successors(node);
        for (int i = 0; i < ag.outdegree(node); i++) {
            a = it.nextInt();
        }
        return a;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AnsGraphBenchmark.class.getSimpleName()).build();

        new Runner(opt).run();
    }


}
