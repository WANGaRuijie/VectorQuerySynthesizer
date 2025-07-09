package ast.nodes;

import ast.ExpressionNode;
import ast.Visitor;
import ast.enums.BinaryOperator;

import java.util.Objects;

/**
 * Represents a binary arithmetic expression (e.g., a + b, c * d).
 *
 * <p>This node combines two child {@link ExpressionNode}s (a left and a right operand)
 * using a specified {@link BinaryOperator}. It implements {@link ExpressionNode}
 * because it evaluates to a numerical result.
 */
public class BinaryOpExpressionNode implements ExpressionNode {

    private final ExpressionNode left;
    private final BinaryOperator operator;
    private final ExpressionNode right;

    /**
     * Constructs a new BinaryOpExprNode.
     *
     * @param left The left operand. Must not be null.
     * @param operator The binary operator. Must not be null.
     * @param right The right operand. Must not be null.
     */
    public BinaryOpExpressionNode(ExpressionNode left, BinaryOperator operator, ExpressionNode right) {
        this.left = Objects.requireNonNull(left, "Left operand cannot be null.");
        this.operator = Objects.requireNonNull(operator, "Binary operator cannot be null.");
        this.right = Objects.requireNonNull(right, "Right operand cannot be null.");
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public BinaryOperator getOperator() {
        return operator;
    }

    public ExpressionNode getRight() {
        return right;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}
