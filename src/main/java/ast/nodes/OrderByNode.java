package ast.nodes;

import ast.QueryNode;
import ast.Visitor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a sort operation on a relation.
 *
 * <p>This node corresponds to the {@code ORDER BY} clause in a SQL query. It takes a
 * {@link QueryNode} as its source and sorts the resulting rows based on a list of
 * {@link SortExpression}s.
 */
public class OrderByNode implements QueryNode {

    private final QueryNode source;
    private final List<SortExpression> sortExpressions;

    public OrderByNode(QueryNode source, List<SortExpression> sortExpressions) {
        this.source = Objects.requireNonNull(source, "Source for OrderByNode cannot be null.");
        this.sortExpressions = Objects.requireNonNull(sortExpressions, "Sort expressions cannot be null.");
        if (sortExpressions.isEmpty()) {
            throw new IllegalArgumentException("Sort expression list cannot be empty.");
        }
    }

    public QueryNode getSource() { return source; }
    public List<SortExpression> getSortExpressions() { return sortExpressions; }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        String orderStr = sortExpressions.stream().map(Object::toString).collect(Collectors.joining(", "));
        return "(" + source + ") ORDER BY " + orderStr;
    }
}