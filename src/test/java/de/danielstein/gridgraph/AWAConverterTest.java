package de.danielstein.gridgraph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class AWAConverterTest {

    @Test
    public void testRead () throws IOException {
        Path path = Path.of("src/test/resources/syt007.json");
        AWAConverter converter = new AWAConverter(path);
        GridGraph<?> gridGraph = converter.convert();
        System.out.println(gridGraph);
    }
}
