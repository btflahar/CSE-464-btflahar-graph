package btflahar.asu.edu.graphDot;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class RandomWalkSearch extends AbstractGraphSearch {

    private final Deque<String> frontier = new ArrayDeque<>();
    private final Random random = new Random();
    private final int maxSteps;

    public RandomWalkSearch(Graph<String, DefaultEdge> graph, int maxSteps) {
        super(graph);
        this.maxSteps = maxSteps;
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
        return frontier.pop();
    }

    @Override
    protected void addToFrontier(String nodeLabel) {
        frontier.push(nodeLabel);
    }

    @Override
    protected Iterable<String> expandNeighbors(String current) {
        List<String> neighbors = new ArrayList<>();

        for (DefaultEdge e : graph.outgoingEdgesOf(current)) {
            neighbors.add(graph.getEdgeTarget(e));
        }

        if (neighbors.isEmpty()) {
            return Collections.emptyList();
        }

        String chosen = neighbors.get(random.nextInt(neighbors.size()));
        return List.of(chosen);
    }
}
