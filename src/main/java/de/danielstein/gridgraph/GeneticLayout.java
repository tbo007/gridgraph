package de.danielstein.gridgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeneticLayout {

    private final Comparator<GridGraph<?>> FITNESS_COMP;

    public static final int GENERATION_SIZE = 250;
    public static final int GENERATION_COUNT = 1000;
    public static final int ELITISM_PERCENT = 1;
    public static final int MUTATION_PERCENT = 50;
    private final  GridGraph<?> startGraph;

    public GeneticLayout(GridGraph<?> startGraph) {
        this.startGraph = startGraph;
        FITNESS_COMP = (o1, o2) -> {
            return Double.compare(o2.getFitness(), o1.getFitness());

        };
    }

    public GridGraph<?> layout() {
        List<GridGraph<?>>  generation = initGeneration();
       // generation.stream().limit(100).forEach(System.out::println);

        generation.sort(FITNESS_COMP);


        System.out.println(generationFitnessStat(generation));
        generation = new ArrayList<>(generation.subList(0,GENERATION_SIZE));

        int genCount = GENERATION_COUNT -1;
        double fittestOf100Gen = 0;
        while (genCount > 0) {
            generation = breadNewGeneration(generation);

            if(genCount % 100   == 0) {
                System.out.println("Gen: " + (GENERATION_COUNT - genCount) + ": " + generationFitnessStat(generation));
               /* if(Math.abs(generation.get(0).getFitness() - fittestOf100Gen) < 0.0001) {
                    break;
                }*/
                    fittestOf100Gen = generation.get(0).getFitness();
            }
            genCount--;
        }
        return generation.get(0);
    }

    private String generationFitnessStat(List<GridGraph<?>> l) {
        StringBuilder retval = new StringBuilder();
        retval.append("Fitness: Overall: ");
        double fitness_gen = (double) (Double) l.stream().map(GridGraph::getFitness).mapToDouble(Double::doubleValue).sum() /  (double )l.size();
        retval.append(fitness_gen);
        retval.append(" fittest: ");
        retval.append(l.get(0).getFitness());
        return  retval.toString();

    }

    private List<GridGraph<?>> breadNewGeneration(List<GridGraph<?>> oldGen) {
        List<CompletableFuture<GridGraph<?>>> retVal = new ArrayList<>(oldGen.size());
        List<GridGraph<?>> newGeneration = new ArrayList<>(oldGen.size());
        int elitism_index = GENERATION_SIZE * ELITISM_PERCENT / 100;
        newGeneration.addAll(oldGen.subList(0,elitism_index));
        oldGen = oldGen.subList(0,oldGen.size()/2);
        Collections.shuffle(oldGen);
        int mutate_index = GENERATION_SIZE * MUTATION_PERCENT /100;
        List<GridGraph<?>> mutatorsList = oldGen.subList(0, mutate_index);
        for (GridGraph<?> g : mutatorsList) {
            retVal.add(CompletableFuture.supplyAsync(mutate(g)));
        }
        int missing_in_gen = GENERATION_SIZE-newGeneration.size() - retVal.size();
        int i = 0;
        while (missing_in_gen > 0) {
            GridGraph<?> a = oldGen.get(i++);
            if(i==oldGen.size()) {
                i = 0;
            }
            GridGraph<?> b = oldGen.get(i++);
            if(i==oldGen.size()) {
                i = 0;
            }
            retVal.add(CompletableFuture.supplyAsync(crossover(a,b)));
            retVal.add(CompletableFuture.supplyAsync(crossover(b,a)));
            missing_in_gen-=2;

        }
        newGeneration.addAll(retVal.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        newGeneration.sort(FITNESS_COMP);
        return newGeneration;
    }

    private List<GridGraph<?>> initGeneration () {
        int initGenSize = startGraph.getSourceEdges().size()*10_000;
        List<CompletableFuture<GridGraph<?>>> asyncComp = IntStream.rangeClosed(1, initGenSize).boxed()
                .map(i -> CompletableFuture.supplyAsync(mutate(startGraph))).collect(Collectors.toList());
        return asyncComp.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }

    private Supplier<GridGraph<?>> mutate(GridGraph<?> graph) {
        return () -> {
            GridGraph<?> mutate = graph.clone();
            mutate.mutate();
            mutate.calculateFitness();
            return mutate;
        };
    }

    private Supplier<GridGraph<?>> crossover(GridGraph<?> a , GridGraph<?> b) {
        return () -> {
            GridGraph<?> crossover = a.crossover(b);
            crossover.calculateFitness();
            return crossover;
        };
    }
}
