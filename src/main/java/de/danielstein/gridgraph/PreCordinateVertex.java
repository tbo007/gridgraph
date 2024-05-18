package de.danielstein.gridgraph;

public class PreCordinateVertex<T> extends Vertex{

    final int layerhint;
    final int rowhint;
    PreCordinateVertex(int number, int layer, int row) {
        super(number);
        this.layerhint = layer;
        this.rowhint = row;
    }
}
