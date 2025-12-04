package btflahar.asu.edu.graphDot;

import org.jgrapht.graph.DefaultDirectedGraph;
import guru.nidi.graphviz.engine.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;

public class GraphApp {
    private Graph<String, DefaultEdge> graph;

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
            str1.append(graph.getEdgeSource(e)).append(" -> ").append(graph.getEdgeTarget(e)).append("\n");
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
            addNode(label); //reuse addNode
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
            throw new NoSuchElementException("No node named - " + label); //in case of non-exist
        }
        graph.removeVertex(label);
    }

    public void removeNodes(String[] labels) {
        for (String s : labels) {
            if (!graph.containsVertex(s)) {
                throw new NoSuchElementException("No node named - " + s); //in case of non-exist
            }
        }
        for (String s : labels) {
            graph.removeVertex(s);
        }
    }

    public void removeEdge(String srcLabel, String dstLabel) {
        if (!graph.containsVertex(srcLabel) || !graph.containsVertex(dstLabel)) {
            throw new NoSuchElementException("Endpoint node missing for edge: " + srcLabel + " -> " + dstLabel);
        }
        DefaultEdge n = graph.getEdge(srcLabel, dstLabel);

        if (n == null) {
            throw new NoSuchElementException("No edge with -  " + srcLabel + " -> " + dstLabel);
        }
        graph.removeEdge(n);
    }

    public Path graphSearch(String nodeSrc, String nodeDst) {
        return bfsSearch(nodeSrc, nodeDst);
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

        java.nio.file.Path tDot = Files.createTempFile("graph", ".dot"); //temp DOT file for base template
        outputDOTGraph(tDot.toString());

        Graphviz.fromFile(tDot.toFile())
                .render(Format.PNG)
                .toFile(new File(path));
        Files.deleteIfExists(tDot); //graphViz renders png form tempDot
    }

    public Path graphSearch(String srcLabel, String dstLabel, Algorithm equate) {
        if (equate == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }
        switch (equate) {
            case BFS:
                return bfsSearch(srcLabel, dstLabel);
            case DFS:
                return dfsSearch(srcLabel, dstLabel);
            default:
                throw new IllegalArgumentException("error - " + equate);
        }
    }

    // --- BFS core (from bfs branch) ---
    private Path bfsSearch(String srcLabel, String dstLabel) {
        if (!graph.containsVertex(srcLabel) || !graph.containsVertex(dstLabel)) {
            return null;
        }

        if (srcLabel.equals(dstLabel)) {
            return new Path(java.util.List.of(srcLabel));
        }

        java.util.Queue<String> queue = new java.util.ArrayDeque<>();
        java.util.Map<String, String> parent = new java.util.HashMap<>();
        java.util.Set<String> visited = new java.util.HashSet<>();

        queue.add(srcLabel);
        visited.add(srcLabel);

        boolean found = false;

        while (!queue.isEmpty()) {
            String current = queue.remove();

            if (current.equals(dstLabel)) {
                found = true;
                break;
            }

            for (DefaultEdge e : graph.outgoingEdgesOf(current)) {
                String neighbor = graph.getEdgeTarget(e);
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (!found) {
            return null;
        }

        java.util.List<String> nodes = new java.util.ArrayList<>();
        String step = dstLabel;
        nodes.add(step);
        while (!step.equals(srcLabel)) {
            step = parent.get(step);
            if (step == null) {
                return null;
            }
            nodes.add(step);
        }
        java.util.Collections.reverse(nodes);

        return new Path(nodes);
    }

    private Path dfsSearch(String srcLabel, String dstLabel) {
        if (!graph.containsVertex(srcLabel) || !graph.containsVertex(dstLabel)) {
            return null;
        }

        if (srcLabel.equals(dstLabel)) {
            return new Path(java.util.List.of(srcLabel));
        }

        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.Map<String, String> parent = new java.util.HashMap<>();

        boolean found = dfsVisit(srcLabel, dstLabel, visited, parent);

        if (!found) {
            return null;
        }

        java.util.List<String> nodes = new java.util.ArrayList<>();
        String step = dstLabel;
        nodes.add(step);
        while (!step.equals(srcLabel)) {
            step = parent.get(step);
            if (step == null) {
                return null;
            }
            nodes.add(step);
        }

        java.util.Collections.reverse(nodes);
        return new Path(nodes);
    }

    private boolean dfsVisit(String current,
                             String dstLabel,
                             java.util.Set<String> visited,
                             java.util.Map<String, String> parent) {
        visited.add(current);
        if (current.equals(dstLabel)) {
            return true;
        }

        for (DefaultEdge e : graph.outgoingEdgesOf(current)) {
            String neighbor = graph.getEdgeTarget(e);
            if (!visited.contains(neighbor)) {
                parent.put(neighbor, current);
                if (dfsVisit(neighbor, dstLabel, visited, parent)) {
                    return true;
                }
            }
        }

        return false;
    }


    public static void main(String[] args) throws IOException {
        GraphApp app = new GraphApp();

        String inputDotF  = (args.length >= 1) ? args[0] : "input.dot";
        String outputPngF = (args.length >= 2) ? args[1] : "input.png";

        app.parseGraph(inputDotF);
        System.out.println(app.toString()); //.toString print in terminal
        app.outputGraph("graph-report.txt"); //output to graph file
        app.outputDOTGraph("input.dot");
        app.outputGraphics(outputPngF, "png");

        System.out.println("dot file path -> " + Paths.get("input.dot").toAbsolutePath());
        System.out.println("png file path -> " + Paths.get(outputPngF).toAbsolutePath());
    }
}
