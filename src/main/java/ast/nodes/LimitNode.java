package ast.nodes;

import ast.QueryNode;
import ast.Visitor;

import java.util.Objects;

/**
 * Represents an operation that limits the number of rows returned from a query.
 *
 * <p>This node corresponds to the {@code LIMIT} clause in a SQL query.
 */
public class LimitNode implements QueryNode {

    private final QueryNode source;
    private final int value;

    public LimitNode(QueryNode source, int value) {
        this.source = Objects.requireNonNull(source, "Source for LimitNode cannot be null.");
        if (value < 0) {
            throw new IllegalArgumentException("LIMIT value must be non-negative.");
        }
        this.value = value;
    }

    public QueryNode getSource() { return source; }
    public int getValue() { return value; }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "(" + source + ") LIMIT " + value;
    }
}
