package ast;

import ast.nodes.*;

public interface Visitor<R, C> {

    R visit(TableNode node, C context);

    R visit(AggregateExpressionNode node, C context);

    R visit(ProjectionNode node, C context);

    R visit(ColumnReferenceNode node, C context);

    R visit(ConstantValueNode node, C context);

    R visit(NullValueNode node, C context);

    R visit(PredicateNode node, C context);

    R visit(IsNullPredicateNode node, C context);

    R visit(DistanceExpressionNode node, C context);

    R visit(AndFilterNode node, C context);

    R visit(OrFilterNode node, C context);

    R visit(NotFilterNode node, C context);

    R visit(BinaryOpExpressionNode node, C context);

    R visit(CastExpressionNode node, C context);

    R visit(SortExpression node, C context);

    R visit(SelectNode node, C context);

    R visit(JoinNode node, C context);

    R visit(UnionNode node, C context);

    R visit(OrderByNode node, C context);

    R visit(LimitNode node, C context);

    R visit(RenameNode node, C context);

    R visit(AggregationNode node, C context);

}
