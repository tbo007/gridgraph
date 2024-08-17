package de.danielstein.gridgraph;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  Ein Objekt im Grid mit einer Position. Wird als Platzhalter verwendet und ist Basis für die weiteren Objekte im Grid.
 *  Eine Tile hat selber nie Verbindungen, aber das Verwalten der Verbindungen ist trotzdem hier angelegt,
 *  weil dann das Arbeiten später mit Objekten im Graph einfacher ist, da wenn es um Verbindungen
 *  geht nicht nach Typ unterschieden werden muss.
 *
 */
public class Tile implements Cloneable {

    private int layer;

    private int row;

    final List<Edge> targetEdges;

    final List<Edge> sourceEdges;

    public Tile () {
        targetEdges = Collections.EMPTY_LIST;
        sourceEdges = Collections.EMPTY_LIST;
    }

    Tile (List<Edge> sourceEdges, List<Edge> targetEdges) {
        this.sourceEdges = sourceEdges;
        this.targetEdges = targetEdges;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public boolean isFake() {
        return false;
    }

    public boolean isDomainObject() {
        return false;
    }

    public boolean isSpacer() {
        return true;
    }

    @Override
    public String toString() {
        if(isSpacer()) {
            return " ";
        }
        return "Error";
    }

    @Override
    public Tile clone() {
        Tile clone = new Tile();
        clone.layer = layer;
        clone.row = row;
        return clone;

    }
}
