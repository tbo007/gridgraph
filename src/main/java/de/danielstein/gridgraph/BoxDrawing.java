package de.danielstein.gridgraph;

import java.util.ArrayList;
import java.util.List;

import static de.danielstein.gridgraph.Symbol.*;

public class BoxDrawing {

    private class EdgeDraw {

        public Symbol left = SPACE;
        public Symbol middle = SPACE;
        public Symbol right = SPACE;

        @Override
        public String toString() {
            char [] array = new char[] {left.getCharacter(), middle.getCharacter(),right.getCharacter()};
            return  String.valueOf(array);
        }
    }

    private class Layer extends ArrayList<Tile> {

        public List<EdgeDraw> outgoingConnections;
        private int layerNum;

        public Layer(int layerNum,int layerSize) {
            super(layerSize);
            this.layerNum = layerNum;
            this.outgoingConnections = new ArrayList<>(layerSize);
            for (int i = 0; i < layerSize; i++) {
                outgoingConnections.add(new EdgeDraw());
            }
        }
        public  void mergeSymbols(int row, Symbol left, Symbol middle, Symbol right) {
            EdgeDraw edgeDraw = outgoingConnections.get(row);
            edgeDraw.left = Symbol.merge(edgeDraw.left,left);
            edgeDraw.middle = Symbol.merge(edgeDraw.middle,middle);
            edgeDraw.right = Symbol.merge(edgeDraw.right,right);
        }
    }

    private  GridGraph<?> graph;
    private List<Layer> layers;
    private int longestDomainObj = 0;

    public BoxDrawing(GridGraph<?> graph) {
        this.graph = graph;
        layers = new ArrayList<>(graph.layers.size());
    }

    public String draw() {
        adoptNodes();
        adoptConnections();
        StringBuilder b = new StringBuilder(1000);
        int rowCount = layers.get(0).size();

        for (int iR = 0; iR < rowCount; iR++) {
            for (int il = 0; il < layers.size(); il++) {
                Layer layer = layers.get(il);
                b.append(tile2String(layer.get(iR)));
                b.append(layer.outgoingConnections.get(iR).toString());
            }
            b.append("\n");
        }
        return b.toString();

    }

    private String tile2String( Tile tile) {
        String retVal = null;
        if (tile.isSpacer()) {
            retVal = " ".repeat(longestDomainObj+2);
        }
        if (tile.isFake()) {
            retVal= String.valueOf(HORIZONTAL.getCharacter()).repeat(longestDomainObj+2);
        }
        if (tile.isDomainObject()) {
            Vertex vertex = (Vertex) tile;
            retVal = vertex.getDomainObj().toString();
            int num2Pad = longestDomainObj - retVal.length();
            if (num2Pad > 0) {
                retVal = retVal + " ".repeat(num2Pad);
            }
            retVal = "[" + retVal+ "]";
        }
        return  retVal;
    }

    private void adoptConnections() {
        for (Edge sourceEdge : graph.getSourceEdges()) {
            Vertex source = sourceEdge.getSource();
            Vertex target = sourceEdge.getTarget();
            Layer sourceLayer = layers.get(source.getLayer());
            adoptConnection(sourceLayer, source,target);
        }
    }

    /**
     * s = 3 , t = 0
     * s = 0 , t = 3
     * Vertical Lines needed = 1,2
     *
     *
     * s = 7 , t = 10
     * s = 10, t = 7
     * * Vertical Lines needed = 8,9
     *
     *
     * @param sourceLayer
     * @param source
     * @param target
     */
    private void adoptConnection(Layer sourceLayer, Vertex source, Vertex target) {
        int sourceRow = source.getRow();
        int targetRow = target.getRow();
        int rowDiff = sourceRow - targetRow;
        // source and target on same row
        if (rowDiff == 0) {
            sourceLayer.mergeSymbols(sourceRow,HORIZONTAL,HORIZONTAL,HORIZONTAL);
            return;
         // target above source
        } else if (rowDiff > 0) {
            sourceLayer.mergeSymbols(sourceRow,HORIZONTAL,CORNER_BOTTOM_RIGHT,SPACE);
            sourceLayer.mergeSymbols(targetRow,SPACE,CORNER_TOP_LEFT,HORIZONTAL);
        // target below source
        }else { // rowDiff <0{
            sourceLayer.mergeSymbols(sourceRow,HORIZONTAL,CORNER_TOP_RIGHT,SPACE);
            sourceLayer.mergeSymbols(targetRow,SPACE,CORNER_BOTTOM_LEFT,HORIZONTAL);
        }
        // VerticalLines
        int from = Math.min(sourceRow,targetRow)+1;
        int to = Math.max(sourceRow,targetRow)-1;
        for (int i = from; i <=to ; i++) {
            sourceLayer.mergeSymbols(i,SPACE,VERTICAL,SPACE);
        }
    }

    private void adoptNodes() {
        for (int i = 0; i < graph.layers.size(); i++) {
            List<Tile> glayer = graph.layers.get(i);
            Layer layer = new Layer(i,glayer.size());
            layers.add(layer);
            for (Tile tile : glayer) {
                layer.add(tile);
                if (tile.isDomainObject()) {
                    Object d = ((Vertex) tile).getDomainObj();
                    longestDomainObj = Math.max(longestDomainObj,d.toString().length());
                }
            }
        }
    }

    public static void main(String[] args) {
//        for (Symbol symbol : Symbol.values()) {
//            System.out.println(symbol);
//        }
        System.out.println(Symbol.CORNER_TOP_LEFT.toString() + Symbol.ARROW_RIGHT.toString());
        System.out.println(Symbol.VERTICAL);
        System.out.println("\n");
        System.out.println(Symbol.HORIZONTAL.toString() + Symbol.CROSS + Symbol.ARROW_RIGHT.toString());
        System.out.println(SPACE.toString() +  Symbol.VERTICAL.toString() );


        System.out.println("SPACE + HORIZONTAL → " + Symbol.merge(SPACE, Symbol.HORIZONTAL));
        System.out.println("HORIZONTAL + VERTICAL → " + Symbol.merge(Symbol.HORIZONTAL, Symbol.VERTICAL));
        System.out.println("CORNER_TOP_LEFT + HORIZONTAL → " + Symbol.merge(Symbol.CORNER_TOP_LEFT, Symbol.HORIZONTAL));
        System.out.println("CORNER_BOTTOM_RIGHT + VERTICAL → " + Symbol.merge(Symbol.CORNER_BOTTOM_RIGHT, Symbol.VERTICAL));
    }
}

