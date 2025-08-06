package de.danielstein.gridgraph;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GridGraphTest extends AbstractTest {





    // Position in der Liste ist der erwartete Layer...
    // Frel hat Vorgänger auf 1 (Fanl) und auf 3 (DBVA)
    // gehört also auf Layer 4
    @Test
    void layering() {
        GridGraph<String> graph = generateJPL().layering();
        List<String> v = Arrays.asList("start", "fanl", "save", "dbva", "frel", "ende");
        for (int i = 0; i < v.size(); i++) {
            Vertex vertex = graph.getVertex(v.get(i));
            assertEquals(i, vertex.getLayer(), vertex +" not on expected layer");
        }
    }
    @Test
    void printgraph () {
        GridGraph<Integer> graph = generateComplexJPL().prepare();
        System.out.println(graph);
    }


    private List<Integer> walkBackToNoneFakes(Vertex start) {
        List<Integer> retVal = new ArrayList<>();
        start.targetEdges.stream().map(Edge::getSource).forEach(source -> {
            if (source.isFake()) {
                retVal.addAll(walkBackToNoneFakes(source));
            } else {
                retVal.add((Integer) (source.getDomainObj()));
            }
        });
        return retVal;
    }


    @Test
    void addFakeNotes() {
        GridGraph<Integer> graph = generateJPLkbm002().layering().addFakeVertexes();
        testGraphStructure(graph);
        System.out.println(graph);
    }

    /** Zwei Pfad JPL... */
    @Test
    void addFakeNotes2() {
        GridGraph<Integer> graph = generateJPLWithTwoMajorPaths().layering().addFakeVertexes();
        Set<Long> fakeCountLayer3_5 = IntStream.rangeClosed(3, 5).mapToObj(graph.layers::get).map(l -> l.stream().filter(Tile::isFake).count()).collect(Collectors.toSet());
        assertEquals(1,fakeCountLayer3_5.size());
        assertEquals(2,fakeCountLayer3_5.iterator().next());
    }

    /** Jeder Vertex darf maximal eine eingehende FakeNode haben **/
    @Test
    void addFakeNotes3() {
        GridGraph<Integer> graph = generateJPLWithTwoMajorPaths().layering().addFakeVertexes();
        assertSingleIncomingFakeNode(graph);
        graph = generateComplexJPL().layering().addFakeVertexes();
        assertSingleIncomingFakeNode(graph);
        graph = generateJPLkbm002().layering().addFakeVertexes();
        assertSingleIncomingFakeNode(graph);
    }


    @Test
    void testclone() {
        GridGraph<Integer> orig = generateJPLkbm002().layering().addFakeVertexes();
        GridGraph<Integer> clone = orig.clone();
        testGraphStructure(clone);
        assertEquals(orig.vertexSequence.get(), clone.vertexSequence.get());
        assertEquals(orig.toString(), clone.toString());
    }

    @Test
    void testarrangeGridAndAlignFakesInRows() {
        Stream.of(generateComplexJPL(),generateJPLWithTwoMajorPaths(),generateJPLkbm002())
                .map(g -> g.layering().addFakeVertexes().arrangeGridAndAlignFakesInRows()).forEach(GridGraphTest::assertFakesAlleOnOneRow);
    }

    /**
     *         S0      S1      S2      S3      S4      S5      S6      S7
     * Z0   D1(1)   D2(2)   D3(3)   D4(4)   D6(5)   D7(6)   D8(7)
     * Z1                             F12     F15     F14  D5(10)
     * Z2                                                   D9(8)
     * Z3                                                  D10(9)
     * Z4                                                     F17 D11(11)
     */
    @Test
    void  testSwapTile() {
        GridGraph<Integer> graph = generateJPLkbm002().prepare();
        System.out.println(graph);
        Vertex d8 = graph.getVertex(8);
        Vertex d9 = graph.getVertex(9);
        Vertex d10 = graph.getVertex(10);
        Vertex d11 = graph.getVertex(11);
        // geht nicht weil F17 auf D(10) nicht erlaubt


        assertFalse(graph.swapTiles(d11.getLayer(), d11.getRow(), d10.getRow()));
        System.out.println(graph);
        // geht weil D10 auf D9 geht und die Fakes von D10 auf Spacer ist auch erlaubt
        assertTrue(graph.swapTiles(d10.getLayer(), d10.getRow(), d9.getRow()));
         System.out.println(graph);
        assertFakesAlleOnOneRow(graph);
        assertTrue(graph.swapTiles(d11.getLayer(), d11.getRow(), d8.getRow()));
         System.out.println(graph);
        assertFakesAlleOnOneRow(graph);
        assertTrue(graph.swapTiles(d10.getLayer(),d10.getRow(),0));
        System.out.println(graph);
        assertFakesAlleOnOneRow(graph);
    }

    /**
     *         S0      S1      S2      S3      S4      S5      S6      S7
     * Z0   D1(1)   D2(2)   D3(3)   D4(4)   D5(5)   D6(6)   D7(7)       S
     * Z1       S       S       S       S       S       S   D8(8)       S
     * Z2       S       S       S       S       S       S   D9(9)       S
     * Z3       S       S       S     F12     F15     F16 D10(10)       S
     * Z4       S       S       S       S       S       S     F17 D11(11)
     */
    @Test
    void testSwapRow(){
        GridGraph<Integer> graph = generateJPLkbm002().prepare();
        List<Tile> row4 = graph.getRow(4);
        List<Tile> row0 = graph.getRow(0);
        graph.swapRow(4,0);
        assertEquals(graph.getRow(0), row4);
        assertEquals(graph.getRow(4), row0);
    }


    /*
                S0          S1          S2          S3          S4          S5
Z0   D1(start)    D2(fanl)    D3(save)    D4(dbva) D5(restore)           S
Z1           S           S          F8          F9    D6(frel)           S
Z2           S           S           S           S         F10    D7(ende)
    Erwarte Crossings
    Edge{source=F9, target=D6(frel)}
    Edge{source=D4(dbva), target=F10}

                S0          S1          S2          S3          S4          S5
Z0   D1(start)    D2(fanl)    D3(save)    D4(dbva)         F10    D7(ende)
Z1           S           S           S           S D5(restore)           S
Z2           S           S          F8          F9    D6(frel)           S

0
     */
    @Test
    void getCrossingEdges() {
        GridGraph<Integer> graph = new GridGraph<>();
        graph.addEdge(1,2);
        graph.addEdge(3,4);
        graph = graph.prepare();
        assertEquals(0, graph.getCrossingEdges().size());
        assertTrue(graph.swapTiles(1,0,1));
        assertEquals(2, graph.getCrossingEdges().size());

    }

    private static void assertFakesAlleOnOneRow(GridGraph<?> graph) {
        List<Vertex> vertexsWithIncomingFakes = graph.layers.stream().flatMap(List::stream).filter(Tile::isDomainObject).map(Vertex.class::cast)
                .filter(v -> v.targetEdges.stream().map(Edge::getSource).anyMatch(Vertex::isFake)).collect(Collectors.toList());
        for (Vertex v:vertexsWithIncomingFakes ) {
            int row = v.getRow();
            Optional<Vertex> nextFake = Optional.empty();
            do {
               nextFake = v.targetEdges.stream().map(Edge::getSource).filter(Vertex::isFake).findAny();
               if(nextFake.isEmpty()) {
                   break;
               }
               v = nextFake.get() ;
               assertEquals(row, v.getRow());
            }while (nextFake.isPresent());
        }
    }


    private void assertSingleIncomingFakeNode(GridGraph<?> graph) {
        Optional<Long> max = graph.layers.stream().flatMap(List::stream).filter(Predicate.not(Tile::isSpacer)).map(Vertex.class::cast).map(
                v -> v.targetEdges.stream().map(Edge::getSource).filter(Vertex::isFake).count()).max(Long::compare);
        assertEquals(1L,max.get());

    }


    /** Es muss von 10 einen Weg nach 3,4 (transitiv) und 6 direkt geben, snst ist was beim mergen
     * der Fakes schiefgegangen @see {@link #addFakeNotes()} und 10 darf nur noch eine eingehende FakeVerbindung
     * vom Layer vorher haben. */
    private void testGraphStructure(GridGraph<Integer> graph) {
        assertEquals(1, graph.layers.get(5).stream().filter(Tile::isFake).count());
        Vertex ten = graph.getVertex(10);
        List<Tile> fakesLayer5 = graph.layers.get(5).stream().filter(Tile::isFake).collect(Collectors.toList());
        List<Vertex> tenIncommingFakes = ten.targetEdges.stream().map(Edge::getSource).filter(Vertex::isFake).collect(Collectors.toList());
        assertEquals(fakesLayer5,tenIncommingFakes);
        List<Integer> expectedDomainObjs = Arrays.asList(3, 4, 6);
        List<Integer> actualConnectedNonFakes = walkBackToNoneFakes(ten);
        Collections.sort(actualConnectedNonFakes);
        assertEquals(expectedDomainObjs,actualConnectedNonFakes );
    }





//
//
//    @Test
//    void mergeFakeNotes2() {
//        GridGraph<Integer> graph = generateComplexJPL().layering().addFakeVertexes();
//        System.out.println(graph);
//        graph.mergeFakes2Connection();
//        System.out.println(graph);
//        System.out.println("Connections");
//        graph.getSourceEdges().stream().map(e -> e.source.number + " -> " + e.target.number).forEach(System.out::println);
//
//    }
//
//    @Test
//    void  calc () {
//        DecimalFormat df = new DecimalFormat("###,###,###,###");
//        System.out.println(df.format(12d*Math.pow(24d,7d)*4d*4d*4d));
//        System.out.println(df.format(3d*Math.pow(4d,3d)*Math.pow(2d,5d)));
//
//    }
//
//
//    @Test
//    void addFakeNotes2() {
//        GridGraph<String> graph = generateJPL();
//        graph.layering().addFakeVertexes();
//        Position position = graph.getPosition("start");
//        assertEquals(1,position.layer, "layer");
//        assertEquals(1,position.row, "row");
//        Vertex vEnde = graph.domainObj2Vertex.get("ende");
//        assertEquals(3, vEnde.targetEdges.size(),"ende targets");
//
//    }
//

//
//    @Test
//    void getCrossingEdgesOne() {
//        GridGraph<String> graph = generateJPL().layering().addFakeVertexes();
//        printDetailedGraphInfo(graph);
//        assertEquals(1, graph.getCrossingEdges().size());
//    }    @Test
//    void getCrossingEdgesfour() {
//        GridGraph<String> graph = generateJPL().layering().addFakeVertexes();
//        Collections.swap(graph.getLayers().get(4),0,1);
//        printDetailedGraphInfo(graph);
//
//        assertEquals(4, graph.getCrossingEdges().size());
//    }
//
//    private static void printDetailedGraphInfo(GridGraph<String> graph) {
//        System.out.println(graph);
//        System.out.println("Crossings");
//        graph.getCrossingEdges().stream().map(e -> e.source.number + " -> " + e.target.number).forEach(System.out::println);
//        System.out.println("Connections");
//        graph.getSourceEdges().stream().map(e -> e.source.number + " -> " + e.target.number).forEach(System.out::println);
//    }
//
//    /*
//                  S0     S1     S2     S3     S4     S5     S6     S7
//            Z0   1(1)   2(2)   3(3)   4(4)   5(5)  F(14)   7(7) 11(11)
//            Z1                       F(12)  F(13)  F(16)   8(8)
//            Z2                              F(15)   6(6)   9(9)
//            Z3                                           10(10)
//            Z4                                            F(17)
//
//            Warum wird crossing 14 --> 10 und 6 --> 8 nicht gesehen
//     */
//    @Test
//    void getCrossingEdgesKBM02() {
//        GridGraph<Integer> graph = generateJPLkbm002().layering().addFakeVertexes();
//                //.mergeFakes2Connection();
//        graph.getLayers().get(5).sort(Comparator.comparing(Vertex::isFake).reversed());
//        System.out.println(graph);
//        graph.getCrossingEdges().stream().map(e -> e.source.number + " -> " + e.target.number).forEach(System.out::println);
//
//       // assertEquals(2, graph.getCrossingEdges().size());
//    }
//
//    @Test
//    void allLayersEqualRowCount() {
//        GridGraph<?> graph = generateComplexJPL().layering().addFakeVertexes();
//        assertEquals(1L, graph.layers.stream().map(List::size).distinct().count());
//
//    }
//
//    @Test
//    void atLeastOneLayerWithNonNullRow() {
//        GridGraph<?> graph = generateComplexJPL().layering().addFakeVertexes();
//        assertEquals(1L, graph.layers.stream().filter(l -> l.contains(null)).count());
//
//    }
//
//

//
//    @Test
//    void layout () {
//        GridGraph<String> graph = generateJPL().layering().addFakeVertexes();
//        Assumptions.assumeFalse(graph.getCrossingEdges().isEmpty());
//        graph.layout();
//        assertTrue(graph.getCrossingEdges().isEmpty());
//        Vertex fakeVertex = graph.get(5, 1);
//        assertTrue(fakeVertex.isFake());
//    }
}

