package de.danielstein.gridgraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Indexierung 1 bassiert...
 * Der Knoten der nur ausgehende Verbindung hat, wird als Start Knoten gewertet.
 * Der Knoten der nur eingehende Verbindung hat, wird als End Knoten gewertet.
 *
 * Es können aber auch Knoten ohne ausgehende Verbindungen gelayoutet werden, in dem
 * eine Kante z.B. zum Ende angegeben wird...
 */
public class GridGraph<T> {

    int vertexNumber = 0;

    double fitness = -1;


    // LinkedHashMap: Vertexe in der Reihenfolge des Hinzufügens verarbeiten
    Map<T,Vertex> domainObj2Vertex = new LinkedHashMap<>();

    Collection<Vertex> fakeVertexes = new ArrayList<>();

    List<List<Vertex>> layers = new ArrayList<>();



    public GridGraph<T> addVertex(T obj) {
        Vertex vertex = newVertex(obj);
        domainObj2Vertex.putIfAbsent(obj,vertex);
        return this;
    }

    public GridGraph<T> addVertex(int layer, int row,T obj) {
        Vertex vertex = newVertex(obj,layer,row);
        domainObj2Vertex.putIfAbsent(obj,vertex);
        return this;
    }

    public GridGraph<T> addEdge(T source , T target, int weight) {
        Vertex sourceVertex = domainObj2Vertex.get(source);
        Vertex targetVertex = domainObj2Vertex.get(target);
        return  addEdge(sourceVertex,targetVertex,weight);
    }

    public GridGraph<T> addEdge(T source , T target) {
        return addEdge(source,target,1);
    }



    public Position getPosition(T domainObject) {
        Vertex vertex = domainObj2Vertex.get(domainObject);
        return getPosition(vertex);
    }




    /** Ein Knoten hat entweder eine vorgegebene Position, oder kommt auf den höchsten Layer
     * seiner Vorgänger plus 1
     * */
    public GridGraph<T> layering() {
        layers.clear();
        domainObj2Vertex.values().forEach(v -> {
            if(v instanceof PreCordinateVertex) {
                PreCordinateVertex pcV = (PreCordinateVertex) v;
                set(pcV.layerhint,pcV.rowhint,pcV);
            } else {
                int layer = maxEdgeCount2Start(v)+1;
                add(layer,v);
            }
        });
        return this;
    }

    public GridGraph<T> addFakeVertexes() {
        List<Edge> sourceEdges = getSourceEdges();
        sourceEdges.forEach( currEdge -> {
            Vertex source = currEdge.source;
            Vertex target = currEdge.target;
            int sourceLayer = getPosition(source).layer;
            int targetLayer = getPosition(target).layer;
            if(targetLayer - sourceLayer == 1) {
                return; // Knoten sind schon direkte Layer Nachbarn
            }
            // Alte Verbindung entfernen und FakeKnoten einfügen
            currEdge.source.sourceConnections.remove(currEdge);
            currEdge.target.targetConnections.remove(currEdge);

            for (int fi = sourceLayer+1; fi <targetLayer ; fi++) {
                target = newVertex(null);
                fakeVertexes.add(target);
                add(fi,target);
                addEdge(source,target, currEdge.weight);
                source = target;
            }
            addEdge(source,currEdge.target,currEdge.weight);
        }
        );
        return this;
    }



    /** Nach Layern sortierte kreuzende Verbindungen
     * So sollte es überkrezungsfrei sein :
     * a1 - b1
     * a2 - b2
     *
     * nicht so: a1  b2
     *             X
     *           a2  b1
     * Zum detektieren: Wenn  a1 < a2, werden die Vertexe geholt, die in den Zeilen über a1 liegen unde deren Target Vertexe
     * im nächsten Layer ermittelt. Danach werden deren Positionen ermittelt. Wenn a1 über einer dieser Positionen liegt hat
     * man ein Kreuz
     * Es kann atürlich auch umgedreht sein ...
     *
     * Ein weiterer Fall ist z.B. dieser:
     *  a1   b1    c1
     *     x Fake x
     *  a1 --> Fake --> c1
     *  a1--> b1 --> c1
     *  Dann Läuft später im Graph die Verbindung über b1 drüber, denn fake gibt es nicht mehr.
     *  Also wird dies als Crossing gewertet. Fakes haben immer nur eine eingehende und ausgehende Verbindung...
     *
     * **/
    public Collection<Edge> getCrossingEdges() {
        Collection<Edge> retval = getSourceEdges().stream().filter(edge -> {
            Position pa1 = getPosition(edge.source);
            Position pb1 = getPosition(edge.target);
            if(pb1 == null) {
                System.out.println("NULL");
            }

                if(pa1.isSmallerRow(pb1)) {
               return getAboveRows(pa1.layer, pa1.row).stream().filter(Objects::nonNull).flatMap(v -> v.sourceConnections.stream())
                        .map(e -> e.target).map(this::getPosition).
                       anyMatch((pb1::isGreaterRow));
            }   if(pa1.isGreaterRow(pb1)) {
               return getBelowRows(pa1.layer, pa1.row).stream().filter(Objects::nonNull).flatMap(v -> v.sourceConnections.stream())
                        .map(e -> e.target).map(this::getPosition).
                       anyMatch((pb1::isSmallerRow));
            }
          return false;

      }).collect(Collectors.toSet());
      fakeVertexes.forEach(fake -> {
            Position fakePos = getPosition(fake);
            Position fakeSourceConVertexPos = getPosition(fake.targetConnections.get(0).source);
            Position fakeTargetConVertexPos = getPosition(fake.sourceConnections.get(0).target);
            int compPos = fakePos.row-1;
            if( compPos== fakeSourceConVertexPos.row && compPos == fakeTargetConVertexPos.row) {
                retval.add(fake.targetConnections.get(0));
            }
        }
        );
      return retval;
    }

    public GridGraph<T> layout() {
        int crossCount = getCrossingEdges().size();
        if(crossCount ==0) {
            return this;
        }
        int maxTries = 100;
        Random random = new Random(4711); // SEED zum nachvollziehen
        List<List<Vertex>> bestlayersSoFar = cloneLayers();

        while(maxTries > 0  && crossCount > 0) {
            List<Integer> layerWithCrossings =  getCrossingEdges().stream().map(e -> e.target).map(this::getPosition).map(p -> p.layer)
                    .distinct().sorted().collect(Collectors.toList());
            for (Integer i: layerWithCrossings) {
                Collections.shuffle(layers.get(i-1),random); // Java 0 based
                int newCrossCount = getCrossingEdges().size();
                if(newCrossCount == 0) {
                    return this;
                }
                if (newCrossCount <= crossCount) {
                    crossCount = newCrossCount;
                    bestlayersSoFar = cloneLayers();
                } else {
                    layers = bestlayersSoFar;
                }
            };
            maxTries--;
        }
        return this;
    }


    // -- Genetic -- //
    public GridGraph<T> clone(){
        GridGraph<T> graph = new GridGraph<>();
        graph.vertexNumber = vertexNumber;
        graph.fakeVertexes = fakeVertexes;
        graph.domainObj2Vertex =  domainObj2Vertex;
        graph.layers = cloneLayers();
        graph.fitness = fitness;
        return graph;
    }

    /** Sucht sich random einen Layer und shuffelt diesen */
    public void mutate() {
        Random random = new Random();
        for (List<?> rows : layers) {
            Collections.shuffle(rows,random);
        }
    }

    public  double getFitness() {
        return  fitness;
    }

    /** (edges - crossingedgges - (lineswitches/floor(edges/4))) / edges **/
    public void calculateFitness(){
        List<Edge> sourceEdges = getSourceEdges();
        double edgeCount = sourceEdges.size();
        double crossings = getCrossingEdges().size();
        double lineSwitches = sourceEdges.stream().map(edge -> {
            Position sourcePos = getPosition(edge.source);
            Position targetPos = getPosition(edge.target);
            return sourcePos.row != targetPos.row ? 1 : 0;
        }).reduce(0, Integer::sum);
        fitness = (edgeCount - crossings - (lineSwitches / Math.floor(edgeCount/4d))) / edgeCount;
    }

    public GridGraph<?> crossover(GridGraph<?> other) {
        GridGraph<?> clone = clone();
        GridGraph<?> otherclone = other.clone();
        for (int i = 0; i < clone.layers.size(); i++) {
            if(i%2 == 0) {
                clone.layers.set(i, otherclone.layers.get(i));
            }
        }
        return clone;
    }

    //--- UtiMethods ---//
    List<List<Vertex>> cloneLayers() {
        List<List<Vertex>> clone = new ArrayList<>(layers.size());
        layers.forEach(vl -> {
            clone.add(new ArrayList<>(vl));
        });
        return clone;
    }

    List<Edge> getSourceEdges() {
        List<Edge> sourceEdges = layers.stream().filter(Objects::nonNull).flatMap(Collection::stream)
                .filter(Objects::nonNull).flatMap(v -> v.sourceConnections.stream()).collect(Collectors.toList());
        return sourceEdges;
    }

    Position getPosition(Vertex vertex) {
        for (int i = 0; i < layers.size(); i++) {
            List<Vertex> rows = layers.get(i);
            int row = rows.indexOf(vertex);
            if (row >= 0) {
                return new Position(i + 1, row + 1); // Java 0 based
            }
        }
        return null;
    }

     GridGraph<T> addEdge(Vertex source , Vertex target, int weight) {
        Edge edge = new Edge(source,target,weight);
        source.sourceConnections.add(edge);
        target.targetConnections.add(edge);
        return this;
    }

    void add(int layer, Vertex v) {
        int row = ensureNextFreeRow(layer);
        List<Vertex> rows = layers.get(layer - 1);// java 0 based
        rows.set(row-1, v);
    }

    /**
     * Setzt den Vertx an der gegebenen Position. Wenn dort schon ein Element ist, wird dieses an die nächst freie Stelle im Layer eingefügt
     * @param layer
     * @param row
     * @param v
     */
    void set(int layer, int row, Vertex v) {
        ensureRowPresent(layer,row);
        List<Vertex> rows = layers.get(layer - 1);// java 0 based
        Vertex prev = rows.set(row -= 1, v);// java 0 based)
        if(prev != null) {
            add(layer,prev);
        }


    }

    Vertex get(int layer, int row) {
        Vertex retVal = null;
        if(layer <= layers.size()) {
            List<Vertex> rows = layers.get(layer - 1);// java 0 based
            if(row <= rows.size()) {
               retVal = rows.get(row-1);
            }
        }
        return retVal;
    }

    Collection<Vertex> getAboveRows(int layer, int row) {
        Collection<Vertex> retVal = Collections.emptyList();
        if(layer <= layers.size()) {
            List<Vertex> rows = layers.get(layer - 1);// java 0 based
            if(row < rows.size()) {
                retVal = new ArrayList<>(rows.subList(row, rows.size())); // row nicht -1, denn wir wollen1 ja alles unter dieser Row...
            }
        }
        return retVal;
    }

    Collection<Vertex> getBelowRows(int layer, int row) {
        Collection<Vertex> retVal = Collections.emptyList();
        if(layer <= layers.size()) {
            List<Vertex> rows = layers.get(layer - 1);// java 0 based
            if(row-1 > 0) {
                retVal = new ArrayList<>(rows.subList(0, row)); // row nicht -1, denn wir wollen1 ja alles über dieser Row...
            }
        }
        return retVal;
    }


    /** 1 based Index **/
    void ensureRowPresent(int layer, int row) {
        int layerNeeded = layer - layers.size();
        for (int i = layerNeeded; i > 0; i--) {
            layers.add(new ArrayList<>());
        }
        ensureLayerHasAtLeastOneMoreThanNeeded(layer,row);
    }

    /** 1 based Index **/
    int ensureNextFreeRow(int layer) {
        int layerNeeded = layer - layers.size();
        for (int i = layerNeeded; i > 0; i--) {
            layers.add(new ArrayList<>());
        }
        List<Vertex> rows = layers.get(layer - 1);
        for (int i = 0; i < rows.size(); i++) {
            if(rows.get(i) == null) {
                return i+1;
            }
        }
        int nextFreeRow = rows.size()+1;
        ensureLayerHasAtLeastOneMoreThanNeeded(layer, nextFreeRow);
        return nextFreeRow;
    }

    /** 1 based Index **/
    private void ensureLayerHasAtLeastOneMoreThanNeeded(int layer, int row) {
        List<Vertex> rows = layers.get(layer-1) ;
            int rowsNeeded = row - rows.size();
            for (int i = rowsNeeded; i > 0; i--) {
                rows.add(null);
            }

    }

    int maxEdgeCount2Start(Vertex v) {
        return maxEdgeCount2Start(v,0);
    }

    int maxEdgeCount2Start(Vertex v, int cnt) {
        if(v.targetConnections.isEmpty()) {
            return cnt;
        }
        cnt++;
        int recVal = cnt;
        for (Edge targetEdge: v.targetConnections){
            cnt = Math.max(cnt,maxEdgeCount2Start(targetEdge.source,recVal));
        }
        return cnt;
    }

    Vertex newVertex(T obj) {
        return new Vertex(obj,++vertexNumber);

    } Vertex newVertex(T obj,int layer, int row) {
        return new PreCordinateVertex(obj,++vertexNumber,layer,row);
    }

    @Override
    public String toString() {
        GridPrinter gridPrinter = new GridPrinter(this);
        return gridPrinter.getGridAsString();
    }

}
