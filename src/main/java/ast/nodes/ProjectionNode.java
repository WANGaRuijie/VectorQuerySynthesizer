package ast.nodes;

import ast.ExpressionNode;
import ast.QueryNode;
import ast.Visitor;

import java.util.List;
import java.util.Objects;

public class ProjectionNode implements QueryNode {

    private final QueryNode source;
    private final List<ExpressionNode> columns;

    public ProjectionNode(QueryNode source, List<ExpressionNode> columns) {
        this.source = Objects.requireNonNull(source);
        this.columns = Objects.requireNonNull(columns);
    }

    @Override
    public <R,C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "Projection[" + columns +"](" + source + ")";
    }
}
