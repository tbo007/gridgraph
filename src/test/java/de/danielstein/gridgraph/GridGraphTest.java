package de.danielstein.gridgraph;

import static org.junit.jupiter.api.Assertions.*;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.IntStream;

public class GridGraphTest {


    /**
     * 1---2---3---4
     * |---5-------|
     */
    private GridGraph<Integer> generateSimpleGraph() {
        Integer v1 = Integer.valueOf(1);
        Integer v2 = Integer.valueOf(2); //PrePos
        Integer v3 = Integer.valueOf(3);
        Integer v4 = Integer.valueOf(4);
        Integer v5 = Integer.valueOf(5);
        return new GridGraph<Integer>().addVertex(v1).addVertex(2,1,v2).addVertex(v3).addVertex(v4)
                .addVertex(v5)
                .addEdge(v1, v2, 1).addEdge(v2, v3, 1).addEdge(v3, v4, 1)
                .addEdge(v1, v5, 1).addEdge(v5, v4, 1);
    }

    /**
     * START--FANL--SAVE--DBVA-------------ENDE
     *           +           +--RESTORE----+
     *           +-----------+--FREL-------+
     */
    private GridGraph<String> generateJPL() {
        List<String> v = Arrays.asList("start", "fanl", "save", "dbva", "restore", "frel", "ende");
        GridGraph<String>  graph = new GridGraph<String>();
        v.forEach(graph::addVertex);
        graph.addEdge(v.get(0),v.get(1)).addEdge(v.get(1),v.get(2)).addEdge(v.get(2),v.get(3)).addEdge(v.get(3),v.get(6))
                .addEdge(v.get(3),v.get(4)).addEdge(v.get(3),v.get(5))
                .addEdge(v.get(4),v.get(6)).addEdge(v.get(5), v.get(6))
                .addEdge(v.get(1),v.get(5));
        return graph;
    }

    @Test
    public void testMaxEdgeCount2Start() {
        GridGraph<Integer> graph = generateSimpleGraph();
        assertEquals(2,graph.domainObj2Vertex.get(4).targetConnections.size());
        assertEquals(3,graph.maxEdgeCount2Start(graph.domainObj2Vertex.get(4)));
        assertEquals(2,graph.maxEdgeCount2Start(graph.domainObj2Vertex.get(3)));
        assertEquals(0,graph.maxEdgeCount2Start(graph.domainObj2Vertex.get(1)));
    }

    @Test
    void ensureRowPresent() {
        GridGraph<Integer> gridGraph = new GridGraph<>();
        gridGraph.ensureRowPresent(9,15);
        List<Vertex> rowsLayer8 = gridGraph.layers.get(8);
        List<Vertex> rowsLayer2 = gridGraph.layers.get(2);
        assertEquals(15,rowsLayer8.size());
    }

    @Test
    void layering() {
        GridGraph<Integer> graph = generateSimpleGraph().layering();
        Position position = graph.getPosition(Integer.valueOf(2));
        assertEquals(2,position.layer);
        assertEquals(1,position.row);

    }

    @Test
    void addFakeNotes() {
        GridGraph<Integer> graph = generateSimpleGraph().layering().addFakeNotes();

        Vertex v5 = graph.domainObj2Vertex.get(Integer.valueOf(5));
        Vertex v4 = graph.domainObj2Vertex.get(Integer.valueOf(4));
        Vertex assumeV5 = v5.sourceConnections.get(0).source;
        Vertex fakeEdge = v5.sourceConnections.get(0).target;
        Vertex assumeV4 = fakeEdge.sourceConnections.get(0).target;
        assertEquals(v5,assumeV5);
        assertEquals(v4,assumeV4);
    }

    @Test
    void addFakeNotes2() {
        GridGraph<String> graph = generateJPL();
        graph.layering().addFakeNotes();
        Position position = graph.getPosition("start");
        assertEquals(1,position.layer, "layer");
        assertEquals(1,position.row, "row");
        Vertex vEnde = graph.domainObj2Vertex.get("ende");
        assertEquals(3, vEnde.targetConnections.size(),"ende targets");

    }

    @Test
    void getCrossingEdges() {
        GridGraph<String> graph = generateJPL().layering().addFakeNotes();
        System.out.println(graph.getCrossingEdges());
        assertEquals(3, graph.getCrossingEdges().size());

    }

    @Test
    void shuffleTest () {
        Random random = new SecureRandom();
        GridGraph<String> graph = generateJPL().layering().addFakeNotes();
        IntStream.rangeClosed(1,100).boxed().forEach( i ->  {

            int crSize = graph.getCrossingEdges().size();

                System.out.println(crSize);
//           graph.layers.forEach(r -> Collections.shuffle(r,random));
            List<Vertex> rows = graph.layers.get(3);
            Collections.shuffle(rows,random);

                }
        );


    }
}

