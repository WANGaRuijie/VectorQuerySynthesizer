package ast.nodes;

import ast.FilterNode;
import ast.Visitor;

import java.util.Objects;

/**
 * Represents a logical NOT operation, which negates a filter condition.
 *
 * <p>This node is a unary filter that wraps a single child {@link FilterNode}.
 * It inverts the boolean result of its child.
 */
public class NotFilterNode implements FilterNode {

    private final FilterNode child;

    /**
     * Constructs a new NotFilterNode.
     *
     * @param child The filter condition to be negated. Must not be null.
     */
    public NotFilterNode(FilterNode child) {
        this.child = Objects.requireNonNull(child, "Child of NOT node cannot be null.");
    }

    /**
     * Gets the child filter node that is being negated.
     *
     * @return The negated {@link FilterNode}.
     */
    public FilterNode getChild() {
        return child;
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
     * @return A string in the format "NOT (child)".
     */
    @Override
    public String toString() {
        return "NOT (" + child + ")";
    }
}
