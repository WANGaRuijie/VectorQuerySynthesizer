package ast.nodes;

import ast.QueryNode;
import ast.Visitor;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a query with a Common Table Expression (CTE) clause.
 *
 * <p>This node is a new top-level query node that contains one or more named
 * subqueries (CTEs) and a final query body that can reference them.
 */
public class WithNode implements QueryNode {

    private final Map<String, QueryNode> ctes; // Maps CTE name to its query AST
    private final QueryNode body;

    /**
     * Constructs a WithNode.
     *
     * @param ctes A map where keys are CTE names and values are their corresponding QueryNodes. Must not be null.
     * @param body The main query that follows the WITH clause. Must not be null.
     */
    public WithNode(Map<String, QueryNode> ctes, QueryNode body) {
        this.ctes = Objects.requireNonNull(ctes, "CTE map cannot be null.");
        this.body = Objects.requireNonNull(body, "Query body cannot be null.");
    }

    /**
     * Gets the map of Common Table Expressions.
     * @return A map where keys are CTE names (String) and values are their query definitions (QueryNode).
     */
    public Map<String, QueryNode> getCtes() {
        return ctes;
    }

    /**
     * Gets the main query body that follows the WITH clause.
     * @return The main QueryNode.
     */
    public QueryNode getBody() {
        return body;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
