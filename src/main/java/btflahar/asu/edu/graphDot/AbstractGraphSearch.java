package btflahar.asu.edu.graphDot;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import java.util.*;

public abstract class AbstractGraphSearch implements SearchStrategy {
    protected final Graph<String, DefaultEdge> graph;
    protected AbstractGraphSearch(Graph<String, DefaultEdge> graph) {
        this.graph = graph;
    }

    @Override
    public final Path search(String srcLabel, String dstLabel) {
        if (!graph.containsVertex(srcLabel) || !graph.containsVertex(dstLabel)) {
            return null;
        }

        if (srcLabel.equals(dstLabel)) {
            return new Path(List.of(srcLabel));
        }

        Set<String> visited = new HashSet<>();
        Map<String, String> parent = new HashMap<>();

        initializeFrontier(srcLabel);
        visited.add(srcLabel);

        while (!isFrontierEmpty()) {
            String curString = removeNextFromFrontier();
            Path visitingPath = buildPathTo(srcLabel, curString, parent);
            System.out.println("visiting " + visitingPath);

            if (curString.equals(dstLabel)) {
                return buildPath(srcLabel, dstLabel, parent);
            }

            for (String node1 : expandNeighbors(curString)) {
                if (!visited.contains(node1)) {
                    visited.add(node1);
                    parent.put(node1, curString);
                    addToFrontier(node1);
                }
            }
        }

        return null;
    }

    protected abstract void initializeFrontier(String srcLabel);
    protected abstract boolean isFrontierEmpty();
    protected abstract String removeNextFromFrontier();
    protected abstract void addToFrontier(String nodeLabel);

    protected Iterable<String> expandNeighbors(String current) {
        List<String> neighbors = new ArrayList<>();
        for (DefaultEdge e : graph.outgoingEdgesOf(current)) {
            neighbors.add(graph.getEdgeTarget(e));
        }
        return neighbors;
    }

    //helper Buildpath function
    protected Path buildPathTo(String srcLabel, String current, Map<String, String> parent) {
        List<String> nodes = new ArrayList<>();
        String step = current;
        nodes.add(step);

        while (!step.equals(srcLabel)) {
            step = parent.get(step);

            if (step == null) {
                break;
            }
            nodes.add(step);
        }

        Collections.reverse(nodes);
        return new Path(nodes);
    }

    protected Path buildPath(String srcLabel, String dstLabel, Map<String, String> parent) {
        List<String> nodes = new ArrayList<>();
        String step = dstLabel;
        nodes.add(step);

        while (!step.equals(srcLabel)) {
            step = parent.get(step);

            if (step == null) {
                return null;
            }
            nodes.add(step);
        }

        Collections.reverse(nodes);
        return new Path(nodes);
    }
}
