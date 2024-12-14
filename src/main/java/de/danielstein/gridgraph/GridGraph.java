package de.danielstein.gridgraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Layering Grid Graph Implementation
 * The Grid is determined by the Edges in the graph.
 * columns:the vertex witch is the furthest away (measured by its incoming edges) from
 * any vertex with no incoming edges determines the column count
 * rows: The column with the most vertices determines the rowsize for the entire grid
 *
 *
 *
 * Ussage (supossing JOBS is tge Domain Class):
 * 1. prepare
 * GridGraph<JOBS> graph = new GridGraph<>()
 * For every Vertex: @see {@link #addVertex(T domainObj)}
 * For every Edge: @see {@link #addEdge(T source , T target)}
 * graph = @see {@link #prepare()}
 *
 * 2. layouting (e.g. using Jenetics)
 * - using {@link #swapRows(Random)} / {@link #mutate(Random)}
 *
 *
 * 3. query Domainobj positions (Indexing is 0 based)
 * @see #getVertex(T domainObj)
 *
 *
 * @param <T> Type of the domainObjects represented in this Graph
 * @author Daniel Stein
 *
 *
 */
public class GridGraph<T>  implements  Cloneable{

    final AtomicInteger vertexSequence;

    // LinkedHashMap: Using Vertices in adding order
    private  Map<T,Vertex> domainObj2Vertex = new LinkedHashMap<>();

    List<List<Tile>> layers = new ArrayList<>();

    public GridGraph () {
        this(0);
    }

    protected GridGraph(int vertexSeqStart) {
        vertexSequence = new AtomicInteger(vertexSeqStart);
    }
    

//    ------ public Methods ----

    /**
     * Turns this into a usable GridGraph, by doing the following steps
     * 1. @see {@link #layering()}
     * 2. @see {@link #addFakeVertexes()}
     * 2.1. @see {@link #mergeFakeVertices()}
     * 2.2.
     * 4. @see {@link #arrangeGridAndAlignFakesInRows()}
     *
     * 3. @see {@link #arrangeGridAndAlignFakesInRows()}
     * @return this
     */
    public GridGraph<T> prepare() {
        return layering().addFakeVertexes().arrangeGridAndAlignFakesInRows();
    }

    public GridGraph<T> addVertex(T domainObj) {
        domainObj2Vertex.putIfAbsent(domainObj,newVertex(domainObj));
        return this;
    }

    public GridGraph<T> addEdge(T source , T target) {
        Vertex sourceVertex = domainObj2Vertex.get(source);
        Vertex targetVertex = domainObj2Vertex.get(target);
        return  addEdge(sourceVertex,targetVertex);
    }

    /**
     * Distributes all Vertices to their target layer.
     * the layer is determined by the max layer of its incoming vertexes plus one
     * E.g.
     * START--FANL--SAVE--DBVA-------------ENDE
     *           +           +--RESTORE----+
     *           +-----------+--FREL-------+
     * This graph has six layers 0-5.
     * The vertex FREL has incoming Connections from layer 1 (START) and layer 3 (DBVA)
     * So its layer is 4.
     */
    public GridGraph<T> layering() {
        layers.clear();
        domainObj2Vertex.values().forEach(v -> {
            int layer = determineLayer(v);
            add(layer,v);
        });
        return this;
    }

    /** For simplifiying crossing detection: If a vertex at layer one is connected to a vertex on layer four:
     * Replace this connection with multiple connections, of which each only span one layer. To do so insert
     * fake vertexes on layer two and three and connect domain vertex on layer one with fake on layer two. fake
     * on layer two with fake on layer three and this fake with the original domain vertex on layer four.
     * @return
     */
    public GridGraph<T> addFakeVertexes() {
        List<Edge> sourceEdges = getSourceEdges();
        sourceEdges.forEach( currEdge -> {
                    Vertex source = currEdge.source;
                    Vertex target = currEdge.target;
                    int sourceLayer = source.getLayer();
                    int targetLayer = target.getLayer();
                    if(targetLayer - sourceLayer == 1) {
                        return; // Vertexes are layer neighbours.
                    }
                    // remove multilayer connection for replacement
                    currEdge.source.sourceEdges.remove(currEdge);
                    currEdge.target.targetEdges.remove(currEdge);

                    for (int fi = sourceLayer+1; fi < targetLayer ; fi++) {
                        target = newVertex(null);
                        add(fi,target);
                        addEdge(source,target);
                        source = target;
                    }
                    // Make Connection from the last fakevertex to the domain Vertex of the
                    // replaced connection
                    addEdge(source,currEdge.target);
                }
        );
        mergeFakeVertices();

        Integer maxLayerSize = layers.stream().map(Collection::size).max(Integer::compareTo).get();
        IntStream.range(0,layers.size()).forEach(i -> ensureLayerHasAtLeast(i,maxLayerSize));
        return this;
    }

    /**
     * Im prevLayer Clone in allen Tiles {@link Tile#sourceEdges} eintragen
     * Im currentLayer Clone in allen Tiles {@link Tile#targetEdges} eintragen

     * @return
     */
    public GridGraph<T> clone() {
        GridGraph<T> graphClone = new GridGraph<>(vertexSequence.get());
        for (int i = 0; i < layers.size(); i++) {
            List<Tile> currLayer = layers.get(i);
            List<Tile> currLayerClone = new ArrayList<>(currLayer.size());
            for (Tile tile: currLayer) {
                Tile tileClone = tile.clone();
                currLayerClone.add(tileClone);
            }
            graphClone.layers.add(currLayerClone);
        }

        List<Edge> edges = getSourceEdges();
        edges.addAll(getTargetEdges());
        for (Edge edge: edges) {
            Vertex source = edge.getSource();
            Vertex target = edge.getTarget();
            Vertex sourceClone = (Vertex) graphClone.layers.get(source.getLayer()).get(source.getRow());
            Vertex targetClone = (Vertex) graphClone.layers.get(target.getLayer()).get(target.getRow());
            graphClone.addEdge(sourceClone,targetClone);
        }

        return graphClone;
    }

    /**
     * Arranges the grid to fit all domain objects and their connections, ensuring that all recursively incoming
     * fake vertices are placed in the same row. This method performs the following steps:
     * 1. Creates a grid with the correct size.
     * 2. Transfers all domain objects and their incoming connections, ensuring connections are on the same row.
     * 3. Deletes rows from layers that contain only spacers.
     *
     * @return A GridGraph with domain objects distributed and fake vertices aligned in rows.
     */
    public GridGraph<T> arrangeGridAndAlignFakesInRows() {
        // Original layers are saved in 'originalLayers' and 'layers' is re-initialized.
        List<List<Tile>> originalLayers = layers;
        layers = new ArrayList<>();

        // Ensure each layer from the original has a corresponding layer in the new grid.
        IntStream.range(0, originalLayers.size()).forEach(this::ensureLayerPresent);

        // Get the number of rows from the first layer.
        int numberOfRows = originalLayers.get(0).size();

        // Ensure that each layer has at least 'numberOfRows' rows.
        IntStream.range(0, originalLayers.size()).forEach(i -> ensureLayerHasAtLeast(i, numberOfRows));

        // Add each domain object to a row, incrementing row index until successful.
        for (Map.Entry<T, Vertex> entry : domainObj2Vertex.entrySet()) {
            int rowIndex = 0;
            while (!placeAndAlignFakes(rowIndex, entry.getValue())) {
                rowIndex++;
            }
        }

        // Remove rows that consist solely of spacer tiles.
        int rowCount = layers.get(0).size();
        IntStream.range(0, rowCount)
                .filter(rowIndex -> getRow(rowIndex).stream().allMatch(Tile::isSpacer))
                .boxed()
                .sorted(Comparator.reverseOrder())
                .forEach(rowIndex -> {
                    layers.forEach(layer -> layer.remove(rowIndex.intValue()));
                });

        return this;
    }



    /**
     * Mutieren
     * tileSwap(layer, rowFrom, rowTo).
     * - Möglich wenn rowto keine eingehende FakeVerbindung hat. Wenn rowFrom eingehende FakeVerbindungen hat, dann transitiver Swap probieren wie bei add2Rows
     * - Wenn from ein Fake ist, ist kein tausch möglich.
     * - Spacer und Vertexe ohne FakeVerbindungen können geswappt werden, wobei zwei Spacer nicht geswappt werden können, denn dies wäre eine null Operation.
     * Wenn das nicht funktioniert dann rowSwap(rowFrom, rowTo)
     * @param random
     */
    public void mutate(Random random) {
        int ilayer = random.nextInt(layers.size());
        List<Tile> layer = layers.get(ilayer);
        int ifrom = random.nextInt(layer.size());
        int ito = ifrom;
        while (ito == ifrom) {
            ito = random.nextInt(layer.size());
        }
        if(!swapTiles(ilayer,ifrom,ito)) {
            swapRow(ifrom,ito);
        }
    }

    public void swapRows(Random random) {
        int ilayer = random.nextInt(layers.size());
        List<Tile> layer = layers.get(ilayer);
        int ifrom = random.nextInt(layer.size());
        int ito = ifrom;
        while (ito == ifrom) {
            ito = random.nextInt(layer.size());
        }
        swapRow(ifrom,ito);

    }

    public boolean swapTiles(int iLayer, int iRowFrom, int iRowTo) {
        Tile tileFrom = getTile(iLayer,iRowFrom);
        return swapTiles(tileFrom,iRowTo,tileFrom);
    }

    public void swapRow(int iFrom, int iTo) {
        IntStream.range(0, layers.size()).forEach(i -> swap(i, iFrom,iTo));
    }




    /**
     * Multiobjective Optimization minimize Vector:
     * [0] = anzahl Line crossings
     * [1] = anzahl Line switch : Wenn ein Vertex mit nur einer ausgehenden nicht fake Verbindung nicht auf der selben Row liegt wie das Ziel der Verbindung.
     * Wenn ein Vertex mit nur einer eingehenden nicht fake Verbindung nicht auf der selben Row liegt wie sein Vorgänger
     * @return
     */
    public int []  fitness() {
        return null;
    }


    public Set<Edge> getCrossingEdges() {
        Set<Edge> retVal = new HashSet<>();
        for (List<Tile> layer : layers) {
            List<Edge> sourceEdges = layer.stream().flatMap(v -> v.sourceEdges.stream()).collect(Collectors.toList());
            // Gehe alle distinct Kombinationen der Edges durch um intersects zu prüfen
            // Beispiel 4 Edges Intersection Prüfung = [1,2],[1,3],[1,4],[2,3],[2,4],[3,4]
            // Wenn es nur eine ausgehende Edge gibt, kann es kein Crossing geben
            for (int i = 0; i < sourceEdges.size() - 1; i++) {
                for (int j = i + 1; j < sourceEdges.size(); j++) {
                    Edge e1 = sourceEdges.get(i);
                    Edge e2 = sourceEdges.get(j);
                    if (Geom.lineIntersect(
                            e1.source.getLayer(), e1.source.getRow(), e1.target.getLayer(), e1.target.getRow(),
                            e2.source.getLayer(), e2.source.getRow(), e2.target.getLayer(), e2.target.getRow())) {
                        retVal.add(e1);
                        retVal.add(e2);
                    }

                }
            }
        }
        return  retVal;
    }

    public Vertex getVertex(T domainobj) {
        if(domainObj2Vertex.isEmpty()) {
           domainObj2Vertex = (Map<T, Vertex>) layers.stream().flatMap(List::stream).filter(Tile::isDomainObject).map(Vertex.class::cast).collect(Collectors.toMap(Vertex::getDomainObj, Function.identity()));
        }
        return domainObj2Vertex.get(domainobj);
    }
    public Vertex getVertex(int layer, int row) {
        return (Vertex) getTile(layer,row);
    }

    public Tile getTile(int layer, int row) {
        return layers.get(layer).get(row);
    }


    public List<Tile> getRow(int iRow ) {
        List<Tile> row = new ArrayList<>(layers.size());
        layers.forEach(l -> row.add(l.get(iRow)));
        return row;
    }

    public boolean swapEnd2Top() {
        getVertex(null);
        Vertex end = domainObj2Vertex.values().stream().filter(Tile::isDomainObject).filter(t -> t.sourceEdges.isEmpty()).findFirst().get();
        return swapTiles(end.getLayer(), end.getRow(), 0);
    }


    @Override
    public String toString() {
        return new GridPrinter(this).getGridAsString();
    }




    //    ------ private  Methods ----


    /**
     *
     * @param tileFrom
     * @param iRowTo
     * @param tileSwapOrigin
     * @return
     */
    private boolean swapTiles(Tile tileFrom, int iRowTo, Tile tileSwapOrigin) {
        boolean swapOk = true;
        Tile tileTo = getTile(tileFrom.getLayer(),iRowTo);
        if(tileFrom.isSpacer() && tileTo.isSpacer()) {
            return false;
        }
        if(tileTo.isFake()) {
            return false;
        }
        if (tileTo.isDomainObject() && tileTo.targetEdges.stream().map(Edge::getSource).anyMatch(Vertex::isFake)) {
            return false;
        }
        Optional<Vertex> incomingFake = tileFrom.targetEdges.stream().map(Edge::getSource).filter(Vertex::isFake).findAny();
            if(swapOk && incomingFake.isPresent()) {
                swapOk &= swapTiles(incomingFake.get(),iRowTo,tileSwapOrigin);
                if(!swapOk) {
                    return  swapOk;
                }
            }
        if(swapOk) {
            swap(tileFrom.getLayer(), tileFrom.getRow(), tileTo.getRow());
        }
        return  swapOk;
    }


    /**
     * Attempts to place the given vertex and all its recursively incoming fake vertices in the specified row.
     * If the tile at the specified layer and row is a spacer, the vertex can be placed.
     * If the vertex has incoming fake edges, the method recursively attempts to place these vertices first.
     * If the vertex and its dependencies are successfully placed, the method sets the vertex in the grid.
     *
     * @param row The row index where the vertex and its fake vertices should be placed.
     * @param vertex The vertex to be placed.
     * @return true if the vertex and all its recursively incoming fakes were successfully placed, false otherwise.
     */
    private boolean placeAndAlignFakes(int row, Vertex vertex) {
        boolean canPlace = false;

        // Check if the tile at the specified layer and row is a spacer.
        Tile tile = getTile(vertex.getLayer(), row);
        if (tile.isSpacer()) {
            canPlace = true;
        }

        // If canPlace is true, check for incoming fake vertices and place them recursively.
        Optional<Vertex> incomingFake = vertex.targetEdges.stream()
                .map(Edge::getSource)
                .filter(Vertex::isFake)
                .findAny();
        if (canPlace && incomingFake.isPresent()) {
            canPlace &= placeAndAlignFakes(row, incomingFake.get());
            if (!canPlace) {
                return canPlace;
            }
        }

        // If canPlace is still true, set the vertex in the grid.
        if (canPlace) {
            set(vertex.getLayer(), row, vertex);
        }

        return canPlace;
    }




    private void mergeFakeVertices() {
        List<Vertex> vertexWithFakesPointingTo =
                getSourceEdges().stream().filter(e -> e.source.isFake() && !e.target.isFake())
                        .map(e -> e.target).distinct().collect(Collectors.toList());
        vertexWithFakesPointingTo.forEach(this::mergeFakeVertices);
    }

    /**
     * Merges all fake vertices connected to the given start vertex into one.
     * This method performs the following steps:
     * 1. Collects all incoming fake vertices connected to the start vertex.
     * 2. If there are less than two fake vertices, the method returns.
     * 3. Otherwise, iterates over the list of incoming fake vertices.
     * 4. For each vertex to be merged:
     *    a. Transfers all outgoing edges from the vertex to be merged to the primary fake vertex.
     *    b. Transfers all incoming edges to the vertex to be merged to the primary fake vertex.
     *    c. Removes the vertex to be merged from its layer in the grid.
     * 5. Recursively calls itself with the merged vertex to ensure all fake vertices are merged.
     *
     * @param startVertex The vertex from which to start merging fake vertices.
     */
    private void mergeFakeVertices(Vertex startVertex) {

        // Collect all incoming fake vertices connected to the start vertex.
        List<Vertex> incomingFakeVertices = startVertex.incomingEdgesFrom().stream()
                .filter(Vertex::isFake)
                .collect(Collectors.toList());

        // If there are less than two fake vertices, exit the method.
        if (incomingFakeVertices.size() < 2) {
            return;
        }

        // Create an iterator over the list of incoming fake vertices.
        ListIterator<Vertex> fakeVertexIterator = incomingFakeVertices.listIterator();
        Vertex primaryFakeVertex = fakeVertexIterator.next();

        while (fakeVertexIterator.hasNext()) {
            Vertex vertexToMerge = fakeVertexIterator.next();

            // Transfer all outgoing edges from the vertex to be merged to the primary fake vertex.
            vertexToMerge.outgoingEdgesTo().forEach(outgoingVertex -> {
                removeEdge(vertexToMerge, outgoingVertex);
                addEdge(primaryFakeVertex, outgoingVertex);
            });

            // Transfer all incoming edges to the vertex to be merged to the primary fake vertex.
            vertexToMerge.incomingEdgesFrom().forEach(incomingVertex -> {
                removeEdge(incomingVertex, vertexToMerge);
                addEdge(incomingVertex, primaryFakeVertex);
            });

            // Remove the vertex to be merged from its layer in the grid.
            List<Tile> rowTiles = layers.get(vertexToMerge.getLayer());
            set(vertexToMerge.getLayer(), vertexToMerge.getRow(), null);
        }

        // Recursively call the method with the merged vertex.
        mergeFakeVertices(primaryFakeVertex);
    }


    /*
        z.B. ilayer = 0 und layersize = 0 --> Anlage neuer Layer
     */
    private void ensureLayerPresent(int iLayer) {
        if (layers.size() <= iLayer) {
            layers.add(new ArrayList<>());
        }
    }

    /**
     * Setzt die übergebene Tile and die gewünschte Position im Grid und updatet
     * die Position in der Tile.
     * @param iLayer
     * @param row
     * @param tile Wenn null, dann wird ein Tile als Platzhalter erzeugt und gesetzt
     * @return null, wenn vorher nichts war an der Position oder das zuvor gesetzte Tile
     */
    private Tile set(int iLayer, int row, Tile tile) {
        List<Tile> layer = layers.get(iLayer);
        if (tile == null) {
            tile = new Tile();
        }
        tile.setLayer(iLayer);
        tile.setRow(row);
        if (row == layer.size()) {
            layer.add(tile);
            return null;
        } else {
            return  layer.set(row,tile);
        }
    }

    private void swap(int iLayer, int iFrom, int iTo) {
        List<Tile> layer = layers.get(iLayer);
        Tile tileFrom = layer.get(iFrom);
        tileFrom.setRow(iTo);
        Tile tileTo = layer.get(iTo);
        tileTo.setRow(iFrom);
        layer.set(iTo,tileFrom);
        layer.set(iFrom,tileTo);
    }


    private void add (int iLayer, Tile tile) {
        ensureLayerPresent(iLayer);
        set(iLayer,layers.get(iLayer).size(),tile);
    }

    private int determineLayer(Vertex v) {
        return determineLayer(v,0);
    }

    /**
     * Determines
     * @param v
     * @param cnt
     * @return
     * todo: method name 
     */
    private int determineLayer(Vertex v, int cnt) {
        if(v.targetEdges.isEmpty()) {
            return cnt;
        }
        cnt++;
        int recVal = cnt;
        for (Edge targetEdge: v.targetEdges){
            cnt = Math.max(cnt, determineLayer(targetEdge.source,recVal));
        }
        return cnt;
    }

    private List<Edge> getSourceEdges() {
        return layers.stream().flatMap(Collection::stream).flatMap (t -> t.sourceEdges.stream()).collect(Collectors.toList());
    }

    private List<Edge> getTargetEdges() {
        return layers.stream().flatMap(Collection::stream).flatMap (t -> t.targetEdges.stream()).collect(Collectors.toList());
    }

    //--- UtiMethods ---//

    /** Fügt eine Edhe zwischen Source und Target ein, sofern sie noch nicht existiert */
     private GridGraph<T> addEdge(Vertex source , Vertex target) {
        Edge edge = new Edge(source,target);
        if(!source.sourceEdges.contains(edge)) {
            source.sourceEdges.add(edge);
        }
        if(!target.targetEdges.contains(edge)) {
            target.targetEdges.add(edge);
        }
        return this;
    }

    private GridGraph<T> removeEdge(Vertex source , Vertex target) {
        List<Edge> edges2Remove = source.sourceEdges.stream().filter(e -> e.target.equals(target)).collect(Collectors.toList());
        source.sourceEdges.removeAll(edges2Remove);
        target.targetEdges.removeAll(edges2Remove);
        return this;
    }


    private void ensureLayerHasAtLeast(int iLayer, int numRows) {
        List<Tile> layer = layers.get(iLayer) ;
            int rowsNeeded = numRows - layer.size();
            for (int i = rowsNeeded; i > 0; i--) {
                add(iLayer,null);
            }

    }

    private Vertex newVertex(T obj) {
          return new Vertex(vertexSequence.incrementAndGet(), obj);
      }
}
