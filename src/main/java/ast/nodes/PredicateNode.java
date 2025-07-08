package ast.nodes;

import ast.ExpressionNode;
import ast.FilterNode;
import ast.Visitor;
import ast.enums.PredicateOperator;

import java.util.Objects;

/**
 * Represents a binary predicate, which is a comparison between two expressions.
 *
 * <p>This node is a fundamental building block of filter conditions (e.g., in a WHERE clause).
 * It compares a left expression with a right expression using a specified operator
 * (e.g., {@code >}, {@code =}, {@code <=}). It implements {@link FilterNode} because
 * it evaluates to a boolean result.
 */
public class PredicateNode implements FilterNode {

    private final ExpressionNode left;
    private final PredicateOperator operator;
    private final ExpressionNode right;

    /**
     * Constructs a new PredicateNode.
     *
     * @param left The expression on the left-hand side of the comparison. Must not be null.
     * @param operator The comparison operator (e.g., EQ, GT). Must not be null.
     * @param right The expression on the right-hand side of the comparison. Must not be null.
     */
    public PredicateNode(ExpressionNode left, PredicateOperator operator, ExpressionNode right) {
        this.left = Objects.requireNonNull(left, "Left expression cannot be null.");
        this.operator = Objects.requireNonNull(operator, "Predicate operator cannot be null.");
        this.right = Objects.requireNonNull(right, "Right expression cannot be null.");
    }

    /**
     * Gets the left-hand side expression.
     * @return The left {@link ExpressionNode}.
     */
    public ExpressionNode getLeft() {
        return left;
    }

    /**
     * Gets the comparison operator.
     * @return The {@link PredicateOperator}.
     */
    public PredicateOperator getOperator() {
        return operator;
    }

    /**
     * Gets the right-hand side expression.
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
     * @return A string in the format "left op right".
     */
    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }
}
