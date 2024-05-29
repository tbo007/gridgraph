package de.danielstein.gridgraph;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeneticGridGraphTest {


    /**
     * START + SAVE x INFO  + ENDE
     *       + DBVA x FREL  +
     *
     *       SAVE --> FREL
     *       DBVA --> INFO
     *
     *       Erwartet: 2 Crossings / 2 Lineswitches
     */
    private GridGraph<String> generateCrossedPlan() {
        //                                0        1       2       3      4       5
        List<String> v = Arrays.asList("start", "save", "info", "dbva", "frel", "ende");
        GridGraph<String>  graph = new GridGraph<>();
        v.forEach(graph::addVertex);
        graph.addEdge(v.get(0),v.get(1)).addEdge(v.get(0),v.get(3)).addEdge(v.get(1),v.get(4)).addEdge(v.get(3),v.get(2))
                .addEdge(v.get(2),v.get(5)).addEdge(v.get(4),v.get(5));

        return graph;
    }
    @Test
    void getCrossingEdges2() {
        GridGraph<String> graph = generateCrossedPlan().layering().addFakeVertexes();
        assertEquals(2, graph.getCrossingEdges().size());
    }

    @Test
    void calculateFitness() {
        GridGraph<String> graph = generateCrossedPlan().layering().addFakeVertexes();
        graph.calculateFitness();
        assertEquals(0.66d,graph.getFitness(),0.01d);
    }
}