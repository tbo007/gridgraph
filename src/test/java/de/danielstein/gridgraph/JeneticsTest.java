package de.danielstein.gridgraph;

import de.danielstein.gridgraph.experiment.SwapRowMutator;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.stat.MinMax;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JeneticsTest extends AbstractTest{


@Test
public void layoutStandardJPL(){
    GridGraph<String> graph = generateJPL().prepare();
    System.out.println(graph);
    doDaMagic(graph).forEach( g -> {
        g.swapEnd2Top();
        String string = g.toString();
        System.out.println(string.hashCode());
        System.out.println(string);
        System.out.println(g.getCrossingEdges());
    }  );
}


    @Test
    public void layoutComplexJPL(){
//    RandomRegistry.random(new Random(4711));
        GridGraph<Integer> graph = generateComplexJPL().prepare();
        System.out.println(graph );
        System.out.println(graph.getCrossingEdges().size());
        Comparator<GridGraph> fitnessComp = (g1, g2) -> {
            int f1 = g1.getCrossingEdges().size();
            int f2 = g2.getCrossingEdges().size();
            return Integer.compare(f1,f2);
        };
        doDaMagic(graph).stream().sorted(fitnessComp).limit(10).forEach( g -> {
            String string = g.toString();
            System.out.println(string);
            System.out.println("CrossingEdges: " + g.getCrossingEdges().size());
        }  );
    }

    @Test
    public void layout2PathsJPL(){
        GridGraph<Integer> graph = generateJPLWithTwoMajorPaths().prepare();
        System.out.println(graph );
        System.out.println(graph.getCrossingEdges().size());
        //doDaMagic(graph);
        doDaMagic(graph).stream().filter(g -> g.getCrossingEdges().size() <= 2).forEach( g -> {
            String string = g.toString();
            System.out.println(string);
            System.out.println("CrossingEdges: " + g.getCrossingEdges().size());
        }  );
    }

    public Collection<GridGraph<?>> doDaMagic(GridGraph<?> graph) {
        Supplier<GridGraph<?>> graphSupplier = () ->  {
            Random random = RandomRegistry.random();
            GridGraph<?> clone = graph.clone();
            clone.mutate(random);
            return clone;
        };

        Codec<GridGraph<?>, AnyGene<GridGraph<?>>> CODEC =
                Codec.of(Genotype.of(AnyChromosome.of(graphSupplier)),gt -> gt.gene().allele());

        final Engine<AnyGene<GridGraph<?>>, Integer> engine =
                Engine.builder( g -> g.getCrossingEdges().size(), CODEC)
                        .minimizing().
                        populationSize(100).
                        alterers(
                                new Mutator<>(0.30)
                                , new SwapRowMutator(0.15)
                        )
                        .build();

        // EvolutionStatistics sammeln
        EvolutionStatistics<Integer, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();


        //ISeq<EvolutionResult<AnyGene<GridGraph<?>>, Integer>> result = engine.stream().limit(10_000).peek(statistics)
        //        .flatMap(MinMax.toStrictlyDecreasing()).collect(ISeq.toISeq(10));

        Map<String, GridGraph<?>> bestUniqueResults = engine.stream()
                //.limit(Limits.byFitnessThreshold(1))
                //.limit(10_000)
                .limit(Limits.byExecutionTime(Duration.ofSeconds(2)))
                //.peek(s -> System.out.println(s.s.generation() + " / " + s.worstFitness()))
                .peek(statistics)
                         .map(EvolutionResult::bestPhenotype)
                .map(p -> p.genotype().gene().allele()).collect(Collectors.toMap(GridGraph::toString, p -> p, (p, q) -> p));
        // Ausgabe der Statistiken
        System.out.println(statistics);

        // Ausgabe des besten Genotyps
       // System.out.println(result);
        //result.stream().map(EvolutionResult::bestPhenotype).map(Phenotype::genotype).map(Genotype::chromosome).map(g -> g.gene().allele()).collect(Collectors.toList());
        return bestUniqueResults.values();




    }
}
