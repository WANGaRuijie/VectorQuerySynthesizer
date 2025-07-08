package ast.nodes;

import ast.ValueNode;
import ast.Visitor;

import java.util.Objects;

public class ConstantValueNode extends ValueNode {

    private final Object value;

    /**
     * Constructs a new ConstantValueNode.
     *
     * @param value The literal value to be held by this node. Must not be null.
     *              For a NULL literal, use {@link NullValueNode} instead.
     */
    public ConstantValueNode(Object value) {
        this.value = Objects.requireNonNull(value, "Value for ConstantValueNode cannot be null. Use NullValueNode for SQL NULL.");
    }

    /**
     * Gets the constant value.
     *
     * @return The stored literal value as an Object.
     */
    public Object getValue() {
        return value;
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
     * @return A string in the format "Constant(value)".
     */
    @Override
    public String toString() {
        if (value instanceof String) {
            return "Constant('" + value + "')";
        }
        return "Constant(" + value + ")";
    }
}
