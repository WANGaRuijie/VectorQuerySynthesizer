package ast.nodes;

import ast.QueryNode;
import ast.Visitor;

import java.util.List;
import java.util.Objects;

/**
 * Represents a rename operation on a relation (a subquery).
 *
 * <p>This node corresponds to the {@code AS} keyword for subqueries, allowing
 * a new name to be assigned to the relation and optionally to its columns.
 */
public class RenameNode implements QueryNode {

    private final QueryNode source;
    private final String newName;
    private final List<String> columnAliases; // Optional, can be empty or null

    public RenameNode(QueryNode source, String newName, List<String> columnAliases) {
        this.source = Objects.requireNonNull(source, "Source for RenameNode cannot be null.");
        this.newName = Objects.requireNonNull(newName, "New name for RenameNode cannot be null.");
        this.columnAliases = columnAliases; // Can be null
    }

    public RenameNode(QueryNode source, String newName) {
        this(source, newName, null);
    }

    public QueryNode getSource() { return source; }
    public String getNewName() { return newName; }
    public List<String> getColumnAliases() { return columnAliases; }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        String aliasStr = "";
        if (columnAliases != null && !columnAliases.isEmpty()) {
            aliasStr = "(" + String.join(", ", columnAliases) + ")";
        }
        return "(" + source + ") AS " + newName + aliasStr;
    }
}
