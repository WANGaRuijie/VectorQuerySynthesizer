package ast.nodes;

import ast.ExpressionNode;
import ast.Visitor;

import java.util.List;
import java.util.Objects;

/**
 * Represents a generic function call expression (e.g., func(arg1, arg2)).
 *
 * <p>This node is an {@link ExpressionNode} that captures the function's name
 * and its list of arguments, which are themselves expressions.
 */
public class FunctionCallNode implements ExpressionNode {

    private final String functionName;
    private final List<ExpressionNode> arguments;

    /**
     * Constructs a FunctionCallNode.
     *
     * @param functionName The name of the function being called. Must not be null.
     * @param arguments    A list of expressions serving as arguments. Must not be null.
     */
    public FunctionCallNode(String functionName, List<ExpressionNode> arguments) {
        this.functionName = Objects.requireNonNull(functionName, "Function name cannot be null.");
        this.arguments = Objects.requireNonNull(arguments, "Argument list cannot be null.");
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}