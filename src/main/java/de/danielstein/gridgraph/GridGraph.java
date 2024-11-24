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
     * 3. @see {@link #stretchOut()}
     * @return this
     */
    public GridGraph<T> prepare() {
        return layering().addFakeVertexes().stretchOut();
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

    /** Wenn z.B. eine Verbindung von eine, Knoten von layer 1 auf einen Knoten auf layer 4 zeigt, dann auf
     * Layer 2,3 einen Fake anlegen und Verbindungen entsprechend umlegen
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
                    // Alte Verbindung entfernen und FakeKnoten einfügen
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
        mergeFakes2Connection();

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
     * 1. Grid in richtiger Größe erzeugen
     * 2. Alle Domain Objekte inklusiver eingehender Verbindung übernehmen. Und zwar so, dass die
     * Verbindungen auf einer Zeile liegen
     * 3. Zeilen die nur Spacer haben aus den Layern löschen.
     * @return
     */
    public GridGraph<T> stretchOut() {
        List<List<Tile>> orig = layers;
        layers = new ArrayList<>();
        IntStream.range(0,orig.size()).forEach(this::ensureLayerPresent);
        int numRows = orig.get(0).size();
        IntStream.range(0, orig.size()).forEach(i -> ensureLayerHasAtLeast(i,numRows));
        for (Map.Entry<T, Vertex> entry: domainObj2Vertex.entrySet()) {
            int i = 0;
            while (!add2Row(i,entry.getValue())) {
                i++;
            }
        }

       int rowCount = layers.get(0).size();
        IntStream.range(0,rowCount).filter(i -> getRow(i).stream().allMatch(Tile::isSpacer)).boxed().sorted(Comparator.reverseOrder()).forEach(i -> {
            for ( List<Tile> layer: layers) {
                layer.remove(i.intValue()); // Achtung remove(Object)...
            }
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


    private boolean add2Row(int row, Vertex v) {
        boolean addOk = false;
        // 1. Check
        Tile tile = getTile(v.getLayer(), row);
        if (tile.isSpacer()) {
            addOk = true;
        }
        // 2. Recursive, wenn !addOk abbruch
        Optional<Vertex> incomingFake = v.targetEdges.stream().map(Edge::getSource).filter(Vertex::isFake).findAny();
        if(addOk && incomingFake.isPresent()) {
            addOk &= add2Row(row, incomingFake.get());
            if (!addOk) {
                return addOk;
            }
        }
         // 3. set
        if(addOk) {
            set(v.getLayer(), row, v);
        }
        return addOk;
    }


    private void mergeFakes2Connection() {
        List<Vertex> vertexWIthFakesPointingTo = getSourceEdges().stream().filter(e -> e.source.isFake() && !e.target.isFake()).map(e -> e.target).distinct().collect(Collectors.toList());
        vertexWIthFakesPointingTo.forEach(this::mergeFakes);
    }

    private void mergeFakes(Vertex start) {

        List<Vertex> incomingFakes = start.incomingEdgesFrom().stream().filter(Vertex::isFake).collect(Collectors.toList());
        if(incomingFakes.size() <2) {
            return;
        }
        ListIterator<Vertex> listIter = incomingFakes.listIterator();
        Vertex mergeInto = listIter.next();
        while(listIter.hasNext()) {
            Vertex toMerge = listIter.next();
            toMerge.outgoingEdgesTo().forEach(o2V -> {
                removeEdge(toMerge,o2V);
                addEdge(mergeInto,o2V);
            });
            toMerge.incomingEdgesFrom().forEach(iV -> {
                removeEdge(iV,toMerge);
                addEdge(iV,mergeInto);
            });

            List<Tile> rows = layers.get(toMerge.getLayer());
            set(toMerge.getLayer(), toMerge.getRow(), null);
        }
        mergeFakes(mergeInto);
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
