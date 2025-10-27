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
        else {
            System.out.println("duplicate-" + label); //check for duplicates in nodes
        }
    }

    public void addNodes(String[] labels) {
        for (String label : labels) {
            addNode(label); //reuse addNode
        }
    }

    public void addEdge(String srcLabel, String dstLabel) {
        if (graph.containsEdge(srcLabel, dstLabel)) {
            System.out.println("duplicate-" + srcLabel + " -> " + dstLabel); //duplicate check
        }
        else {
            graph.addEdge(srcLabel, dstLabel);
        }
    }



    public static void main(String[] args) throws IOException {
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"A", "B", "C"}); //example DOT graph to be tested using main
        app.addEdge("A", "B");
        app.addEdge("B", "C"); //add edges for test
        System.out.println(app);
    }
}
