package de.danielstein.gridgraph;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class AWAConverter {

    Path  jsonFile;

    public AWAConverter(Path jsonFile) {
        this.jsonFile = jsonFile;
    }

    public GridGraph<?> convert() throws IOException {
        try (InputStream inputStream = Files.newInputStream(jsonFile)) {

            Object doc = Configuration.defaultConfiguration().jsonProvider().parse(inputStream, "UTF-8");
            List<Map<String, Integer>> list = JsonPath.read(doc, "$.data.jobp.line_conditions[*]");
            GridGraph<Integer> graph = new GridGraph<>();
            for (Map<String, Integer> lc : list) {
                Integer target = lc.get("workflow_line_number");
                Integer source = lc.get("predecessor_line_number");
                graph.addEdge(source, target);
            }
            return graph.prepare();
        }
    }
}
