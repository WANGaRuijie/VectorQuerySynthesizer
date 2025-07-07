package queryast.ast;

import queryast.ast.nodes.TableNode;

public interface Visitor<R, C> {

    R visit(TableNode node, C context);

}
