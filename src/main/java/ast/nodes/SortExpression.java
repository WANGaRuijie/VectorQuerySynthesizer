package ast.nodes;

import ast.ASTNode;
import ast.ExpressionNode;
import ast.Visitor;
import ast.enums.SortOrder;

import java.util.Objects;

/**
 * Represents a single expression within an ORDER BY clause, including the sort direction.
 *
 * <p>This is a helper node used by {@link OrderByNode}. It is not a query, expression,
 * or filter itself, but it is part of the AST and must be visitable.
 */
public class SortExpression implements ASTNode {

    private final ExpressionNode expression;
    private final SortOrder order;

    /**
     * Constructs a new SortExpression.
     *
     * @param expression The expression to sort by (e.g., a column reference). Must not be null.
     * @param order The direction of the sort (ASC or DESC). Must not be null.
     */
    public SortExpression(ExpressionNode expression, SortOrder order) {
        this.expression = Objects.requireNonNull(expression, "Sort expression cannot be null.");
        this.order = Objects.requireNonNull(order, "Sort order cannot be null.");
    }

    /**
     * Convenience constructor that defaults to ascending order.
     * @param expression The expression to sort by.
     */
    public SortExpression(ExpressionNode expression) {
        this(expression, SortOrder.ASC);
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    public SortOrder getOrder() {
        return order;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return expression + " " + order;
    }
}
