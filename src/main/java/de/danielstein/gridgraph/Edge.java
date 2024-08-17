package de.danielstein.gridgraph;

import java.util.Objects;

public class Edge {

    public Vertex getSource() {
        return source;
    }

    public final Vertex source;

    public Vertex getTarget() {
        return target;
    }

    public final Vertex target;


     Edge(Vertex source, Vertex target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public String toString() {
        return "Edge{" +"source=" + source +", target=" + target +'}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }
}
