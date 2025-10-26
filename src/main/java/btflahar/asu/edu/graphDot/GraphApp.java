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

                addNode(src); //add node and add edge for feature 2/3
                addNode(dst);
                addEdge(src, dst);
            }

            else if (line.matches("[a-zA-Z0-9_]+;")) {
                String node = line.replace(";", "").trim();
                addNode(node);
            }
        }
    }
    //TASK 2 - Add node to graph
    public void addNode(String label) {
        if (!graph.containsVertex(label)) {
            graph.addVertex(label);
        }

        else { //check for duplicate labels:
            System.out.println("Duplicate: " + label); //duplicate text for test
        }
    }

    //TASK 2 -add a list of nodes, just reuse add node
    public void addNodes(String[] labels) {
        for (String label : labels) {
            addNode(label);
        }
    }
    //TASK 3 -  Add edge and check for duplicate edges
    public void addEdge(String srcLabel, String dstLabel) {
        if (graph.containsEdge(srcLabel, dstLabel)) {
            System.out.println("Duplicate: " + srcLabel + " -> " + dstLabel); //duplicate text for test
        }
        else {
            graph.addEdge(srcLabel, dstLabel);
        }
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
            throw new IllegalArgumentException("error, invalid format for graph");
        }

        Path tempDot = Files.createTempFile("graph", ".dot");
        outputDOTGraph(tempDot.toString());


        Graphviz.fromFile(tempDot.toFile()) //use graphViz function to create from png
                .render(Format.PNG)
                .toFile(new File(path));
        Files.deleteIfExists(tempDot);
    }


    @Override
    public String toString() {
        StringBuilder str1 = new StringBuilder();

        str1.append("Nodes = ").append(graph.vertexSet().size()).append("\n");
        str1.append("Edges = ").append(graph.edgeSet().size()).append("\n");

        for (DefaultEdge e : graph.edgeSet()) {
            str1.append(graph.getEdgeSource(e)).append(" -> ").append(graph.getEdgeTarget(e)).append("\n");
        }

        return str1.toString();
    }


    public void outGraph(String filepath) throws IOException {
        Files.writeString(Paths.get(filepath), toString());
    }


    public static void main(String[] args) throws IOException {
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"A", "B", "C"});
        app.addEdge("A", "B"); //test graph for test cases
        app.addEdge("B", "C");

        String outDot = "C:/Users/brady/Desktop/out.dot";

        String outPng = "C:/Users/brady/Desktop/input.png";

        app.outputDOTGraph(outDot);
        app.outputGraphics(outPng, "png");

        System.out.println("DOT graph created and written to: " + outDot + " exists at" + Files.exists(Paths.get(outDot)));
        System.out.println("PNG  written to: " + outPng + " exists at" + Files.exists(Paths.get(outPng)));
        System.out.println(app);

    }
}
