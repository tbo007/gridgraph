package de.danielstein.gridgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Ein Knoten im Grid.
 *
 */
public class Vertex  extends Tile{

    public final int id;


    private final  Object domainObj;


    public Vertex(int id, Object domainObj) {
        super(new ArrayList<>(), new ArrayList<>());
        this.id = id;
        this.domainObj = domainObj;
    }

    public Collection<Vertex> incomingEdgesFrom() {
        return targetEdges.stream().map(e-> e.source).collect(Collectors.toSet());
    }

    public Collection<Vertex> outgoingEdgesTo() {
        return sourceEdges.stream().map(e-> e.target).collect(Collectors.toSet());
    }

    public boolean isFake() {
        return !isDomainObject();
    }

    public boolean isDomainObject() {
        return domainObj != null;
    }

    public boolean isSpacer() {
        return false;
    }

    public Object getDomainObj() {
        return domainObj;
    }

    @Override
    public String toString() {
        if(isFake()) {
            return "F" + id ;
        }
        if(isDomainObject()) {
            return "D" + id + "(" + domainObj + ")";
        }
        return "Error";
    }

    /**
     * shallowCopy ohne die Edges. Der Aufrufer muss sich um das clonen dieser kümmern.
     * @return
     */
    @Override
    public Vertex clone() {
        Vertex clone  = new Vertex(id,domainObj);
        clone.setLayer(getLayer());
        clone.setRow(getRow());
        return clone;
    }
}
