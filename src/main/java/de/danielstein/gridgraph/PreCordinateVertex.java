package de.danielstein.gridgraph;

public class PreCordinateVertex extends Vertex{

    final int layerhint;
    final int rowhint;
    PreCordinateVertex(Object domObj, int number, int layer, int row) {
        super(domObj,number);
        this.layerhint = layer;
        this.rowhint = row;
    }
}
