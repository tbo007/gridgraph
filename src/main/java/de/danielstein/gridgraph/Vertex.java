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

    @Override
    public String toString() {
        return  "Vertex: " + number + " / " +   (domainObj == null ? "FakeVertex" :  "DomainObj: " + domainObj.toString());
    }
}
