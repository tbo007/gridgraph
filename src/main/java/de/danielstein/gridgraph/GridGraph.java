package de.danielstein.gridgraph;

import java.util.*;

/**
 * Indexierung 1 bassiert...
 * Der Knoten der nur ausgehende Verbindung hat, wird als Start Knoten gewertet.
 * Der Knoten der nur eingehende Verbindung hat, wird als End Knoten gewertet.
 *
 * Es können aber auch Knoten ohne ausgehende Verbindungen gelayoutet werden, in dem
 * eine Kante z.B. zum Ende angegeben wird...
 */
public class GridGraph<T> {

    public class Position {
        public final int layer;
        public final int row;

        public Position(int layer, int row) {
            this.layer = layer;
            this.row = row;
        }
    }

    private int vertexNumber = 0;

    final Map<T,Vertex> domainObj2Vertex = new HashMap<>();

    final List<List<Vertex>> layers = new ArrayList<>();



    public GridGraph<T> addVertex(T obj) {
        Vertex vertex = newVertex();
        domainObj2Vertex.putIfAbsent(obj,vertex);
        return this;
    }

    public GridGraph<T> addVertex(int layer, int row,T obj) {
        Vertex vertex = newVertex(layer,row);
        domainObj2Vertex.putIfAbsent(obj,vertex);
        return this;
    }

    public GridGraph<T> addEdge(T source , T target, int weight) {
        Vertex sourceVertex = domainObj2Vertex.get(source);
        Vertex targetVertex = domainObj2Vertex.get(target);
        Edge edge = new Edge(sourceVertex,targetVertex,weight);
        sourceVertex.sourceConnections.add(edge);
        targetVertex.targetConnections.add(edge);
        return this;
    }

    public Position getPosition(T domainObject) {
        Vertex vertex = domainObj2Vertex.get(domainObject);
        for (int i = 0; i < layers.size(); i++) {
            List<Vertex> rows = layers.get(i);
            int row = rows.indexOf(vertex);
            if (row >= 0) {
                return new Position(i + 1, row + 1); // Java 0 based
            }
        }
        return null;
    }



    /** Ein Knoten hat entweder eine vorgegebene Position, oder kommt auf den höchsten Layer
     * seiner Vorgänger plus 1
     * */
    public GridGraph<T> layering() {
        layers.clear();
        domainObj2Vertex.values().forEach(v -> {
            if(v instanceof PreCordinateVertex) {
                PreCordinateVertex pcV = (PreCordinateVertex) v;
                set(pcV.layerhint,pcV.rowhint,pcV);
            } else {
                int layer = maxEdgeCount2Start(v)+1;
                add(layer,v);
            }
        });
        return this;
    }

    void addFakeNotes() {
    }




    //---- UtiMethods ---

    void add(int layer, Vertex v) {
        ensureRowPresent(layer,1);
        List<Vertex> rows = layers.get(layer - 1);// java 0 based
        rows.add(v);
    }

    /**
     * Setzt den Vertx an der gegdeben Position. WEnn dort schon ein Elment ist, werden dieses und alle anderen
     * eins weitergeschoben
     * @param layer
     * @param row
     * @param v
     */
    void set(int layer, int row, Vertex v) {
        ensureRowPresent(layer,row);
        List<Vertex> rows = layers.get(layer - 1);// java 0 based
        row-=1;// java 0 based
        Vertex vertex = rows.get(row);
        if(vertex == null) {
            rows.set(row,v);
        } else {
            rows.add(row,v);
        }
    }

    Vertex get(int layer, int row) {
        List<Vertex> rows = layers.get(layer - 1);// java 0 based
        return rows.get(row-1);
    }

    void ensureRowPresent(int layer, int row) {
        int layerNeeded = layer - layers.size();
        for (int i = layerNeeded; i >0 ; i--) {
            layers.add(new ArrayList<>());
        }
        List<Vertex> rows = layers.get(layer - 1);// java 0 based
        int rowsNeeded = row - rows.size();
        for (int i = rowsNeeded; i >0 ; i--) {
            rows.add(null);
        }
    }

    int maxEdgeCount2Start(Vertex v) {
        return maxEdgeCount2Start(v,0);
    }

    int maxEdgeCount2Start(Vertex v, int cnt) {
        if(v.targetConnections.isEmpty()) {
            return cnt;
        }
        cnt++;
        int recVal = cnt;
        for (Edge targetEdge: v.targetConnections){
            cnt = Math.max(cnt,maxEdgeCount2Start(targetEdge.source,recVal));
        }
        return cnt;
    }



    Vertex newVertex() {
        return new Vertex(++vertexNumber);

    } Vertex newVertex(int layer, int row) {
        return new PreCordinateVertex(++vertexNumber,layer,row);
    }


}
