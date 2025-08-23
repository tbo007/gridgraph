package de.danielstein.gridgraph;

import org.junit.jupiter.api.Test;
import  static de.danielstein.gridgraph.Symbol.*;

import static org.junit.jupiter.api.Assertions.*;

public class BoxDrawingTest extends GridGraphTest{

    @Test
    public void testDrawJPL() {
        GridGraph<String> gridGraph = generateJPL();
        draw(gridGraph);
    }
    @Test
    public void testSymbolMerge() {
        assertBothWays(T_RIGHT,CORNER_BOTTOM_RIGHT,CORNER_TOP_RIGHT);
        assertBothWays(T_LEFT,CORNER_BOTTOM_LEFT,CORNER_TOP_LEFT);
    }

    private void assertBothWays(Symbol merged, Symbol one, Symbol two) {
        assertEquals(merged, merge(one,two));
        assertEquals(merged, merge(two,one));
    }

    private  void draw(GridGraph<?> gridGraph) {
        gridGraph = gridGraph.prepare();
        BoxDrawing drawing = new BoxDrawing(gridGraph);
        System.out.println(drawing.draw());
    }

}