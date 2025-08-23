package de.danielstein.gridgraph;

public enum Symbol {
    SPACE(' ', "Leerzeichen"),
    HORIZONTAL('─', "Horizontale Linie (leicht)"),
    VERTICAL('│', "Vertikale Linie (leicht)"),
    CORNER_TOP_LEFT('┌', "Ecke oben links"),
    CORNER_TOP_RIGHT('┐', "Ecke oben rechts"),
    CORNER_BOTTOM_LEFT('└', "Ecke unten links"),
    CORNER_BOTTOM_RIGHT('┘', "Ecke unten rechts"),
    T_LEFT('├', "T-Verbindung links"),
    T_RIGHT('┤', "T-Verbindung rechts"),
    T_TOP('┬', "T-Verbindung oben"),
    T_BOTTOM('┴', "T-Verbindung unten"),
    CROSS('┼', "Kreuzung (alle Richtungen)"),
    ARROW_RIGHT('→', "Pfeil nach rechts");


    private final char character;
    private final String description;

    Symbol(char character, String description) {
        this.character = character;
        this.description = description;
    }

    public char getCharacter() {
        return character;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return character + " - " + description;
    }

    /**
     * Logische Ersetzung zweier Symbole.
     */
    public static Symbol merge(Symbol existing, Symbol incoming) {

        if (existing == SPACE) return incoming;
        if (incoming == SPACE) return existing;

        // Linienkombinationen
        if ((existing == HORIZONTAL && incoming == VERTICAL) ||
                (existing == VERTICAL && incoming == HORIZONTAL)) {
            return CROSS;
        }

        // Ecken + Linien → T-Verbindungen
        if ((isTopCorner(existing) && incoming == HORIZONTAL) ||
                (isTopCorner(incoming) && existing == HORIZONTAL)) {
            return T_TOP;
        }

        if ((isBottomCorner(existing) && incoming == HORIZONTAL) ||
                (isBottomCorner(incoming) && existing == HORIZONTAL)) {
            return T_BOTTOM;
        }

        if ((isLeftCorner(existing) && incoming == VERTICAL) ||
                (isLeftCorner(incoming) && existing == VERTICAL)) {
            return T_LEFT;
        }

        if ((isRightCorner(existing) && incoming == VERTICAL) ||
                (isRightCorner(incoming) && existing == VERTICAL)) {
            return T_RIGHT;
        }

        if (existing == CORNER_BOTTOM_RIGHT && incoming == CORNER_TOP_RIGHT ||
            incoming  == CORNER_BOTTOM_RIGHT && existing == CORNER_TOP_RIGHT  ) {
            return T_RIGHT;

        } if (existing == CORNER_BOTTOM_LEFT && incoming == CORNER_TOP_LEFT ||
            incoming  == CORNER_BOTTOM_LEFT && existing == CORNER_TOP_LEFT ) {
            return T_LEFT;
        }


        if ((isTOnSide(existing) && incoming == HORIZONTAL) ||
                (isTOnSide(incoming) && existing == HORIZONTAL)) {
            return CROSS;
        }
        if ((isTBottomOrTop(existing) && incoming == VERTICAL) ||
                (isTBottomOrTop(incoming) && existing == VERTICAL)) {
            return CROSS;
        }
//        if (isTBottomOrTop(existing) &&
//                (isTopCorner(incoming) || isBottomCorner(incoming))) {
//            return existing;
//
//        }
//        if (isTOnSide(existing) &&
//                (isLeftCorner(incoming) || isRightCorner(incoming))) {
//            return existing;
//        }

       // TonSide && Vertical == existing

        return existing;
    }

    private static boolean isTopCorner(Symbol s) {
        return s == CORNER_TOP_LEFT || s == CORNER_TOP_RIGHT;
    }

    private static boolean isBottomCorner(Symbol s) {
        return s == CORNER_BOTTOM_LEFT || s == CORNER_BOTTOM_RIGHT;
    }

    private static boolean isLeftCorner(Symbol s) {
        return s == CORNER_TOP_LEFT || s == CORNER_BOTTOM_LEFT;
    }
    private static boolean isTOnSide(Symbol s) { return s == T_LEFT || s == T_RIGHT;}
    private static boolean isTBottomOrTop(Symbol s) { return s == T_BOTTOM || s == T_TOP;}

    private static boolean isRightCorner(Symbol s) {
        return s == CORNER_TOP_RIGHT || s == CORNER_BOTTOM_RIGHT;
    }
}
