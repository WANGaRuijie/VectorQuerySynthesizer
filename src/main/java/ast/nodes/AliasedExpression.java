package ast.nodes;

import ast.ExpressionNode;

import java.util.Objects;

/**
 * A helper class that holds an expression and an optional alias for it.
 * Used in the select list of a ProjectionNode.
 * This is a data holder and not a visitable AST node itself.
 *
 * @param expression The expression to be projected.
 * @param alias The optional alias for the expression (e.g., "AS my_column"). Can be null.
 */
public record AliasedExpression(ExpressionNode expression, String alias) {
    public AliasedExpression {
        Objects.requireNonNull(expression, "Expression cannot be null.");
    }

    /**
     * Convenience constructor for expressions without an alias.
     */
    public AliasedExpression(ExpressionNode expression) {
        this(expression, null);
    }

    public boolean hasAlias() {
        return alias != null && !alias.isEmpty();
    }
}
