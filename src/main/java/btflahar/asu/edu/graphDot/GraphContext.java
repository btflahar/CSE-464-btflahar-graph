package btflahar.asu.edu.graphDot;

public class GraphContext {
    private SearchStrategy strategy;

    public GraphContext(SearchStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(SearchStrategy strategy) {
        this.strategy = strategy;
    }

    public Path executeSearch(String srcLabel, String dstLabel) {
        if (strategy == null) {
            throw new IllegalStateException("Search strategy not set");
        }
        return strategy.search(srcLabel, dstLabel);
    }
}
