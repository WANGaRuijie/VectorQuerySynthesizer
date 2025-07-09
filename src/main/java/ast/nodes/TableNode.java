package ast.nodes;

import ast.QueryNode;
import ast.Visitor;

public class TableNode implements QueryNode {

    private final String tableName;

    public TableNode(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public <R,C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        return "Table(" + tableName + ")";
    }
}
