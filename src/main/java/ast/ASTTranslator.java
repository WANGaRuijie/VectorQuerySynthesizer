package ast;

import ast.enums.*;
import ast.nodes.*;
import model.Vector;
import java.util.stream.Collectors;

/**
 * A Visitor that translates an AST into a valid, executable SQL string.
 * This version correctly assembles queries with chained operations like ORDER BY and LIMIT.
 */
public class ASTTranslator implements Visitor<String, Void> {

    /**
     * Public entry point for translation.
     * This method starts the visitor traversal from the root node.
     *
     * @param root The root QueryNode (the complete query AST).
     * @return The resulting executable SQL string.
     */
    public String translate(QueryNode root) {
        // We use 'root.accept(this, null)' to start the traversal.
        // The context (Void) is not used in this translator.
        return root.accept(this, null);
    }

    @Override
    public String visit(ProjectionNode node, Void context) {
        // This is the top-level query constructor.
        // It's responsible for the "SELECT ... FROM ..." part.

        String selectListStr = node.getSelectList().stream()
                .map(this::translateAliasedExpression)
                .collect(Collectors.joining(", "));

        // Recursively visit the source to get the "FROM ... WHERE ... ORDER BY ... LIMIT ..." part
        String fromAndRestOfQuery = node.getSource().accept(this, null);

        return "SELECT " + selectListStr + " FROM " + fromAndRestOfQuery;
    }

    @Override
    public String visit(LimitNode node, Void context) {
        String sourceSql = node.getSource().accept(this, null);
        return sourceSql + " LIMIT " + node.getValue();
    }

    @Override
    public String visit(RenameNode node, Void context) {
        // For subqueries, we need to apply an alias.
        String sourceSql = node.getSource().accept(this, null);
        // Wrap the source in parentheses to apply the alias.
        return "(" + sourceSql + ") AS " + node.getNewName();
    }

    @Override
    public String visit(OrderByNode node, Void context) {
        // An OrderByNode appends "ORDER BY ..." to its source's SQL.
        String sourceSql = node.getSource().accept(this, null);
        ColumnReferenceNode sortColumn = node.getSortColumn();
        return sourceSql + " ORDER BY " + sortColumn.getColumnName() + " " + node.getSortOrder().toString();
    }

    @Override
    public String visit(TableNode node, Void context) {
        // The base case for the recursion: a table name.
        return node.getTableName();
    }

    private String translateAliasedExpression(AliasedExpression aliasedExpr) {
        String exprSql = aliasedExpr.expression().accept(this, null);
        if (aliasedExpr.hasAlias()) {
            return exprSql + " AS " + aliasedExpr.alias();
        }
        return exprSql;
    }

    @Override
    public String visit(DistanceExpressionNode node, Void context) {
        String left = node.getLeft().accept(this, null);
        String right = node.getRight().accept(this, null);
        return left + " " + node.getOperator().toString() + " " + right;
    }

    @Override
    public String visit(ColumnReferenceNode node, Void context) {
        return node.getColumnName();
    }

    @Override
    public String visit(ConstantValueNode node, Void context) {
        Object value = node.getValue();
        if (value == null) return "NULL";
        if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        }
        if (value instanceof Vector) {
            return "'" + ((Vector) value).toSqlString() + "'";
        }
        return value.toString();
    }

    @Override
    public String visit(NullValueNode node, Void context) {
        return null;
    }

    @Override
    public String visit(SortExpression node, Void context) {
        String expr = node.getExpression().accept(this, null);
        if (node.getOrder() != null && node.getOrder() == SortOrder.DESC) {
            return expr + " DESC";
        }
        // ASC is the default in SQL, so we can often omit it for brevity.
        // We will include it to be explicit.
        return expr + " ASC";
    }

    private String unsupported(ASTNode node) {
        throw new UnsupportedOperationException("ASTTranslator does not yet support visiting " + node.getClass().getSimpleName());
    }

    @Override public String visit(WithNode node, Void c) { return unsupported(node); }
    @Override public String visit(FunctionCallNode node, Void c) { return unsupported(node); }

    @Override
    public String visit(ScalarSubqueryNode node, Void context) {
        // A scalar subquery must be enclosed in parentheses.
        return "(" + node.getQuery().accept(this, null) + ")";
    }

    @Override
    public String visit(SelectNode node, Void context) {
        // A SelectNode represents a WHERE clause.
        String sourceSql = node.getSource().accept(this, null);
        String filterSql = node.getFilter().accept(this, null);
        return sourceSql + " WHERE " + filterSql;
    }

    @Override
    public String visit(JoinNode node, Void context) {
        // Translates to "source1 JOIN source2 ON condition".
        String leftSql = node.getLeft().accept(this, null);
        String rightSql = node.getRight().accept(this, null);
        String conditionSql = node.getCondition().accept(this, null);

        // Wrap sources in parentheses if they are not simple tables.
        if (!(node.getLeft() instanceof TableNode)) {
            leftSql = "(" + leftSql + ") AS left_sub";
        }
        if (!(node.getRight() instanceof TableNode)) {
            rightSql = "(" + rightSql + ") AS right_sub";
        }
        return leftSql + " JOIN " + rightSql + " ON " + conditionSql;
    }

    @Override
    public String visit(UnionNode node, Void context) {
        // Translates to "(query1) UNION (query2)". Parentheses are important.
        String leftSql = node.getLeft().accept(this, null);
        String rightSql = node.getRight().accept(this, null);
        return "(" + leftSql + ") UNION (" + rightSql + ")";
    }

    @Override public String visit(AggregationNode node, Void c) { return unsupported(node); }
    @Override public String visit(AggregateExpressionNode node, Void c) { return unsupported(node); }

    @Override public String visit(AndFilterNode node, Void c) { return "(" + node.getLeft().accept(this, c) + " AND " + node.getRight().accept(this, c) + ")"; }

    @Override public String visit(PredicateNode node, Void c) { return unsupported(node); }

    @Override
    public String visit(IsNullPredicateNode node, Void context) {
        return null;
    }

    @Override public String visit(CastExpressionNode node, Void c) { return "CAST(" + node.getExpression().accept(this, c) + " AS " + node.getTargetType() + ")"; }

    @Override public String visit(OrFilterNode node, Void c) { return "(" + node.getLeft().accept(this, c) + " OR " + node.getRight().accept(this, c) + ")"; }

    @Override public String visit(NotFilterNode node, Void c) { return "NOT (" + node.getChild().accept(this, c) + ")"; }

    @Override
    public String visit(BinaryOpExpressionNode node, Void context) {
        return null;
    }
}