//package de.danielstein.gridgraph.experiment;
//
//import de.danielstein.gridgraph.GridGraph;
//import io.jenetics.*;
//import io.jenetics.util.MSeq;
//import io.jenetics.util.RandomRegistry;
//import io.jenetics.util.Seq;
//
//import java.util.Random;
//
//import static io.jenetics.internal.math.Randoms.indexes;
//import static java.lang.Math.min;
//
///** Wält ein Individum aus, mutiert es und ersetzt das Individum mit dem Mutant*/
//public class Replacer<
//        G extends Gene<?, G>,
//        C extends Comparable<? super C>
//        >
//        extends Recombinator<G, C> {
//    /**
//     * Constructs an alterer with a given recombination probability.
//     *
//     * @param probability The recombination probability.
//     * @throws IllegalArgumentException if the {@code probability} is not in the
//     *                                  valid range of {@code [0, 1]} or the given {@code order} is
//     *                                  smaller than two.
//     */
//    public Replacer(double probability) {
//        super(probability, 2);
//    }
//
//
//    @Override
//    protected int recombine(MSeq<Phenotype<G, C>> population, int[] individuals, long generation) {
//        final Random random = RandomRegistry.random();
//
//        final Phenotype<G, C> pt1 = population.get(individuals[0]);
//        final Genotype<G> gt1 = pt1.genotype();
//
//        Chromosome<G> chromosome;
//        Chromosome<G> chromosomeToReplace = gt1.chromosome();
//        if(chromosomeToReplace instanceof  GraphChromosome) {
//            GraphChromosome graphChromosome = (GraphChromosome) chromosomeToReplace;
//            GridGraph<?> gridGraph = graphChromosome.getGridGraph();
//            gridGraph.mutate(random);
//            chromosome = (Chromosome<G>) new GraphChromosome(gridGraph);
//        } else {
//            chromosome = chromosomeToReplace.newInstance();
//        }
//
//        population.set(individuals[0], Phenotype.of(Genotype.of(chromosome), generation));
//        return 1;
//    }
//}
//
//
