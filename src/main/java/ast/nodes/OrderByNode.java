package ast.nodes;

import ast.LimitableQuery;
import ast.OrderableQuery;
import ast.QueryNode;
import ast.Visitor;
import ast.enums.SortOrder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a sort operation on a relation based on a single column.
 *
 * This node corresponds to a simplified ORDER BY clause like "ORDER BY column_name ASC/DESC".
 * It takes an OrderableQuery as its source.
 */
public class OrderByNode implements LimitableQuery {

    private final OrderableQuery source;
    private final ColumnReferenceNode sortColumn;
    private final SortOrder sortOrder;

    /**
     * Constructs a new OrderByNode.
     * @param source The input query that provides the data to be sorted. Must not be null.
     * @param sortColumn The column to sort by. Must not be null.
     * @param sortOrder The sort order (ASC or DESC). Must not be null.
     */

    /**
     * Onlu for order by column name
     */
    public OrderByNode(OrderableQuery source, ColumnReferenceNode sortColumn, SortOrder sortOrder) {
        this.source = Objects.requireNonNull(source, "Source for OrderByNode cannot be null.");
        this.sortColumn = Objects.requireNonNull(sortColumn, "Sort column for OrderByNode cannot be null.");
        this.sortOrder = Objects.requireNonNull(sortOrder, "Sort order for OrderByNode cannot be null.");
    }

    public OrderableQuery getSource() {
        return source;
    }

    public ColumnReferenceNode getSortColumn() {
        return sortColumn;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "(" + source + ") ORDER BY " + sortColumn.getColumnName() + " " + sortOrder;
    }
}