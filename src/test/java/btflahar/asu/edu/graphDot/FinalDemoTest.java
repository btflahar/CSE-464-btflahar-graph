package btflahar.asu.edu.graphDot;
import org.junit.jupiter.api.Test;

public class FinalDemoTest {
    @Test
    public void demoALlFunctions() throws Exception {
        GraphApp app = new GraphApp();

        String inputDotFile = "input.dot";
        app.parseGraph(inputDotFile);
        String start = "a";
        String dest  = "h";

        System.out.println("\nBFS Execution");
        Path bfsPath = app.graphSearch(start, dest, Algorithm.BFS);
        System.out.println("Final BFS path: " + bfsPath);

        System.out.println("\nDFS Execution");
        Path dfsPath = app.graphSearch(start, dest, Algorithm.DFS);
        System.out.println("Final DFS path: " + dfsPath);

        System.out.println("\nRandom Walk Execution");
        int maxSteps = 20;

        for (int i = 1; i <= 5; i++) {
            Path rw = app.randomWalk(start, dest, maxSteps);
            if (rw == null) {
                System.out.println("Random Walk #" + i + ": (max steps)");
            }
            else {
                System.out.println("Random Walk #" + i + ": " + rw);
            }
        }
    }

}
