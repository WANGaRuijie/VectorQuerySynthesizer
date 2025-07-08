package ast.nodes;

import ast.ExpressionNode;
import ast.FilterNode;
import ast.Visitor;

import java.util.Objects;

/**
 * Represents a predicate that checks if an expression is NULL or IS NOT NULL.
 *
 * <p>This node implements {@link FilterNode} as it evaluates to a boolean result.
 * It contains a single child expression to be checked for nullity.
 */
public class IsNullPredicateNode implements FilterNode {

    private final ExpressionNode expression;
    private final boolean isNull; // true for "IS NULL", false for "IS NOT NULL"

    /**
     * Constructs a new IsNullPredicateNode.
     *
     * @param expression The expression to check for nullity. Must not be null.
     * @param isNull Set to true to check for IS NULL, false to check for IS NOT NULL.
     */
    public IsNullPredicateNode(ExpressionNode expression, boolean isNull) {
        this.expression = Objects.requireNonNull(expression, "Expression for IS NULL check cannot be null.");
        this.isNull = isNull;
    }

    /**
     * Gets the expression being checked for nullity.
     * @return The child {@link ExpressionNode}.
     */
    public ExpressionNode getExpression() {
        return expression;
    }

    /**
     * Checks if the predicate is for 'IS NULL' or 'IS NOT NULL'.
     * @return true if the check is 'IS NULL', false if it is 'IS NOT NULL'.
     */
    public boolean isNull() {
        return isNull;
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
     * @return A string in the format "expr IS NULL" or "expr IS NOT NULL".
     */
    @Override
    public String toString() {
        return expression + (isNull ? " IS NULL" : " IS NOT NULL");
    }
}
