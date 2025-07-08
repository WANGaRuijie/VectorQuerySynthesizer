package ast;

public interface ASTNode {
    /**
     * Accepts a visitor to perform operations on this AST node.
     *
     * @param <R> The return type of the visitor's visit method.
     * @param <C> The context type that the visitor may require.
     * @param visitor The visitor instance that will process this node.
     * @param context The context in which the visitor operates.
     * @return The result of the visitor's operation on this node.
     */
    <R, C> R accept(ast.Visitor<R, C> visitor, C context);
}
