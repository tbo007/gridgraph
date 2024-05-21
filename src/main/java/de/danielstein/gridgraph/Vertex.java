package de.danielstein.gridgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Vertex {

    private final int number;

    final  Object domainObj;


    final List<Edge> targetConnections = new ArrayList<>();

    final List<Edge> sourceConnections = new ArrayList<>();


    public Vertex(Object domainObj ,int number) {
        this.number = number;
        this.domainObj = domainObj;
    }
   
    private Collection<Vertex> incomingFrom() {
        return targetConnections.stream().map(e-> e.source).collect(Collectors.toSet());
    }

    private Collection<Vertex> outgoingTo() {
        return sourceConnections.stream().map(e-> e.target).collect(Collectors.toSet());
    }

    public boolean isFake() {
        return domainObj == null;
    }



    @Override
    public String toString() {
        return  "Vertex: " + number + " / " +   (isFake() ? "FakeVertex" :  "DomainObj: " + domainObj.toString());
    }
}
