package queryast.ast.nodes;

import queryast.ast.QueryNode;
import queryast.ast.Visitor;

public class TableNode implements QueryNode {

    private final String tableName;

    public TableNode(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public <R,C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        return "Table(" + tableName + ")";
    }
}
