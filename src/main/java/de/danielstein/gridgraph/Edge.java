package de.danielstein.gridgraph;

public class Edge {

    final Vertex source;

    final Vertex target;

    final int weight;


     Edge(Vertex source, Vertex target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }
}
