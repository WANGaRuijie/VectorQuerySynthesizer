package ast;

import ast.Visitor;
import ast.nodes.*;

import java.util.List;
import java.util.Map;

/**
 * An updated AST printer that handles the new language features like
 * WITH clauses, function calls, and aliased expressions.
 */
public class ASTPrinter implements Visitor<String, Integer> {

    private String indent(int level) {
        return "  ".repeat(Math.max(0, level));
    }

    private String visitChildren(List<? extends ASTNode> children, int level) {
        StringBuilder sb = new StringBuilder();
        for (ASTNode child : children) {
            sb.append(child.accept(this, level));
        }
        return sb.toString();
    }

    @Override
    public String visit(WithNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("WithNode\n");
        sb.append(indent(level + 1)).append("CTEs:\n");
        for (Map.Entry<String, QueryNode> cte : node.getCtes().entrySet()) {
            sb.append(indent(level + 2)).append("CTE(name=").append(cte.getKey()).append(")\n");
            sb.append(cte.getValue().accept(this, level + 3));
        }
        sb.append(indent(level + 1)).append("Body:\n");
        sb.append(node.getBody().accept(this, level + 2));
        return sb.toString();
    }

    @Override
    public String visit(FunctionCallNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("FunctionCallNode(name=").append(node.getFunctionName()).append(")\n");
        if (!node.getArguments().isEmpty()) {
            sb.append(indent(level + 1)).append("Arguments:\n");
            sb.append(visitChildren(node.getArguments(), level + 2));
        }
        return sb.toString();
    }

    @Override
    public String visit(ScalarSubqueryNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("ScalarSubqueryNode\n");
        sb.append(indent(level + 1)).append("Query:\n");
        sb.append(node.getQuery().accept(this, level + 2));
        return sb.toString();
    }

    @Override
    public String visit(ProjectionNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("ProjectionNode\n");
        sb.append(indent(level + 1)).append("SelectList:\n");
        // We can't use visitChildren directly because AliasedExpression is not an ASTNode.
        // We must iterate and handle it manually.
        for (AliasedExpression aliasedExpr : node.getSelectList()) {
            String aliasStr = aliasedExpr.hasAlias() ? " (AS " + aliasedExpr.alias() + ")" : "";
            sb.append(indent(level + 2)).append("AliasedExpression").append(aliasStr).append("\n");
            sb.append(indent(level + 3)).append("Expression:\n");
            sb.append(aliasedExpr.expression().accept(this, level + 4));
        }
        sb.append(indent(level + 1)).append("Source:\n");
        sb.append(node.getSource().accept(this, level + 2));
        return sb.toString();
    }

    @Override
    public String visit(TableNode node, Integer level) {
        return indent(level) + "TableNode(name=" + node.getTableName() + ")\n";
    }

    @Override
    public String visit(SelectNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("SelectNode (WHERE)\n");
        sb.append(indent(level + 1)).append("Filter:\n");
        sb.append(node.getFilter().accept(this, level + 2));
        sb.append(indent(level + 1)).append("Source:\n");
        sb.append(node.getSource().accept(this, level + 2));
        return sb.toString();
    }

    @Override
    public String visit(OrderByNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("OrderByNode\n");
        sb.append(indent(level + 1)).append("Sort Column:\n");
        sb.append(node.getSortColumn().accept(this, level + 2));
        sb.append(indent(level + 1)).append("Sort Order: ").append(node.getSortOrder()).append("\n");
        sb.append(indent(level + 1)).append("Source:\n");
        sb.append(node.getSource().accept(this, level + 2));
        return sb.toString();
    }


    @Override
    public String visit(LimitNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("LimitNode(value=").append(node.getValue()).append(")\n");
        sb.append(indent(level + 1)).append("Source:\n");
        sb.append(node.getSource().accept(this, level + 2));
        return sb.toString();
    }

    @Override
    public String visit(AggregationNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("AggregationNode (GROUP BY)\n");
        sb.append(indent(level + 1)).append("Grouping Keys:\n");
        sb.append(visitChildren(node.getGroupingKeys(), level + 2));
        sb.append(indent(level + 1)).append("Aggregates:\n");
        sb.append(visitChildren(node.getAggregateExpressions(), level + 2));
        if (node.getHaving() != null) {
            sb.append(indent(level + 1)).append("Having:\n");
            sb.append(node.getHaving().accept(this, level + 2));
        }
        sb.append(indent(level + 1)).append("Source:\n");
        sb.append(node.getSource().accept(this, level + 2));
        return sb.toString();
    }

    // --- Expression Nodes ---

    // Corrected to match your class name
    @Override
    public String visit(ColumnReferenceNode node, Integer level) {
        return indent(level) + "ColumnReferenceNode(name=" + node.getColumnName() + ")\n";
    }

    @Override
    public String visit(AggregateExpressionNode node, Integer level) {
        String distinctStr = node.isDistinct() ? "DISTINCT " : "";
        String argStr = (node.getArgument() == null) ? "*" : "\n" + node.getArgument().accept(this, level + 1);
        return indent(level) + "AggregateExpressionNode(func=" + node.getFunction() + ", distinct=" + distinctStr + ")\n" + argStr;
    }

    // --- Filter Nodes ---

    @Override
    public String visit(AndFilterNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("AndFilterNode\n");
        sb.append(node.getLeft().accept(this, level + 1));
        sb.append(node.getRight().accept(this, level + 1));
        return sb.toString();
    }

    @Override
    public String visit(PredicateNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("PredicateNode(op=").append(node.getOperator()).append(")\n");
        sb.append(indent(level + 1)).append("Left:\n");
        sb.append(node.getLeft().accept(this, level + 2));
        sb.append(indent(level + 1)).append("Right:\n");
        sb.append(node.getRight().accept(this, level + 2));
        return sb.toString();
    }

    // --- Value Nodes ---

    @Override
    public String visit(ConstantValueNode node, Integer level) {
        String valueStr = node.getValue() instanceof String ? "'" + node.getValue() + "'" : node.getValue().toString();
        return indent(level) + "ConstantValueNode(value=" + valueStr + ")\n";
    }

    // --- Helper Nodes ---

    @Override
    public String visit(SortExpression node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("SortExpression(order=").append(node.getOrder()).append(")\n");
        sb.append(node.getExpression().accept(this, level + 1));
        return sb.toString();
    }

    private String defaultVisit(String nodeName, int level) {
        return indent(level) + nodeName + " (not fully implemented in printer)\n";
    }

    @Override public String visit(JoinNode n, Integer l) { return defaultVisit("JoinNode", l); }
    @Override public String visit(UnionNode n, Integer l) { return defaultVisit("UnionNode", l); }
    @Override public String visit(RenameNode n, Integer l) { return defaultVisit("RenameNode", l); }
    @Override public String visit(IsNullPredicateNode n, Integer l) { return defaultVisit("IsNullPredicateNode", l); }
    @Override public String visit(OrFilterNode n, Integer l) { return defaultVisit("OrFilterNode", l); }
    @Override public String visit(NotFilterNode n, Integer l) { return defaultVisit("NotFilterNode", l); }
    @Override public String visit(NullValueNode n, Integer l) { return indent(l) + "NullValueNode\n"; }


    @Override
    public String visit(DistanceExpressionNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("DistanceExpressionNode(op=").append(node.getOperator()).append(")\n");
        sb.append(indent(level + 1)).append("Left:\n");
        sb.append(node.getLeft().accept(this, level + 2));
        sb.append(indent(level + 1)).append("Right:\n");
        sb.append(node.getRight().accept(this, level + 2));
        return sb.toString();
    }

    @Override
    public String visit(BinaryOpExpressionNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("BinaryOpExpressionNode(op=").append(node.getOperator()).append(")\n");
        sb.append(indent(level + 1)).append("Left:\n");
        sb.append(node.getLeft().accept(this, level + 2));
        sb.append(indent(level + 1)).append("Right:\n");
        sb.append(node.getRight().accept(this, level + 2));
        return sb.toString();
    }

    @Override
    public String visit(CastExpressionNode node, Integer level) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent(level)).append("CastExpressionNode(type=").append(node.getTargetType()).append(")\n");
        sb.append(indent(level + 1)).append("Expression:\n");
        sb.append(node.getExpression().accept(this, level + 2));
        return sb.toString();
    }
}