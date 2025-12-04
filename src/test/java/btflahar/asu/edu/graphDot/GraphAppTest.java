package btflahar.asu.edu.graphDot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.util.NoSuchElementException;

class GraphAppTest {

    private Path write(Path dir, String name, String content) throws IOException {
        Path p = dir.resolve(name);
        Files.writeString(p, content);
        return p;
    }

    //Feature 1 ParseGraph Test
    @Test
    void parseGraphLoopOnItselfDot(@TempDir Path tmp) throws IOException {
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
    void addNodeDuplicateTest() {
        GraphApp app = new GraphApp();
        app.addNode("M");
        app.addNode("M");// duplicate test case

        String out = app.toString();

        assertTrue(out.contains("Nodes: 1"), out);
        assertTrue(out.contains("Edges: 0"), out);
    }

    @Test
    void addNodesDuplicatesTestDot() {
        GraphApp app = new GraphApp();
        app.addNodes(new String[]{"P","Q","R","P","Q"});
        String out = app.toString();
        assertTrue(out.contains("Nodes: 3"), out); //Nodes P, Q and R
        assertTrue(out.contains("Edges: 0"), out); // No Edges
    }

    //Feature 3 Add Edge Test Functionality
    @Test
    void addEdgeDuplicateIgnoreTest1() {
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
    void outputDOTGraphCreatesProperTextTest(@TempDir Path tmp) throws IOException {
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
    void outputGraphicsPngTest(@TempDir Path tmp) throws IOException, InterruptedException {

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
    void outputGraphTextTest(@TempDir Path tmp) throws IOException {
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"X","Y","Z"});
        app.addEdge("X","Y");
        app.addEdge("Y","Z");

        Path actual = tmp.resolve("graph-report.txt");
        app.outputGraph(actual.toString());

        String expected = Files.readString(Path.of("expected.txt")); //match expected.txt file
        String got = Files.readString(actual);

        assertEquals(expected.replace("\r\n","\n").trim(),
                got.replace("\r\n","\n").trim());
    }


    @Test
    void someRemovedCorrectly() { //SCENARIO 1
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"A","B","C","D"});
        app.addEdge("A","B");
        app.addEdge("B","C");
        app.addEdge("C","D");


        app.removeNode("A"); //test removeNode
        app.removeEdge("B","C"); //test removeEdge

        String out = app.toString();

        //only c-> should remain and 3 nodes (B, C, D)
        assertTrue(out.contains("Nodes: 3"), out);
        assertTrue(out.contains("Edges: 1"), out);
        assertFalse(out.contains("A -> B"), out);
        assertFalse(out.contains("B -> C"), out);
        assertTrue(out.contains("C -> D"), out);
    }

    @Test
    void scenario2_throwsException() { //SCENARIO 2
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"M","N", "O"});

        assertThrows(NoSuchElementException.class, //test removal of node that doesn't exist
                () -> app.removeNode("A"));

        assertThrows(NoSuchElementException.class,
                () -> app.removeNodes(new String[]{"M","A"})); //test removal of one correct node and one incorrect
    }

    @Test
    void scenario3_throwsException() { //SCENARIO 3
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"X","Y","Z"});
        app.addEdge("X","Y");

        assertThrows(NoSuchElementException.class,
                () -> app.removeEdge("Y","Z")); //remove edge that was never there

        assertThrows(NoSuchElementException.class,
                () -> app.removeEdge("X","Z")); //remove edge with incorrect end node
    }

    @Test
    void graphSearchBfs() {
        GraphApp app = new GraphApp();
        app.addNodes(new String[]{"A","B","C","D"});


        app.addEdge("A","B");
        app.addEdge("B","C"); //add 3 edges for test
        app.addEdge("C","D");

        btflahar.asu.edu.graphDot.Path path = app.graphSearch("A","D"); //path for all edges

        assertNotNull(path, "Path A->D is valid"); //valid test path
        assertEquals(List.of("A","B","C","D"), path.getNodes());
        assertEquals("A -> B -> C -> D", path.toString());
    }

    @Test
    void graphSearchBfsNullNoPath() {
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"A","B","C","D"});
        app.addEdge("A","B");   // no edges to C/D

        btflahar.asu.edu.graphDot.Path path = app.graphSearch("A","D");

        assertNull(path, "Path A->D should be null");
    }

    @Test
    void graphSearchDfsExist() {
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"W","X","Y","Z"});
        app.addEdge("W","X");
        app.addEdge("X","Y");
        app.addEdge("Y","Z");

        btflahar.asu.edu.graphDot.Path path = app.graphSearch("W","Z");

        assertNotNull(path, "Path W->Z is valid");
        assertEquals(List.of("W","X","Y","Z"), path.getNodes());
        assertEquals("W -> X -> Y -> Z", path.toString());
    }

    @Test
    void graphSearchDfsNullPath() {
        GraphApp app = new GraphApp();

        app.addNodes(new String[]{"W","X","Y","Z"});
        app.addEdge("W","X"); // No edges for Z

        btflahar.asu.edu.graphDot.Path path = app.graphSearch("W","Z");

        assertNull(path, "Path W->Z should be null");
    }
}