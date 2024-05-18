package de.danielstein.gridgraph;

public class PreCordinateVertex<T> extends Vertex{

    private  final int layerhint;
    private  final int rowhint;
    PreCordinateVertex(int number, int layer, int row) {
        super(number);
        this.layerhint = layer;
        this.rowhint = row;
    }
}
