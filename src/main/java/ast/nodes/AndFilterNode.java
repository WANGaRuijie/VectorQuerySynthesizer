package ast.nodes;

import ast.FilterNode;
import ast.Visitor;

import java.util.Objects;

public class AndFilterNode implements FilterNode {

    private final FilterNode left;
    private final FilterNode right;

    /**
     * Constructs a new AndFilterNode.
     *
     * @param left The left-hand side of the AND condition. Must not be null.
     * @param right The right-hand side of the AND condition. Must not be null.
     */
    public AndFilterNode(FilterNode left, FilterNode right) {
        this.left = Objects.requireNonNull(left, "Left child of AND node cannot be null.");
        this.right = Objects.requireNonNull(right, "Right child of AND node cannot be null.");
    }

    /**
     * Gets the left child filter node.
     *
     * @return The left {@link FilterNode}.
     */
    public FilterNode getLeft() {
        return left;
    }

    /**
     * Gets the right child filter node.
     *
     * @return The right {@link FilterNode}.
     */
    public FilterNode getRight() {
        return right;
    }

    /**
     * Accepts a visitor, implementing the double-dispatch mechanism of the Visitor pattern.
     *
     * @param visitor The visitor to accept.
     * @param context The context object to pass to the visitor.
     * @param <R> The return type of the visitor's methods.
     * @param <C> The type of the context object.
     * @return The result of the visitor's processing.
     */
    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    /**
     * Provides a string representation for debugging purposes.
     * @return A string in the format "(left) AND (right)".
     */
    @Override
    public String toString() {
        return "(" + left + ") AND (" + right + ")";
    }
}
