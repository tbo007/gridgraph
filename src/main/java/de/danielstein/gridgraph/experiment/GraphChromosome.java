//package de.danielstein.gridgraph.experiment;
//
//import de.danielstein.gridgraph.Edge;
//import de.danielstein.gridgraph.GridGraph;
//import de.danielstein.gridgraph.Vertex;
//import io.jenetics.Chromosome;
//import io.jenetics.internal.collection.Array;
//import io.jenetics.util.ISeq;
//import io.jenetics.util.RandomRegistry;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
///** Als Proxy zwischen GridGraph und Chromosome verwenden **/
//public class GraphChromosome implements Chromosome<ListGene> {
//
//    private final List<ListGene> chromosome;
//
//
//
//    private final GridGraph<?> gridGraph;
//
//    private GraphChromosome(List<ListGene> chromosome, GridGraph graph) {
//        this.chromosome = chromosome;
//        this.gridGraph =  graph;
//        graph.setLayers(map(chromosome));
//    }
//
//    public GraphChromosome(GridGraph<?> graph) {
//        this.gridGraph = graph;
//        chromosome =  cloneLayers(graph.getLayers()).stream().map(ListGene::new).collect(Collectors.toList());
//    }
//
//    List<List<Vertex>> cloneLayers(List<List<Vertex>> layers) {
//        List<List<Vertex>> clone = new ArrayList<>(layers.size());
//        layers.forEach(vl -> {
//            clone.add(new ArrayList<>(vl));
//        });
//        return clone;
//    }
//
//    List<List<?>> map(List<ListGene> chromosome) {
//        return chromosome.stream().map(ListGene::allele).collect(Collectors.toList());
//    }
//
//    public Integer calculateFitness() {
//        return gridGraph.absoluteFitness();
//    }
//
//  public Integer countCrossings() {
//        return gridGraph.getCrossingEdges().size();
//    }
//
//
//
//
//    @Override
//    public Chromosome<ListGene> newInstance(ISeq<ListGene> genes) {
//        return new GraphChromosome(genes.asList(),gridGraph.clone());
//
//    }
//
//    @Override
//    public ListGene get(int index) {
//        return chromosome.get(index);
//    }
//
//    @Override
//    public int length() {
//        return chromosome.size();
//    }
//
//    @Override
//    public Chromosome<ListGene> newInstance() {
//        GridGraph<?> clone = gridGraph.clone();
//        clone.mutate(RandomRegistry.random());
//        return new GraphChromosome(clone);
//
//
//
////        return new GraphChromosome(chromosome.stream().map(ListGene::newInstance).collect(Collectors.toList()), gridGraph.clone());
//
////        Random random = RandomRegistry.random();
////        GridGraph<?>  clonedGraph = gridGraph.clone();
////        Collection<Edge> crossingEdges = clonedGraph.getCrossingEdges();
////        Set<Vertex> crossingVertexes = new HashSet<>();
////        crossingEdges.stream().forEach( e-> {
////                    crossingVertexes.add(e.source);
////                    crossingVertexes.add(e.target);
////                }
////        );
////        List<List<Vertex>> layers = clonedGraph.getLayers();
////        for (List<Vertex> layer : layers) {
////            Set<Integer> pos2Swap = new HashSet<>();
////            for (int i = 0; i < layer.size(); i++) {
////                if(crossingVertexes.contains(layer.get(i))) {
////                    pos2Swap.add(i);
////                }
////            }
////
////            if (!pos2Swap.isEmpty()) {
////                Collections.shuffle(layer,random);
////            }
////            //System.out.println(layer);
//////            for (Integer oldPos : pos2Swap) {
//////                Integer newpos;
//////                do {
//////                    newpos = random.nextInt(layer.size());
//////                } while (oldPos.equals(newpos));
//////
//////                Collections.swap(layer, oldPos, newpos);
//////
//////                // System.out.println((Arrays.asList(layer, integer, random.nextInt(layer.size()))));
//////            }
////        }
////        return new GraphChromosome(layers.stream().map(ListGene::new).collect(Collectors.toList()), clonedGraph);
//    }
//
//    public GridGraph<?> getGridGraph() {
//        return gridGraph;
//    }
//}
