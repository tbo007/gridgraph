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

    private int vertexNumber = 0;

    final Map<T,Vertex> domainObj2Vertex = new HashMap<>();

    final List<List<Vertex>> layers = new ArrayList<>();



    public GridGraph<T> addVertex(T obj) {
        Vertex vertex = newVertex();
        domainObj2Vertex.putIfAbsent(obj,vertex);
        return this;
    }

    public GridGraph<T> addVertex(T obj, int layer, int row) {
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

    /** Geht alle ausgehenden Verbindungen rekursiv durch und verteilt die Knoten auf Layer.
     * Außerdem werden Fakeknoten eingefügt, wenn eine Kante länger als ein Layer ist,
     *      * so dass jede Kante maximal auf einen Knoten im nächstgelegenen Layer zeigt.
     * */
//    public GridGraph<T> layering() {
       // layers.clear()
//        domainObj2Vertex.values().forEach(v -> {
//            int layer, row;
//            if(v instanceof PreCordinateVertex) {
//                PreCordinateVertex pcV = (PreCordinateVertex) v;
//
//                layer = pcV.layerhint;
//                row = pcV.rowhint;
//            } else
//
//
//        });
//
//
//        return this;
//    }

    void addFakeNotes() {
    }


    //---- UtiMethods ---

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


    Vertex findEnd() {
        return domainObj2Vertex.values().stream().filter(v -> v.sourceConnections.isEmpty()).findAny().get();
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
        return new PreCordinateVertex<>(++vertexNumber,layer,row);
    }


}
