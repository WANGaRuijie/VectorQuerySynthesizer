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
 * the specified expressions (columns), which can have aliases.
 */
public class ProjectionNode implements QueryNode {

    private final QueryNode source;
    private final List<AliasedExpression> selectList;

    /**
     * Constructs a new ProjectionNode.
     *
     * @param source The input query providing the data. Must not be null.
     * @param selectList The list of aliased expressions to be included in the output. Must not be null.
     */
    public ProjectionNode(QueryNode source, List<AliasedExpression> selectList) {
        this.source = Objects.requireNonNull(source, "Source for ProjectionNode cannot be null.");
        this.selectList = Objects.requireNonNull(selectList, "Select list for ProjectionNode cannot be null.");
    }

    public QueryNode getSource() {
        return source;
    }

    // --- MODIFICATION START ---
    // The getter is updated to return the new type.
    public List<AliasedExpression> getSelectList() {
        return selectList;
    }
    // --- MODIFICATION END ---

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    // toString() is also updated for better debugging.
    @Override
    public String toString() {
        String columnStr = selectList.stream()
                .map(ae -> ae.hasAlias() ? ae.expression() + " AS " + ae.alias() : ae.expression().toString())
                .collect(Collectors.joining(", "));
        return "SELECT " + columnStr + " FROM (" + source + ")";
    }
}
