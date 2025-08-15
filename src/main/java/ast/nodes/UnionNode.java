package ast.nodes;

import ast.LimitableQuery;
import ast.OrderableQuery;
import ast.QueryNode;
import ast.Visitor;
import java.util.Objects;

/**
 * Represents the set union of two relations.
 *
 * <p>This node combines the result sets of two child {@link QueryNode}s.
 */
public class UnionNode implements OrderableQuery, LimitableQuery {

    private final QueryNode left;
    private final QueryNode right;

    public UnionNode(QueryNode left, QueryNode right) {
        this.left = Objects.requireNonNull(left, "Left source for UnionNode cannot be null.");
        this.right = Objects.requireNonNull(right, "Right source for UnionNode cannot be null.");
    }

    public QueryNode getLeft() { return left; }
    public QueryNode getRight() { return right; }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "(" + left + ") UNION (" + right + ")";
    }
}
