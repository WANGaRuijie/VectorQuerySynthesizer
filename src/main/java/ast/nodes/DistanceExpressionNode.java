package ast.nodes;

import ast.ExpressionNode;
import ast.Visitor;
import ast.enums.DistanceOperator;

import java.util.Objects;

/**
 * Represents a vector distance calculation between two vector expressions.
 *
 * <p>This node implements {@link ExpressionNode} because it evaluates to a numerical
 * value (the distance or similarity). It is typically used in ORDER BY clauses for
 * vector similarity search or in WHERE clauses to filter by distance.
 */
public class DistanceExpressionNode implements ExpressionNode {

    private final ExpressionNode left;
    private final DistanceOperator operator;
    private final ExpressionNode right;

    /**
     * Constructs a new DistanceExprNode.
     *
     * @param left     The left vector expression. Must not be null.
     * @param operator The distance operator to use. Must not be null.
     * @param right    The right vector expression. Must not be null.
     */
    public DistanceExpressionNode(ExpressionNode left, DistanceOperator operator, ExpressionNode right) {
        this.left = Objects.requireNonNull(left, "Left vector expression cannot be null.");
        this.operator = Objects.requireNonNull(operator, "Distance operator cannot be null.");
        this.right = Objects.requireNonNull(right, "Right vector expression cannot be null.");
    }

    /**
     * Gets the left-hand side vector expression.
     *
     * @return The left {@link ExpressionNode}.
     */
    public ExpressionNode getLeft() {
        return left;
    }

    /**
     * Gets the distance operator.
     *
     * @return The {@link DistanceOperator}.
     */
    public DistanceOperator getOperator() {
        return operator;
    }

    /**
     * Gets the right-hand side vector expression.
     *
     * @return The right {@link ExpressionNode}.
     */
    public ExpressionNode getRight() {
        return right;
    }

    /**
     * Accepts a visitor, implementing the double-dispatch mechanism of the Visitor pattern.
     *
     * @param visitor The visitor to accept.
     * @param context The context object to pass to the visitor.
     * @param <R>     The return type of the visitor's methods.
     * @param <C>     The type of the context object.
     * @return The result of the visitor's processing.
     */
    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    /**
     * Provides a string representation for debugging purposes.
     *
     * @return A string in the format "left op right".
     */
    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }
}