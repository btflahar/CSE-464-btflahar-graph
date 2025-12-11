package btflahar.asu.edu.graphDot;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import java.util.ArrayDeque;
import java.util.Deque;

public class DFS extends AbstractGraphSearch{
    private final Deque<String> frontier = new ArrayDeque<>();

    public DFS(Graph<String, DefaultEdge> graph) {
        super(graph);
    }

    @Override
    protected void initializeFrontier(String srcLabel) {
        frontier.clear();
        frontier.push(srcLabel);
    }

    @Override
    protected boolean isFrontierEmpty() {
        return frontier.isEmpty();
    }

    @Override
    protected String removeNextFromFrontier() {
        return frontier.pop(); // LIFO
    }

    @Override
    protected void addToFrontier(String nodeLabel) {
        frontier.push(nodeLabel);
    }
}
