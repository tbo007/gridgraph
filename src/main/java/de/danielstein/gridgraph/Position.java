package de.danielstein.gridgraph;

public class Position {
    public final int layer;
    public final int row;

    public Position(int layer, int row) {
        this.layer = layer;
        this.row = row;
    }

    @Override
    public String toString() {
        return "Position{" +
                "layer=" + layer +
                ", row=" + row +
                '}';
    }
}
