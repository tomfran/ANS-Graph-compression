package it.tomfran.thesis.clustering;

import java.util.List;

public interface Kmeans {

    abstract void fit(Datapoint[] p);

    abstract Centroid predict(Datapoint p);

}
