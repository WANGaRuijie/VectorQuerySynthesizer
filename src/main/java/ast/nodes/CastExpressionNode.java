package ast.nodes;

import ast.ExpressionNode;
import ast.Visitor;
import ast.enums.DataType;

import java.util.Objects;

/**
 * Represents a type casting operation (e.g., CAST(expression AS type)).
 *
 * <p>This node wraps an existing {@link ExpressionNode} and specifies a target
 * {@link DataType} to convert it to. It implements {@link ExpressionNode} as it
 * produces a value of the new type.
 */
public class CastExpressionNode implements ExpressionNode {

    private final ExpressionNode expression;
    private final DataType targetType;

    /**
     * Constructs a new CastExprNode.
     *
     * @param expression The expression whose value will be cast. Must not be null.
     * @param targetType The data type to cast to. Must not be null.
     */
    public CastExpressionNode(ExpressionNode expression, DataType targetType) {
        this.expression = Objects.requireNonNull(expression, "Expression for CAST cannot be null.");
        this.targetType = Objects.requireNonNull(targetType, "Target type for CAST cannot be null.");
    }

    public ExpressionNode getExpression() {
        return expression;
    }

    public DataType getTargetType() {
        return targetType;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "CAST(" + expression + " AS " + targetType + ")";
    }
}
