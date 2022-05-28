package it.tomfran.thesis.clustering;

public interface Datapoint {

    abstract double distance(Datapoint p);


    abstract double distance(Centroid c);

}
