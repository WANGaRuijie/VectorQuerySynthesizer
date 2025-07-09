package ast.nodes;

import ast.FilterNode;
import ast.QueryNode;
import ast.Visitor;

import java.util.Objects;

/**
 * Represents a selection operation, which filters rows from a data source.
 *
 * <p>This node corresponds to the {@code WHERE} clause in a SQL query. It takes a
 * {@link QueryNode} as its source and applies a {@link FilterNode} to it.
 */
public class SelectNode implements QueryNode {

    private final QueryNode source;
    private final FilterNode filter;

    /**
     * Constructs a new SelectNode.
     *
     * @param source The input query providing the data. Must not be null.
     * @param filter The filter condition to apply. Must not be null.
     */
    public SelectNode(QueryNode source, FilterNode filter) {
        this.source = Objects.requireNonNull(source, "Source for SelectNode cannot be null.");
        this.filter = Objects.requireNonNull(filter, "Filter for SelectNode cannot be null.");
    }

    public QueryNode getSource() {
        return source;
    }

    public FilterNode getFilter() {
        return filter;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "(" + source + ") WHERE (" + filter + ")";
    }
}
