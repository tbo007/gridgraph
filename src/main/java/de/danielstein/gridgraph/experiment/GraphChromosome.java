package de.danielstein.gridgraph.experiment;

import de.danielstein.gridgraph.GridGraph;
import de.danielstein.gridgraph.Vertex;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Als Proxy zwischen GridGraph und Chromosome verwenden **/
public class GraphChromosome implements Chromosome<ListGene> {

    private final List<ListGene> chromosome;



    private final GridGraph<?> gridGraph;

    private GraphChromosome(List<ListGene> chromosome, GridGraph graph) {
        this.chromosome = chromosome;
        this.gridGraph =  graph;
        graph.setLayers(map(chromosome));
    }

    public GraphChromosome(GridGraph<?> graph) {
        this.gridGraph = graph;
        chromosome =  cloneLayers(graph.getLayers()).stream().map(ListGene::new).collect(Collectors.toList());
    }

    List<List<Vertex>> cloneLayers(List<List<Vertex>> layers) {
        List<List<Vertex>> clone = new ArrayList<>(layers.size());
        layers.forEach(vl -> {
            clone.add(new ArrayList<>(vl));
        });
        return clone;
    }

    List<List<?>> map(List<ListGene> chromosome) {
        return chromosome.stream().map(ListGene::allele).collect(Collectors.toList());
    }

    public Integer calculateFitness() {
        return gridGraph.absoluteFitness();
    }

  public Integer countCrossings() {
        return gridGraph.getCrossingEdges().size();
    }




    @Override
    public Chromosome<ListGene> newInstance(ISeq<ListGene> genes) {
        return new GraphChromosome(genes.asList(),gridGraph.clone());

    }

    @Override
    public ListGene get(int index) {
        return chromosome.get(index);
    }

    @Override
    public int length() {
        return chromosome.size();
    }

    @Override
    public Chromosome<ListGene> newInstance() {
        return new GraphChromosome(chromosome.stream().map(ListGene::newInstance).collect(Collectors.toList()), gridGraph.clone());

    }

    public GridGraph<?> getGridGraph() {
        return gridGraph;
    }
}
