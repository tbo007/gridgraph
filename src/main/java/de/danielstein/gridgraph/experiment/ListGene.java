package de.danielstein.gridgraph.experiment;

import io.jenetics.Gene;
import io.jenetics.util.RandomRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ListGene implements Gene<List<?>, ListGene> {

    private final List<?> layer;

    public ListGene(List<?> layer) {
        this.layer = layer;
    }

    @Override
    public List<?> allele() {
        return layer;
    }

    @Override
    public ListGene newInstance() {
        Random random = RandomRegistry.random();
        ListGene newInstance = newInstance(layer);
        Collections.shuffle(newInstance.allele(),random);
        return newInstance;
    }

    @Override
    public ListGene newInstance(List<?> value) {
        List<?> newInstance = new ArrayList<>(value);
        return new ListGene(newInstance);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
