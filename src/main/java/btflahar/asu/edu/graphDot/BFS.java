package btflahar.asu.edu.graphDot;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import java.util.ArrayDeque;
import java.util.Queue;

public class BFS extends AbstractGraphSearch{
    private final Queue<String> frontier = new ArrayDeque<>();

    public BFS(Graph<String, DefaultEdge> graph) {
        super(graph);
    }

    @Override
    protected void initializeFrontier(String srcLabel) {
        frontier.clear();
        frontier.add(srcLabel);
    }

    @Override
    protected boolean isFrontierEmpty() {
        return frontier.isEmpty();
    }

    @Override
    protected String removeNextFromFrontier() {
        return frontier.remove(); // FIFO
    }

    @Override
    protected void addToFrontier(String nodeLabel) {
        frontier.add(nodeLabel);
    }
}
