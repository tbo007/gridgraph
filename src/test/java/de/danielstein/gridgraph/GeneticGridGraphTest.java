package de.danielstein.gridgraph;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class GeneticGridGraphTest extends  AbstractTest{



    @Test
    void getCrossingEdges2() {
        GridGraph<String> graph = generateCrossedPlan().layering().addFakeVertexes();
        assertEquals(2, graph.getCrossingEdges().size());
    }

//    @Test
//    void calculateFitness() {
//        GridGraph<String> graph = generateCrossedPlan().layering().addFakeVertexes();
//        graph.calculateFitnessFactors();
//        assertEquals(0.66d,graph.getFitness(),0.01d);
//    }

    @Test
    void clone10k () {
        GridGraph<String> graph = generateCrossedPlan().layering().addFakeVertexes();
        Supplier<GridGraph<?>> s = () -> {
            GridGraph<String> clone = graph.clone();
            clone.mutate();
            clone.calculateFitness();
            return clone;
        };

//        List<? extends GridGraph<?>> collect = IntStream.rangeClosed(1, 10000).boxed().map(i -> s.get()).collect(Collectors.toList());
//        System.out.println(collect.size());
        List<CompletableFuture<GridGraph<?>>> cfs = IntStream.rangeClosed(1, 10000000).boxed().
                map(i -> CompletableFuture.supplyAsync(s)).collect(Collectors.toList());
        List<GridGraph<?>> graphs = cfs.stream().map(CompletableFuture::join).collect(Collectors.toList());
        System.out.println(graphs.size());
    }

    @Test
    public void layout(){
        GridGraph<String> graph = generateCrossedPlan().layering().addFakeVertexes();
        GeneticLayout gl = new GeneticLayout(graph);
        GridGraph<?> layouted = gl.layout();
        System.out.println(new GridPrinter(layouted).getGridAsString());
    }  @Test
    public void layout2(){
        GridGraph<String> graph = generateJPL().layering().addFakeVertexes();
        GeneticLayout gl = new GeneticLayout(graph);
        GridGraph<?> layouted = gl.layout();
        System.out.println(new GridPrinter(layouted).getGridAsString());
    }

    @Test
    public void layout3(){
        GridGraph<Integer> graph = generateComplexJPL().layering().addFakeVertexes().mergeFakes2Connection();
        GeneticLayout gl = new GeneticLayout(graph);
        GridGraph<?> layouted = gl.layout();
        System.out.println(new GridPrinter(layouted).getGridAsString());

    }
    @Test
    public void layoutKBM02(){
        GridGraph<Integer> graph = generateJPLkbm002().layering().addFakeVertexes().mergeFakes2Connection();
        GeneticLayout gl = new GeneticLayout(graph);
        GridGraph<?> layouted = gl.layout();
        System.out.println(new GridPrinter(layouted).getGridAsString());
    }

    @Test
    public void gentest(){
        GridGraph<Integer> graph = generateComplexJPL().layering().addFakeVertexes();
        System.out.println(graph);
    }
}