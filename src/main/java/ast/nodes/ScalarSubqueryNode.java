package ast.nodes;

import ast.ExpressionNode;
import ast.QueryNode;
import ast.Visitor;

import java.util.Objects;

/**
 * Represents a scalar subquery, which is a query used as a single value.
 *
 * <p>This node is an {@link ExpressionNode} that wraps a {@link QueryNode}.
 * During validation or execution, the wrapped query must be guaranteed to
 * return exactly one row and one column.
 */
public class ScalarSubqueryNode implements ExpressionNode {

    private final QueryNode query;

    /**
     * Constructs a ScalarSubqueryNode.
     *
     * @param query The query that will produce the scalar value. Must not be null.
     */
    public ScalarSubqueryNode(QueryNode query) {
        this.query = Objects.requireNonNull(query, "Query for scalar subquery cannot be null.");
    }

    public QueryNode getQuery() {
        return query;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
