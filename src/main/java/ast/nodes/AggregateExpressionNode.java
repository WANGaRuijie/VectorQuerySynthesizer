package ast.nodes;

import ast.enums.AggregateFunction;
import ast.ExpressionNode;
import ast.Visitor;

import java.util.Objects;

public class AggregateExpressionNode implements ExpressionNode {

    private final AggregateFunction function;
    private final ExpressionNode argument;
    private final boolean distinct;

    public AggregateExpressionNode(AggregateFunction function, ExpressionNode argument, boolean distinct) {
        this.function = Objects.requireNonNull(function, "Aggregate function cannot be null");

        // COUNT(*) is the only aggregate function that can have a null argument
        if (argument == null && function != AggregateFunction.COUNT) {
            throw new IllegalArgumentException("Argument can only be null for COUNT(*)");
        }

        this.argument = argument;
        this.distinct = distinct;
    }

    /**
     * Creates an aggregate expression node for the specified aggregate function.
     * @param function The aggregate function to apply (e.g., SUM, COUNT).
     * @param argument The expression to aggregate (e.g., a column reference).
     */
    public AggregateExpressionNode(AggregateFunction function, ExpressionNode argument) {
        this(function, argument, false);
    }

    /**
     * Creates an aggregate expression node for COUNT.
     * @return An AggregateExpressionNode representing COUNT(*).
     */
    public static AggregateExpressionNode createCountStar() {
        return new AggregateExpressionNode(AggregateFunction.COUNT, null, false);
    }

    /**
     * Returns the argument for the aggregate function.
     * @return The argument, or null if the function is COUNT(*) which has no argument.
     */
    public ExpressionNode getArgument() {
        return argument;
    }

    /**
     * Returns whether the aggregate function is distinct.
     * @return True if the aggregate function is distinct, false otherwise.
     */
    public boolean isDistinct() {
        return distinct;
    }

    public AggregateFunction getFunction() {
        return function;
    }

    /**
     * Accepts a visitor
     * @param visitor The visitor instance that will process this node.
     * @param context The context in which the visitor operates.
     * @return the result returned by the visitor's visit method.
     */
    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

}