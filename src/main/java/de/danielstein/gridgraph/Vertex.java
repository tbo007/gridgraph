package de.danielstein.gridgraph;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

    private final int number;

    private final  Object domainObj;


    final List<Edge> targetConnections = new ArrayList<>();

    final List<Edge> sourceConnections = new ArrayList<>();


    public Vertex(Object domainObj ,int number) {
        this.number = number;
        this.domainObj = domainObj;
    }

    public boolean targets (Vertex v) {
        return sourceConnections.stream().map(e -> (e.target)).anyMatch(e-> e.equals(v));
    }

    public boolean incomes(Vertex v) {
        return targetConnections.stream().map(e -> (e.source)).anyMatch(e-> e.equals(v));
    }

    @Override
    public String toString() {
        return  "Vertex: " + number + " / " +   (domainObj == null ? "FakeVertex" :  "DomainObj: " + domainObj.toString());
    }
}
