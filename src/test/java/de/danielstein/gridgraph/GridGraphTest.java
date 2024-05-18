package de.danielstein.gridgraph;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GridGraphTest {


    /**
     * 1---2---3---4
     * |-----------|
     */
    private GridGraph<Integer> generateTestGraph() {
        Integer v1 = Integer.valueOf(1);
        Integer v2 = Integer.valueOf(2);
        Integer v3 = Integer.valueOf(3);
        Integer v4 = Integer.valueOf(4);
        return new GridGraph<Integer>().addVertex(v1).addVertex(v2).addVertex(v3).addVertex(v4)
                .addEdge(v1, v2, 1).addEdge(v2, v3, 1).addEdge(v3, v4, 1)
                .addEdge(v1, v4, 1);



    }

    @Test
    public void testCountEdges2Start() {
        GridGraph<Integer> graph = generateTestGraph();
        assertEquals(2,graph.domainObj2Vertex.get(4).targetConnections.size());
        assertEquals(3,graph.maxEdgeCount2Start(graph.domainObj2Vertex.get(4)));
        assertEquals(2,graph.maxEdgeCount2Start(graph.domainObj2Vertex.get(3)));
        assertEquals(0,graph.maxEdgeCount2Start(graph.domainObj2Vertex.get(1)));
    }
}

