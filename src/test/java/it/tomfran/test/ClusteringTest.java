package it.tomfran.test;

import it.tomfran.thesis.ans.SymbolStats;
import it.tomfran.thesis.clustering.DatapointHistogram;
import it.tomfran.thesis.clustering.KmeansHistogram;

import java.util.Random;

public class ClusteringTest {

    static int n = 30;
    static int sampleLen = 50;
    static DatapointHistogram[] data;
    static Random rand = new Random(0);
    static int[][] dist = {{1, 1, 1, 1, 2, 2, 3, 4, 9876},
            {1, 2, 3, 4, 2143},
            {1, 1, 2, 2, 2, 2, 2, 73281}};

    static DatapointHistogram getSample(int i) {
        int[] sample = new int[sampleLen];
        for (int j = 0; j < sampleLen; j++) {
            int pos = rand.nextInt(dist[i].length);
            sample[j] = dist[i][pos];
        }
        return new DatapointHistogram(new SymbolStats(sample, sampleLen, 10));
    }

    static void generateDatapoints() {
        data = new DatapointHistogram[n];
        for (int i = 0; i < n; i++) {
            data[i] = getSample(i / 10);
        }
    }

    public static void main(String[] args) {
        generateDatapoints();
        System.out.println("Starting with clustering");
        KmeansHistogram k = new KmeansHistogram(3, 10, data);
        k.fit();

        System.out.println("Final centroids: ");
        for (DatapointHistogram c : k.centroid) {
            System.out.println(c);
        }

    }

}
