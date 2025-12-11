package btflahar.asu.edu.graphDot;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.engine.Format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class GraphApp {
    private final Graph<String, DefaultEdge> graph;

    public GraphApp() {
        graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        Graphviz.useEngine(new GraphvizCmdLineEngine());
    }

    //TASK 1 - parseGraph
    public void parseGraph(String filepath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filepath));

        for (String line : lines) {
            line = line.trim();

            if (line.contains("->")) {
                parseEdgeHelper(line);
            } else if (line.matches("[a-zA-Z0-9_]+;")) {
                parseNodeHelper(line);
            }
        }
    }

    private void parseEdgeHelper(String line) {
        String[] parts = line.split("->");
        String src = parts[0].replaceAll("[^a-zA-Z0-9]", "").trim();
        String dst = parts[1].replaceAll("[^a-zA-Z0-9]", "").trim();

        addNode(src);
        addNode(dst);
        addEdge(src, dst);
    }

    private void parseNodeHelper(String line) {
        String node = line.replace(";", "").trim();
        addNode(node);
    }

    @Override
    public String toString() {
        StringBuilder str1 = new StringBuilder();

        str1.append("Nodes: ").append(graph.vertexSet().size()).append("\n");
        str1.append("Edges: ").append(graph.edgeSet().size()).append("\n");

        for (DefaultEdge e : graph.edgeSet()) {
            str1.append(graph.getEdgeSource(e))
                    .append(" -> ")
                    .append(graph.getEdgeTarget(e))
                    .append("\n");
        }
        return str1.toString();
    }

    public void outputGraph(String filepath) throws IOException {
        Files.writeString(Paths.get(filepath), toString());
    }

    public void addNode(String label) {
        if (!graph.containsVertex(label)) {
            graph.addVertex(label);
        }
        // else: duplicate
    }

    public void addNodes(String[] labels) {
        for (String label : labels) {
            addNode(label); // reuse addNode
        }
    }

    public void addEdge(String srcLabel, String dstLabel) {
        if (!graph.containsEdge(srcLabel, dstLabel)) {
            graph.addEdge(srcLabel, dstLabel);
        }
        // else: duplicate
    }

    public void removeNode(String label) {
        if (!graph.containsVertex(label)) {
            throw new NoSuchElementException("No node named - " + label);
        }
        graph.removeVertex(label);
    }

    public void removeNodes(String[] labels) {
        for (String s : labels) {
            if (!graph.containsVertex(s)) {
                throw new NoSuchElementException("No node named - " + s);
            }
        }
        for (String s : labels) {
            graph.removeVertex(s);
        }
    }

    public void removeEdge(String srcLabel, String dstLabel) {
        if (!graph.containsVertex(srcLabel) || !graph.containsVertex(dstLabel)) {
            throw new NoSuchElementException(
                    "Endpoint node missing for edge: " + srcLabel + " -> " + dstLabel);
        }
        DefaultEdge n = graph.getEdge(srcLabel, dstLabel);

        if (n == null) {
            throw new NoSuchElementException("No edge with -  " + srcLabel + " -> " + dstLabel);
        }
        graph.removeEdge(n);
    }

    public Path graphSearch(String nodeSrc, String nodeDst) {
        SearchStrategy strategy = new BFS(graph);
        return strategy.search(nodeSrc, nodeDst);
    }

    public Path graphSearch(String srcLabel, String dstLabel, Algorithm algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }

        SearchStrategy strategy;
        switch (algorithm) {
            case BFS -> strategy = new BFS(graph);
            case DFS -> strategy = new DFS(graph);
            case RANDOM_WALK -> strategy = new RandomWalkSearch(graph, 50);
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }

        return strategy.search(srcLabel, dstLabel);
    }


    public void outputDOTGraph(String path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path))) {
            writer.write("digraph G {\n");
            for (String v : graph.vertexSet()) {
                writer.write("  " + v + ";\n");
            }
            for (DefaultEdge e : graph.edgeSet()) {
                String src = graph.getEdgeSource(e);
                String dst = graph.getEdgeTarget(e);
                writer.write("  " + src + " -> " + dst + ";\n");
            }
            writer.write("}\n");
        }
    }

    public void outputGraphics(String path, String format) throws IOException {

        if (!format.equalsIgnoreCase("png")) {
            throw new IllegalArgumentException("PNG only");
        }

        java.nio.file.Path tDot = Files.createTempFile("graph", ".dot"); // temp DOT file
        outputDOTGraph(tDot.toString());

        Graphviz.fromFile(tDot.toFile())
                .render(Format.PNG)
                .toFile(new File(path));

        Files.deleteIfExists(tDot); // cleanup temp file
    }

    public static void main(String[] args) throws IOException {
        GraphApp app = new GraphApp();

        String inputDotF  = (args.length >= 1) ? args[0] : "input.dot";
        String outputPngF = (args.length >= 2) ? args[1] : "input.png";

        String start = (args.length >= 3) ? args[2] : "a";
        String dest  = (args.length >= 4) ? args[3] : "h";

        app.parseGraph(inputDotF);

        System.out.println("=== BFS Demo (Scheme A) ===");
        Path bfsPath = app.graphSearch(start, dest);
        System.out.println("Final BFS path: " + bfsPath);

        System.out.println("\n=== DFS Demo (Scheme A) ===");
        Path dfsPath = app.graphSearch(start, dest, Algorithm.DFS);
        System.out.println("Final DFS path: " + dfsPath);

        System.out.println("\n=== Random Walk Demo ===");
        for (int i = 1; i <= 5; i++) {
            Path rw = app.graphSearch(start, dest, Algorithm.RANDOM_WALK);
            if (rw == null) {
                System.out.println("Attempt " + i + ": (dead end)");
            } else {
                System.out.println("Attempt " + i + ": " + rw);
            }
        }


        System.out.println(app.toString());

        try {
            app.outputGraph("graph-report.txt");
            app.outputDOTGraph("output.dot");
            app.outputGraphics(outputPngF, "png");
        }

        catch (Exception ignored) {
        }
    }
}