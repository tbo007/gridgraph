package de.danielstein.gridgraph;

import java.io.StringWriter;
import java.util.List;

public class GridPrinter {

    private final GridGraph<?> graph;

    public GridPrinter(GridGraph<?> graph) {
        this.graph = graph;
    }


    // Methode, um die maximale Länge der Elemente zu bestimmen
    private int getMaxElementLength() {
        int maxLength = 0;
        for (List<Tile> layer : graph.layers) {
            for (Tile tile : layer) {
                int length = tile.toString().length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
        }
        return maxLength;
    }

    public String getGridAsString() {

        StringBuilder sb = new StringBuilder();
        //sb.append("Fitness: " +graph.getCrossingEdges().size());
        sb.append("\n\n");
        // Bestimme die maximale Länge der Elemente
        int maxLength = getMaxElementLength();

        // Anzahl der maximalen Zeilen ermitteln
        int numberOfRows = graph.layers.stream().map(List::size).max(Integer::compare).get();

        // Spaltennummern hinzufügen
        sb.append("   "); // Platz für Zeilennummern lassen
        for (int col = 0; col < graph.layers.size(); col++) {
            sb.append(String.format("%" + maxLength + "s ", "S" + col));
        }
        sb.append("\n");

        // Durch jede Zeile iterieren
        for (int row = 0; row < numberOfRows; row++) {
            // Zeilennummer hinzufügen
            sb.append(String.format("Z%d ", row));

            // Durch jede Spalte iterieren
            for (List<Tile> column : graph.layers) {
                // Element der aktuellen Zeile und Spalte hinzufügen
                Tile tile = column.size() > row ? column.get(row) : null;

                sb.append(String.format("%" + maxLength + "s ", tile.toString()));
            }
            // Nach jeder Zeile einen Zeilenumbruch hinzufügen
            sb.append("\n");
        }

        return sb.toString();
    }
}
