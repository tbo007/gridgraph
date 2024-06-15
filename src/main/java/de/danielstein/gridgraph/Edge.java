package de.danielstein.gridgraph;

public class Edge {

    final Vertex source;

    final Vertex target;


     Edge(Vertex source, Vertex target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public String toString() {
        return "Edge{" +"source=" + source +", target=" + target +'}';
    }
}
