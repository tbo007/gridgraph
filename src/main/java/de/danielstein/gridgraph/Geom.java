package de.danielstein.gridgraph;

public class Geom {

    /**
     * Überprüft, ob sich zwei Liniensegmente schneiden, wobei Schnittpunkte an den Start- oder Endpunkten ignoriert werden.
     *
     * @param x1 x-Koordinate des Startpunkts der ersten Linie
     * @param y1 y-Koordinate des Startpunkts der ersten Linie
     * @param x2 x-Koordinate des Endpunkts der ersten Linie
     * @param y2 y-Koordinate des Endpunkts der ersten Linie
     * @param x3 x-Koordinate des Startpunkts der zweiten Linie
     * @param y3 y-Koordinate des Startpunkts der zweiten Linie
     * @param x4 x-Koordinate des Endpunkts der zweiten Linie
     * @param y4 y-Koordinate des Endpunkts der zweiten Linie
     * @return true, wenn sich die Liniensegmente innerhalb ihrer Längen schneiden und nicht an den Endpunkten, sonst false
     */
    public static boolean lineIntersect(int x1, int y1, int x2, int y2, // line one
                                        int x3, int y3, int x4, int y4) { // line two
        // Berechne den Nenner der Schnittpunktformel
        int denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);

        // Wenn der Nenner 0 ist, sind die Linien parallel und schneiden sich nicht
        if (denominator == 0) {
            return false;
        }

        // Berechne die Zähler für die Parameter ua und ub
        int uaNumerator = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
        int ubNumerator = (x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3);

        // Berechne die Parameter ua und ub als Double-Werte
        double ua = uaNumerator / (double) denominator;
        double ub = ubNumerator / (double) denominator;

        // Überprüfe, ob ua und ub zwischen 0 und 1 liegen, um Schnittpunkte an den Endpunkten auszuschließen
        return (ua > 0 && ua < 1 && ub > 0 && ub < 1);
    }
}