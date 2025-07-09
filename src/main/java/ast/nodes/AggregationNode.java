package ast.nodes;

import ast.ExpressionNode;
import ast.FilterNode;
import ast.QueryNode;
import ast.Visitor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

/**
        * Represents an aggregation operation in the AST, corresponding to a GROUP BY clause.
        *
        * <p>This node takes an input relation (source), partitions it into groups based on
        * the {@code groupingKeys}, and then computes the {@code aggregateExpressions} for each group.
        * An optional {@code having} filter can be applied to the groups afterward.
        */
public class AggregationNode implements QueryNode {

    private final QueryNode source;
    private final List<ExpressionNode> groupingKeys;
    private final List<AggregateExpressionNode> aggregateExpressions;
    private final FilterNode having; // Optional, can be null for queries without a HAVING clause

    /**
     * Constructs a new AggregationNode.
     *
     * @param source The input query node that provides the data to be aggregated. Must not be null.
     * @param groupingKeys A list of expressions to group the rows by. Must not be null. An empty list signifies aggregating all rows into a single group.
     * @param aggregateExpressions A list of aggregate functions to compute for each group. Must not be null.
     * @param having An optional filter to apply after grouping (the HAVING clause). Can be null.
     */
    public AggregationNode(
            QueryNode source,
            List<ExpressionNode> groupingKeys,
            List<AggregateExpressionNode> aggregateExpressions,
            FilterNode having) {

        this.source = Objects.requireNonNull(source, "Source cannot be null.");
        this.groupingKeys = Objects.requireNonNull(groupingKeys, "Grouping keys cannot be null.");
        this.aggregateExpressions = Objects.requireNonNull(aggregateExpressions, "Aggregate expressions cannot be null.");
        this.having = having; // `having` is optional and permitted to be null.
    }

    /**
     * Gets the source query node.
     *
     * @return The child {@link QueryNode} that provides the data for aggregation.
     */
    public QueryNode getSource() {
        return source;
    }

    /**
     * Gets the list of expressions used for grouping.
     *
     * @return A list of {@link ExpressionNode}s for the GROUP BY clause.
     */
    public List<ExpressionNode> getGroupingKeys() {
        return groupingKeys;
    }

    /**
     * Gets the list of aggregate functions to be computed.
     *
     * @return A list of {@link AggregateExpressionNode}s.
     */
    public List<AggregateExpressionNode> getAggregateExpressions() {
        return aggregateExpressions;
    }

    /**
     * Gets the optional HAVING clause filter.
     *
     * @return The {@link FilterNode} for the HAVING clause, or null if it doesn't exist.
     */
    public FilterNode getHaving() {
        return having;
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
     * @return A string summarizing the aggregation node.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GROUP BY ");
        String keys = groupingKeys.stream().map(Object::toString).collect(Collectors.joining(", "));
        sb.append(keys.isEmpty() ? "(all rows)" : keys);

        if (having != null) {
            sb.append(" HAVING (").append(having).append(")");
        }

        sb.append(" FROM (").append(source).append(")");
        return sb.toString();
    }
}
