package ast.nodes;

import ast.ExpressionNode;
import ast.Visitor;

import java.util.Objects;

public class ColumnReferenceNode implements ExpressionNode {

    private final String columnName;

    /**
     * Constructs a new ColumnRefNode.
     *
     * @param columnName The name of the column being referenced. Must not be null.
     */
    public ColumnReferenceNode(String columnName) {
        this.columnName = Objects.requireNonNull(columnName, "Column name cannot be null.");
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
     * @return A string in the format "Column(columnName)".
     */
    @Override
    public String toString() {
        return "Column(" + columnName + ")";
    }

}
