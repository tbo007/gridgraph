package de.danielstein.gridgraph.experiment;

import de.danielstein.gridgraph.GridGraph;
import io.jenetics.AnyGene;
import io.jenetics.Gene;
import io.jenetics.Mutator;

import java.util.Random;

public class SwapRowMutator extends Mutator<AnyGene<GridGraph<?>>, Integer> {


    public SwapRowMutator(double probability) {
        super(probability);
    }

    @Override
    protected AnyGene<GridGraph<?>> mutate(AnyGene<GridGraph<?>> gene, Random random) {
        // return super.mutate(gene, random);
        GridGraph<?> clone = gene.allele().clone();
        clone.swapRows(random);
        return gene.newInstance(clone);
    }




}
