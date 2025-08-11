package synthesizer;

import ast.ExpressionNode;
import ast.FilterNode;
import ast.QueryNode;
import ast.nodes.*;
import ast.enums.*;
import ast.ASTNode;
import model.Table;
import model.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements a top-down, syntax-directed enumerative search using the new Table metadata.
 * It recursively builds ASTs based on the language grammar and performs type-based pruning.
 */
public class TopDownEnumerator {

    private final List<Table> inputTables;
    // Memoization table to store results for (depth, targetType) to avoid re-computation.
    private final Map<String, List<? extends ASTNode>> memo;

    // Base components (leaf nodes)
    private final List<ColumnReferenceNode> availableColumns;
    private final List<ConstantValueNode> availableConstants;
    private final Table primaryTable; // Assuming one table for simplicity

    public TopDownEnumerator(List<Table> inputTables, List<Vector> queryVectors) {
        if (inputTables == null || inputTables.isEmpty()) {
            throw new IllegalArgumentException("Input tables cannot be null or empty.");
        }
        this.inputTables = inputTables;
        this.primaryTable = inputTables.get(0); // Main table for type lookups
        this.memo = new HashMap<>();

        // Pre-generate the leaf nodes of our grammar.
        this.availableColumns = new ArrayList<>();
        this.availableConstants = new ArrayList<>();
        initializeBaseComponents(queryVectors);
    }

    private void initializeBaseComponents(List<Vector> queryVectors) {
        for (Table.Column column : primaryTable.getColumns()) {
            this.availableColumns.add(new ColumnReferenceNode(column.getName()));
        }
        for (List<Object> row : primaryTable.getRows()) {
            for (Object cellValue : row) {
                if (!(cellValue instanceof Vector)) {
                    this.availableConstants.add(new ConstantValueNode(cellValue));
                }
            }
        }
        for (Vector v : queryVectors) {
            this.availableConstants.add(new ConstantValueNode(v));
        }
    }

    /**
     * Main entry point for the top-down search.
     * @param maxDepth The maximum depth/size of the AST to generate.
     * @return A list of all valid, complete QueryNodes up to the given depth.
     */
    public List<QueryNode> enumerate(int maxDepth) {
        return (List<QueryNode>) enumerate(QueryNode.class, maxDepth);
    }

    /**
     * The core recursive enumeration function with memoization.
     */
    private List<? extends ASTNode> enumerate(Class<? extends ASTNode> targetType, int depth) {
        if (depth < 0) return new ArrayList<>();

        String memoKey = depth + "_" + targetType.getName();
        if (memo.containsKey(memoKey)) {
            return memo.get(memoKey);
        }

        List<ASTNode> results = new ArrayList<>();

        // --- Leaf Node Generation (depth budget can be 0 or more) ---
        if (targetType.isAssignableFrom(ColumnReferenceNode.class)) {
            results.addAll(availableColumns);
        }
        if (targetType.isAssignableFrom(ConstantValueNode.class)) {
            results.addAll(availableConstants);
        }
        if (targetType.isAssignableFrom(TableNode.class)) {
            results.add(new TableNode(primaryTable.getName()));
        }
        if (depth == 0) {
            memo.put(memoKey, results);
            return results;
        }

        // --- Recursive, Syntax-Directed Rules (depth must be > 0) ---

        // Rule: Generate ExpressionNodes
        if (ExpressionNode.class.isAssignableFrom(targetType)) {
            // An Expression can be a DistanceExpression
            List<ExpressionNode> leftExprs = (List<ExpressionNode>) enumerate(ExpressionNode.class, depth - 1);
            List<ExpressionNode> rightExprs = (List<ExpressionNode>) enumerate(ExpressionNode.class, depth - 1);
            for (ExpressionNode left : leftExprs) {
                for (ExpressionNode right : rightExprs) {
                    // Semantic Pruning: only combine if both sides are vectors.
                    if (isVector(left) && isVector(right)) {
                        for (DistanceOperator op : DistanceOperator.values()) {
                            results.add(new DistanceExpressionNode(left, op, right));
                        }
                    }
                }
            }
        }

        // Rule: Generate FilterNodes (e.g., PredicateNode)
        if (FilterNode.class.isAssignableFrom(targetType)) {
            List<ExpressionNode> leftExprs = (List<ExpressionNode>) enumerate(ExpressionNode.class, depth - 1);
            List<ExpressionNode> rightExprs = (List<ExpressionNode>) enumerate(ExpressionNode.class, depth - 1);
            for (ExpressionNode left : leftExprs) {
                for (ExpressionNode right : rightExprs) {
                    // Semantic Pruning: Check for type compatibility
                    if (areTypesCompatibleForPredicate(left, right)) {
                        for (PredicateOperator op : PredicateOperator.values()) {
                            results.add(new PredicateNode(left, op, right));
                        }
                    }
                }
            }
        }

        // Rule: Generate QueryNodes (e.g., OrderByNode, LimitNode)
        if (QueryNode.class.isAssignableFrom(targetType)) {
            List<QueryNode> sourceQueries = (List<QueryNode>) enumerate(QueryNode.class, depth - 1);
            for (QueryNode source : sourceQueries) {
                // Option 1: Add an OrderBy clause
                List<ExpressionNode> sortableExprs = (List<ExpressionNode>) enumerate(ExpressionNode.class, 0); // Use simple expressions for sorting
                for (ExpressionNode expr : sortableExprs) {
                    results.add(new OrderByNode(source, List.of(new SortExpression(expr))));
                }

                // Option 2: Add a Limit clause
                for (int k : List.of(1, 2, 3, 4, 5, 6, 7)) { // Hardcoded limit values for enumeration
                    results.add(new LimitNode(source, k));
                }

                // Option 3: Add a Where clause
                //List<FilterNode> filters = (List<FilterNode>) enumerate(FilterNode.class, depth - 1);
                //for (FilterNode filter a: filters) {
                //    results.add(new SelectNode(source, filter));
                //}
            }
        }

        // The final assembly of ProjectionNode should be handled in the Synthesizer
        // to ensure the final query is always a valid SELECT statement.

        memo.put(memoKey, results);
        return results;
    }

    /**
     * Helper for semantic checks: Determines if an expression resolves to a vector type.
     */
    private boolean isVector(ExpressionNode node) {
        if (node instanceof ColumnReferenceNode) {
            String colName = ((ColumnReferenceNode) node).getColumnName();
            Optional<Table.Column> column = primaryTable.getColumn(colName);
            return column.isPresent() && column.get().isVector();
        }
        if (node instanceof ConstantValueNode) {
            return ((ConstantValueNode) node).getValue() instanceof Vector;
        }
        // A full implementation would require a type inference system for complex expressions.
        return false;
    }

    /**
     * Helper for semantic checks: Determines if two expressions are compatible for a predicate.
     * E.g., don't compare a vector to a number.
     */
    private boolean areTypesCompatibleForPredicate(ExpressionNode left, ExpressionNode right) {
        boolean leftIsVec = isVector(left);
        boolean rightIsVec = isVector(right);
        // Don't compare vectors with non-vectors using simple predicates.
        return leftIsVec == rightIsVec;
    }
}

