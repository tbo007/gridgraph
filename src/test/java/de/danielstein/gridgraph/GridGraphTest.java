package de.danielstein.gridgraph;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GridGraphTest extends AbstractTest {



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
        GridGraph<Integer> graph = generateSimpleGraph().layering().addFakeVertexes();

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
        graph.layering().addFakeVertexes();
        Position position = graph.getPosition("start");
        assertEquals(1,position.layer, "layer");
        assertEquals(1,position.row, "row");
        Vertex vEnde = graph.domainObj2Vertex.get("ende");
        assertEquals(3, vEnde.targetConnections.size(),"ende targets");

    }

    @Test
    void getCrossingEdges() {
        GridGraph<String> graph = generateJPL().layering().addFakeVertexes();
        //System.out.println(graph.getCrossingEdges());
        assertEquals(2, graph.getCrossingEdges().size());
    }

    @Test
    void allColumnsEqualRowCount() {
        GridGraph<?> graph = generateComplexJPL().layering().addFakeVertexes();
        assertEquals(1L, graph.layers.stream().map(List::size).distinct().count());

    }

    @Test
    void layout () {
        GridGraph<String> graph = generateJPL().layering().addFakeVertexes();
        Assumptions.assumeFalse(graph.getCrossingEdges().isEmpty());
        graph.layout();
        assertTrue(graph.getCrossingEdges().isEmpty());
        Vertex fakeVertex = graph.get(5, 1);
        assertTrue(fakeVertex.isFake());
    }
}

