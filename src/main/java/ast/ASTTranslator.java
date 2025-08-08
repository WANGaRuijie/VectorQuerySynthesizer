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

    // --- Core Query Assembly Logic ---
    // The main idea is to recursively build the query string.
    // The final SELECT clause is handled by ProjectionNode.
    // Other nodes just append their clauses.

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
        // A LimitNode appends "LIMIT k" to its source's SQL.
        String sourceSql = node.getSource().accept(this, null);
        return sourceSql + " LIMIT " + node.getValue();
    }

    @Override
    public String visit(RenameNode node, Void context) {
        return null;
    }

    @Override
    public String visit(OrderByNode node, Void context) {
        // An OrderByNode appends "ORDER BY ..." to its source's SQL.
        String sourceSql = node.getSource().accept(this, null);
        String sortExprs = node.getSortExpressions().stream()
                .map(se -> se.accept(this, null))
                .collect(Collectors.joining(", "));
        return sourceSql + " ORDER BY " + sortExprs;
    }

    @Override
    public String visit(TableNode node, Void context) {
        // The base case for the recursion: a table name.
        return node.getTableName();
    }

    // --- Expression and other node translations remain the same ---

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

    // ... [ The rest of your visit methods, using the 'unsupported' helper ] ...
    private String unsupported(ASTNode node) {
        throw new UnsupportedOperationException("ASTTranslator does not yet support visiting " + node.getClass().getSimpleName());
    }

    @Override public String visit(WithNode node, Void c) { return unsupported(node); }
    @Override public String visit(FunctionCallNode node, Void c) { return unsupported(node); }
    @Override public String visit(ScalarSubqueryNode node, Void c) { return unsupported(node); }
    @Override public String visit(SelectNode node, Void c) { return unsupported(node); } // For WHERE

    @Override
    public String visit(JoinNode node, Void context) {
        return null;
    }

    @Override
    public String visit(UnionNode node, Void context) {
        return null;
    }

    @Override public String visit(AggregationNode node, Void c) { return unsupported(node); }
    @Override public String visit(AggregateExpressionNode node, Void c) { return unsupported(node); }
    @Override public String visit(AndFilterNode node, Void c) { return "(" + node.getLeft().accept(this, c) + " AND " + node.getRight().accept(this, c) + ")"; }
    @Override public String visit(PredicateNode node, Void c) { return unsupported(node); }

    @Override
    public String visit(IsNullPredicateNode node, Void context) {
        return null;
    }

    // ... etc. for all methods in the Visitor interface
    @Override public String visit(CastExpressionNode node, Void c) { return "CAST(" + node.getExpression().accept(this, c) + " AS " + node.getTargetType() + ")"; }
    @Override public String visit(OrFilterNode node, Void c) { return "(" + node.getLeft().accept(this, c) + " OR " + node.getRight().accept(this, c) + ")"; }
    @Override public String visit(NotFilterNode node, Void c) { return "NOT (" + node.getChild().accept(this, c) + ")"; }

    @Override
    public String visit(BinaryOpExpressionNode node, Void context) {
        return null;
    }
}