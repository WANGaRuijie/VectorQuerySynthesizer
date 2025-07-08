package ast.nodes;

import ast.ValueNode;
import ast.Visitor;

public class NullValueNode extends ValueNode {

    /**
     * The single, public, immutable instance of the NullValueNode.
     */
    public static final NullValueNode INSTANCE = new NullValueNode();

    /**
     * Private constructor to prevent external instantiation, enforcing the Singleton pattern.
     */
    private NullValueNode() {
        // Private constructor
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
     * @return The string "NULL".
     */
    @Override
    public String toString() {
        return "NULL";
    }
}
