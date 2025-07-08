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

}
