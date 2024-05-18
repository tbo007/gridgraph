package de.danielstein.gridgraph;

import java.util.ArrayList;
import java.util.List;

public class Vertex {

    private final int number;


    final List<Edge> targetConnections = new ArrayList<>();

    final List<Edge> sourceConnections = new ArrayList<>();


    public Vertex(int number) {
        this.number = number;
    }



}
