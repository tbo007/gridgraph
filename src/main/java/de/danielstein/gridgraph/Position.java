package de.danielstein.gridgraph;

import java.util.Objects;

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

    public boolean isGreaterRow(Position other) {
        return row > other.row;

    }public boolean isSmallerRow(Position other) {
        return row < other.row;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return layer == position.layer && row == position.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(layer, row);
    }
}
