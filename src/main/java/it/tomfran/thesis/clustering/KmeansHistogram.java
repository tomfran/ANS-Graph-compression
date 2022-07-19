package it.tomfran.thesis.clustering;

import static it.tomfran.thesis.clustering.DatapointHistogram.buildCentroidFromCluster;

public class KmeansHistogram {

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
            if (PROGRESS && clusterCardinality[i] == 0)
                System.out.println("Cluster "+ i + " is empty");
            if (clusterCardinality[i] > 0) {
                if (PROGRESS)
                    System.out.println("$$$ Cluster: " + i);
                centroid[i] = buildCentroidFromCluster(getClusterPoints(i));
            }
        }
    }

    private void buildCentroidAnsStructures() {
        int total = 0;
        for (int i = 0; i < K; i++) {
            if (clusterCardinality[i] == 0) continue;
            centroid[i].buildAnsStructures();
            total += centroid[i].symbolsMapping.size();
        }
        if (PROGRESS)
            System.out.println("Average number of symbols after excaping: " + (double)total/K);
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
        int total = 0;
        for (int k = 0; k < K; k++) {
            total += centroid[k].rawFrequencyMap.size();
        }
        if (PROGRESS)
            System.out.println("Average number of symbols per cluster: " + (double)total/K);


        boolean stop = false;

        double minDistance, currDistance;
        // for each iteration
        int i;
        for (i = 0; i < iterations && !stop; i++) {
            if (PROGRESS)
                System.out.println("\n\nIteration: " + i);
            stop = true;
            // for each point, compute the closest centroid
            for (int j = 0; j < n; j++) {
                if (PROGRESS && ((j % (n/10)) == 0))
                    System.out.println("Point: " + j + "/" + n);
                // current distance from the centroid
                minDistance = data[j].distance(centroid[pointMapping[j]]);
                // find the closest centroid and update distance
                for (int k = 0; k < K; k++) {
                    if ((k != pointMapping[j]) &&
                            (currDistance = data[j].distance(centroid[k])) < minDistance) {
                        stop = false;
                        minDistance = currDistance;
                        // update cluster cardinality and mappings
                        clusterCardinality[pointMapping[j]]--;
                        pointMapping[j] = k;
                        clusterCardinality[k]++;
                    }
                }
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
        int ind;
        for (int i = 0; i < n; i++) {
            ind = i%K;
            pointMapping[i] = ind;
            clusterCardinality[ind] ++;
        }

        updateCentroids();

        if (sorting) {
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
