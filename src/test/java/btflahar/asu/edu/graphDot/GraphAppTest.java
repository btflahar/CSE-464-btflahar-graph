package btflahar.asu.edu.graphDot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphAppTest {

    private Path write(Path dir, String name, String content) throws IOException {
        Path p = dir.resolve(name);
        Files.writeString(p, content);
        return p;
    }

    //Feature 1 ParseGraph Test
    @Test
    void parseGraph_LoopOnItselfDot(@TempDir Path tmp) throws IOException {
        String dot = """ 
                digraph G {
                  L -> M;
                  M -> N;
                  N -> L;
                  A;
                }
                """;
        Path f = write(tmp, "tri.dot", dot); //parseGraph example DOT

        GraphApp app = new GraphApp();
        app.parseGraph(f.toString());

        String out = app.toString();

        assertTrue(out.contains("Nodes: 4"), out);
        assertTrue(out.contains("Edges: 3"), out);
        assertTrue(out.contains("L -> M"), out);
        assertTrue(out.contains("M -> N"), out);
        assertTrue(out.contains("N -> L"), out);
    }

    @Test
    void addNode_duplicateTest() {
        GraphApp app = new GraphApp();
        app.addNode("M");
        app.addNode("M");// duplicate test case

        String out = app.toString();

        assertTrue(out.contains("Nodes: 1"), out);
        assertTrue(out.contains("Edges: 0"), out);
    }

    @Test
    void addNodes_duplicatesTestDot() {
        GraphApp app = new GraphApp();
        app.addNodes(new String[]{"P","Q","R","P","Q"});
        String out = app.toString();
        assertTrue(out.contains("Nodes: 3"), out); //Nodes P, Q and R
        assertTrue(out.contains("Edges: 0"), out); // No Edges
    }

    //Feature 3 Add Edge Test Functionality
    @Test
    void addEdge_duplicateIgnoreTest1() {
        GraphApp app = new GraphApp();
        app.addNodes(new String[]{"L","M","N"});

        app.addEdge("L","M");
        app.addEdge("L","M"); // test case duplicate
        app.addEdge("L","N"); // different after duplicate test case

        String out = app.toString();

        assertTrue(out.contains("Edges: 2"), out);
        assertTrue(out.contains("L -> M"), out);
        assertTrue(out.contains("L -> N"), out);
    }

    //Feature 4 outputTest
    @Test
    void outputDOTGraph_createsProperTextTest(@TempDir Path tmp) throws IOException {
        GraphApp app = new GraphApp();
        app.addNodes(new String[]{"A1","B2","C3","D4"});
        app.addEdge("A1","B2");
        app.addEdge("B2","C3");

        Path out = tmp.resolve("graph.dot");
        app.outputDOTGraph(out.toString());

        String text = Files.readString(out);

        assertTrue(text.startsWith("digraph G {"));
        assertTrue(text.contains("A1;"));
        assertTrue(text.contains("B2;"));
        assertTrue(text.contains("C3;"));
        assertTrue(text.contains("D4;"));
        assertTrue(text.contains("A1 -> B2;"));
        assertTrue(text.contains("B2 -> C3;"));
        assertTrue(text.endsWith("}\n"));
    }

    //Feature 4 Unit Test, Test outputGraphics and outputGraph validity
    @Test
    void outputGraphics_pngTest(@TempDir Path tmp) throws IOException, InterruptedException {

        boolean dotPresent = false;

        try {
            Process funP = new ProcessBuilder("dot", "-V").redirectErrorStream(true).start();
            funP.waitFor();
            dotPresent = (funP.exitValue() == 0) || (funP.exitValue() == 1);
        }
        catch (Exception ignore) {}
        Assumptions.assumeTrue(dotPresent, "Exception occured, graphviz error in path");

        GraphApp app = new GraphApp();
        app.addNodes(new String[]{"A","B","C"});
        app.addEdge("A","B");
        app.addEdge("B","C");
        Path png = tmp.resolve("chain.png");

        app.outputGraphics(png.toString(), "png");

        assertTrue(Files.size(png) > 0, "PNG blank");
        assertTrue(Files.exists(png), "PNG does not exist");
    }


    @Test
    void outputGraph_textTest(@TempDir Path tmp) throws IOException {
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"X","Y","Z"});
        app.addEdge("X","Y");
        app.addEdge("Y","Z");

        Path actual = tmp.resolve("graph-report.txt");
        app.outputGraph(actual.toString());

        String expected = Files.readString(Path.of("expected.txt.")); //match expected.txt file
        String got = Files.readString(actual);

        assertEquals(expected.replace("\r\n","\n").trim(),
                got.replace("\r\n","\n").trim());
    }
}