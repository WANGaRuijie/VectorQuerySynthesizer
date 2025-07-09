package ast.nodes;

import ast.ExpressionNode;
import ast.QueryNode;
import ast.Visitor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a projection operation, which reshapes the output columns.
 *
 * <p>This node corresponds to the {@code SELECT} list in a SQL query. It takes a
 * {@link QueryNode} as its source and produces a new relation containing only
 * the specified expressions (columns).
 */
public class ProjectionNode implements QueryNode {

    private final QueryNode source;
    private final List<ExpressionNode> columns;

    /**
     * Constructs a new ProjectionNode.
     *
     * @param source The input query providing the data. Must not be null.
     * @param columns The list of expressions to be included in the output. Must not be null.
     */
    public ProjectionNode(QueryNode source, List<ExpressionNode> columns) {
        this.source = Objects.requireNonNull(source, "Source for ProjectionNode cannot be null.");
        this.columns = Objects.requireNonNull(columns, "Column list for ProjectionNode cannot be null.");
    }

    public QueryNode getSource() {
        return source;
    }

    public List<ExpressionNode> getColumns() {
        return columns;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        String columnStr = columns.stream().map(Object::toString).collect(Collectors.joining(", "));
        return "SELECT " + columnStr + " FROM (" + source + ")";
    }
}
