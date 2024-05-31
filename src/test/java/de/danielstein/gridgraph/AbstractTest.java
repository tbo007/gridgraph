package de.danielstein.gridgraph;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public abstract class AbstractTest {

    /**
     * START + SAVE x INFO  + ENDE
     *       + DBVA x FREL  +
     *
     *       SAVE --> FREL
     *       DBVA --> INFO
     *
     *       Erwartet: 2 Crossings / 2 Lineswitches
     */
     GridGraph<String> generateCrossedPlan() {
        //                                0        1       2       3      4       5
        List<String> v = Arrays.asList("start", "save", "info", "dbva", "frel", "ende");
        GridGraph<String>  graph = new GridGraph<>();
        v.forEach(graph::addVertex);
        graph.addEdge(v.get(0),v.get(1)).addEdge(v.get(0),v.get(3)).addEdge(v.get(1),v.get(4)).addEdge(v.get(3),v.get(2))
                .addEdge(v.get(2),v.get(5)).addEdge(v.get(4),v.get(5));

        return graph;
    }


    /**
     * 1---2---3---4
     * |---5-------|
     */
     GridGraph<Integer> generateSimpleGraph() {
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
     GridGraph<String> generateJPL() {
        List<String> v = Arrays.asList("start", "fanl", "save", "dbva", "restore", "frel", "ende");
        GridGraph<String>  graph = new GridGraph<String>();
        v.forEach(graph::addVertex);
        graph.addEdge(v.get(0),v.get(1)).addEdge(v.get(1),v.get(2)).addEdge(v.get(2),v.get(3)).addEdge(v.get(3),v.get(6))
                .addEdge(v.get(3),v.get(4)).addEdge(v.get(3),v.get(5))
                .addEdge(v.get(4),v.get(6)).addEdge(v.get(5), v.get(6))
                .addEdge(v.get(1),v.get(5));
        return graph;

    }


    /** Beispiel aus Obsidian Note 14b503*/
    GridGraph<Integer> complexJPL() {

        GridGraph<Integer>  graph = new GridGraph<Integer>();
        IntStream.rangeClosed(1,16).boxed().forEach(graph::addVertex);
        graph.addEdge(1,2).addEdge(2,3).addEdge(3,4).addEdge(3,15)
                .addEdge(4,5).addEdge(4,14).addEdge(5,6).addEdge(6,7)
                .addEdge(6,9).addEdge(7,8).addEdge(7,9).addEdge(8,10)
                .addEdge(9,11).addEdge(10,15).addEdge(11,12).addEdge(11,13)
                .addEdge(12,13).addEdge(12,14).addEdge(13,15).addEdge(14,15)
                .addEdge(15,16);
        return graph;

    }
}
