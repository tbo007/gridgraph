package de.danielstein.gridgraph;

import de.danielstein.gridgraph.experiment.GraphChromosome;
import de.danielstein.gridgraph.experiment.ListGene;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
import io.jenetics.stat.DoubleMomentStatistics;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class JeneticsTest extends AbstractTest{


    @Test
    public void layoutKBM02(){
        GridGraph<Integer> graph = generateJPLkbm002().layering().addFakeVertexes().mergeFakes2Connection();
        GridGraph<?> layouted =doDaMagic(graph);
        System.out.println(layouted);
    }

    @Test
    void layoutComplexJPL() {
        GridGraph<Integer> graph = generateComplexJPL().layering().addFakeVertexes().mergeFakes2Connection();
        GridGraph<?> gridGraph = doDaMagic(graph);
        System.out.println(gridGraph);

    }

    @Test
    void layout2PathsJpl() {
        GridGraph<Integer> graph = generateJPLWithTwoMajorPaths().layering().addFakeVertexes().mergeFakes2Connection();
        GridGraph<?> gridGraph = doDaMagic(graph);
        System.out.println(gridGraph);

    }


@Test
public void layoutStabdardJPL(){
    GridGraph<String> graph = generateJPL().layering().addFakeVertexes();
    GridGraph<?> layouted =doDaMagic(graph);
    System.out.println(layouted);
}



    public static Integer eval(final Genotype<ListGene> gt) {
        GraphChromosome chromosome = (GraphChromosome) gt.chromosome();
        return chromosome.calculateFitness();

    } public static Integer countCrossings(final Genotype<ListGene> gt) {
        GraphChromosome chromosome = (GraphChromosome) gt.chromosome();
        return chromosome.countCrossings();
    }

    public GridGraph<?> doDaMagic(GridGraph<?> graph) {
        GraphChromosome graphChromosome = new GraphChromosome(graph);

        // 1.) Define the genotype (factory) suitable for the problem.
        Genotype<ListGene> genotype = Genotype.of(graphChromosome);

        Alterer<ListGene, Integer> alteres = Alterer.of(
                new SinglePointCrossover<ListGene, Integer>(0.3),
                new Mutator<>(0.15)
        );

        // 3.) Create the execution environment.
        Engine<ListGene, Integer> engine = Engine.builder(JeneticsTest::countCrossings, genotype)
                //.maximizing()
                .minimizing()
                        .populationSize(100)

                        .alterers(alteres)
                .build();

        // EvolutionStatistics sammeln
        EvolutionStatistics<Integer, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
        int max_gen = 120_000;
        // 4.) Start the execution (evolution) and collect the result.
        final Genotype<ListGene> result = engine.stream()
                .limit(Limits.byFitnessThreshold(1))
               // .limit( e-> e.bestFitness() != null && e.bestFitness() ==0)
               // .limit(Limits.bySteadyFitness(max_gen/4))
               // .limit(max_gen)
                .limit(Limits.byExecutionTime(Duration.ofMinutes(10)))
                .peek(statistics)
                .collect(EvolutionResult.toBestGenotype());

        // Ausgabe der Statistiken
        System.out.println(statistics);

        // Ausgabe des besten Genotyps
       // System.out.println(result);
        return  ((GraphChromosome) result.chromosome()).getGridGraph();
    }
}
