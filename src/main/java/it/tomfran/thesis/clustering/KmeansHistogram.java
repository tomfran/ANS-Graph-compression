package it.tomfran.thesis.clustering;

import static it.tomfran.thesis.clustering.DatapointHistogram.buildCentroidFromCluster;

public class KmeansHistogram {

    protected static final int CLUSTER_BUILD_PRECISION = 2048;
    private static final boolean PROGRESS = true;
    public DatapointHistogram[] centroid;
    public int K;
    protected int iterations;
    protected int n;
    protected DatapointHistogram[] data;
    protected int[] pointMapping;
    protected int[] clusterCardinality;

    public KmeansHistogram(int k, int iterations, DatapointHistogram[] data) {
        K = k;
        n = data.length;
        this.iterations = iterations;
        this.data = data;
        pointMapping = new int[n];
        clusterCardinality = new int[k];
        centroid = new DatapointHistogram[k];
        initializeCentroids();
    }

    private void initializeCentroids() {
        for (int i = 0; i < K; i++)
            centroid[i] = data[i].copy();
    }

    private void updateCentroids() {
        for (int i = 0; i < K; i++) {
            if (PROGRESS) {
                System.out.println("Cluster num: " + i + " cluster points: " + clusterCardinality[i]);
            }
            centroid[i] = buildCentroidFromCluster(getClusterPoints(i),
                    CLUSTER_BUILD_PRECISION);
        }
    }

    private void buildCentroidAnsStructures() {
        for (int i = 0; i < K; i++)
            centroid[i].buildAnsStructures();
    }

    private DatapointHistogram[] getClusterPoints(int i) {
        DatapointHistogram[] ret = new DatapointHistogram[clusterCardinality[i]];

        int pos = 0;
        for (int j = 0; j < n; j++)
            if (pointMapping[j] == i) {
                ret[pos++] = data[j];
            }
        return ret;
    }

    public void fit() {

        if (PROGRESS)
            System.out.println("Kmeans Clustering:");

        lazyFit(false);

        boolean stop = false;

        double minDistance, currDistance;
        // for each iteration
        int i;
        for (i = 0; i < iterations && !stop; i++) {

            if (PROGRESS)
                System.out.println("- iteration " + (i + 1));

            stop = true;
            // for each point, compute the closest centroid
            for (int j = 0; j < n; j++) {
                if (PROGRESS)
                    if (j % 10000 == 0 && j > 0)
                        System.out.println("datapoint number: " + j);
                // current distance from the centroid
                minDistance = data[j].distance(centroid[pointMapping[j]]);
                // find the closest centroid and update distance
                for (int k = 0; k < K; k++) {
                    currDistance = data[j].distance(centroid[k]);
                    if (currDistance < minDistance) {
                        stop = false;
                        minDistance = currDistance;
                        // update cluster cardinality and mappings
                        clusterCardinality[pointMapping[j]]--;
                        pointMapping[j] = k;
                        clusterCardinality[k]++;
                    }
                }
            }
            if (PROGRESS) {
                System.out.println("\tCentroid assignments: ");
                for (int j = 0; j < K; j++)
                    System.out.println("\t- centroid " + j + ": " + clusterCardinality[j]);
            }
            // rebuild centroids according to new clusters
            updateCentroids();
        }
        buildCentroidAnsStructures();
        if (PROGRESS) {
            if (i < iterations)
                System.out.println("Early termination");
            System.out.println("Clustering completed");
        }
    }


    public void lazyFit(boolean sorting) {
        if (PROGRESS)
            System.out.println("Lazy clustering");
        int interval = n/(K-1);

        int index;
        for (int i = 0; i < n; i++) {
            index = i / interval;

            pointMapping[i] = index;
            clusterCardinality[index] ++;
        }

        if (PROGRESS)
            System.out.println("Lazy clustering completed\nUpdating centroids");

        updateCentroids();

        if (sorting) {
            if (PROGRESS)
                System.out.println("Sorting frequencies");
            buildCentroidAnsStructures();
        }
    }

    public DatapointHistogram getCluster (int index) {
        return centroid[pointMapping[index]];
    }

    public int getClusterIndex (int index) {
        return pointMapping[index];
    }


    public DatapointHistogram predict(DatapointHistogram p) {
        int r = 0;
        double minDistance = p.distance(centroid[r]), currDistance;
        for (int k = 1; k < K; k++) {
            currDistance = p.distance(centroid[k]);
            if (currDistance < minDistance) {
                minDistance = currDistance;
                r = k;
            }
        }
        return centroid[r];
    }
}
