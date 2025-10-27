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

            if (line.contains("->")) { //check if line contains arrow for node count/splitting function
                String[] parts = line.split("->");
                String src = parts[0].replaceAll("[^a-zA-Z0-9]", "").trim();
                String dst = parts[1].replaceAll("[^a-zA-Z0-9]", "").trim();

                addNode(src); //add node and add edge for feature 2/3 later on.
                addNode(dst);
                addEdge(src, dst); //add edge for later in feature 3 next
            }

            else if (line.matches("[a-zA-Z0-9_]+;")) {
                String node = line.replace(";", "").trim();
                addNode(node);
            }
        }
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

        Path tDot = Files.createTempFile("graph", ".dot"); //temp DOT file for base template
        outputDOTGraph(tDot.toString());

        Graphviz.fromFile(tDot.toFile())
                .render(Format.PNG)
                .toFile(new File(path));
        Files.deleteIfExists(tDot); //graphViz renders png form tempDot
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
