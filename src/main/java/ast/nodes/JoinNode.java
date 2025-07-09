package ast.nodes;

import ast.FilterNode;
import ast.QueryNode;
import ast.Visitor;

import java.util.Objects;

/**
 * Represents a join operation between two relations based on a condition.
 *
 * <p>This node corresponds to an {@code INNER JOIN} in SQL. It combines rows from a
 * left and right {@link QueryNode} source where the join {@code condition} is true.
 */
public class JoinNode implements QueryNode {

    private final QueryNode left;
    private final QueryNode right;
    private final FilterNode condition;

    /**
     * Constructs a new JoinNode.
     *
     * @param left The left-side query for the join. Must not be null.
     * @param right The right-side query for the join. Must not be null.
     * @param condition The join condition (the ON clause). Must not be null.
     */
    public JoinNode(QueryNode left, QueryNode right, FilterNode condition) {
        this.left = Objects.requireNonNull(left, "Left source for JoinNode cannot be null.");
        this.right = Objects.requireNonNull(right, "Right source for JoinNode cannot be null.");
        this.condition = Objects.requireNonNull(condition, "Join condition cannot be null.");
    }

    public QueryNode getLeft() { return left; }
    public QueryNode getRight() { return right; }
    public FilterNode getCondition() { return condition; }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "(" + left + ") JOIN (" + right + ") ON (" + condition + ")";
    }
}
