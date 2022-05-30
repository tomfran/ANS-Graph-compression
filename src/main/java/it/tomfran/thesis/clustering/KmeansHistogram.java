package it.tomfran.thesis.clustering;

import javax.xml.crypto.Data;

public class KmeansHistogram {

    protected int K;
    protected int iterations;
    protected DatapointHistogram centroids[];

    public KmeansHistogram(int k, int iterations) {
        K = k;
        this.iterations = iterations;
        centroids = new DatapointHistogram[k];
        initializeCentroids();
    }

    private void initializeCentroids(){

    }

    private void updateCentroids() {

    }

    public void fit(DatapointHistogram[] p) {

        int n = p.length;
        int centroidMappings[] = new int[n];
        boolean stop = false;

        double minDistance, currDistance;

        // for each iteration
        for (int i = 0; i < iterations && !stop; i++) {
            stop = true;
            // for each point, compute the closest centroid
            for (int j = 0; j < n; j++) {
                // current distance from the centroid
                minDistance = p[j].distance(centroids[centroidMappings[j]]);
                // find the closest centroid and update distance
                for (int k = 0; k < K; k++) {
                    currDistance = p[j].distance(centroids[k]);
                    if (currDistance < minDistance){
                        stop = false;
                        minDistance = currDistance;
                        centroidMappings[j] = k;
                    }
                }
            }
            updateCentroids();
        }
    }

    public DatapointHistogram predict(DatapointHistogram p) {
        int r = 0;
        double minDistance = p.distance(centroids[r]), currDistance;
        for (int k = 1; k < K; k++) {
            currDistance = p.distance(centroids[k]);
            if (currDistance < minDistance){
                minDistance = currDistance;
                r = k;
            }
        }
        return centroids[r];
    }
}
