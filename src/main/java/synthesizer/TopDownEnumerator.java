package synthesizer;

import ast.ExpressionNode;
import ast.FilterNode;
import ast.QueryNode;
import ast.nodes.*;
import ast.enums.*;
import ast.ASTNode;
import ast.OrderableQuery;
import ast.LimitableQuery;
import model.Table;
import model.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 Implements a top-down, syntax-directed enumerative search using the new Table metadata.
 It recursively builds ASTs based on the language grammar and performs type-based pruning.
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
     Main entry point for the top-down search.
     @param maxDepth The maximum depth/size of the AST to generate.
     @return A list of all valid, complete QueryNodes up to the given depth.
     */
    public List<QueryNode> enumerate(int maxDepth) {
        return (List<QueryNode>) enumerate(QueryNode.class, maxDepth);
    }

    /**
     The core recursive enumeration function with memoization.
     */
    private List<? extends ASTNode> enumerate(Class<? extends ASTNode> targetType, int depth) {
        if (depth < 0) return new ArrayList<>();
        String memoKey = depth + "_" + targetType.getName();
        if (memo.containsKey(memoKey)) {
            return memo.get(memoKey);
        }
        List<ASTNode> results = new ArrayList<>();

        // --- Base Cases: depth = 0 (Leaf Nodes) ---
        if (depth == 0) {
            // A TableNode is the only OrderableQuery we can generate at depth 0.
            if (OrderableQuery.class.isAssignableFrom(targetType)) {
                results.add(new TableNode(primaryTable.getName()));
            }

            // Expression leaves
            if (ExpressionNode.class.isAssignableFrom(targetType)) {
                results.addAll(availableColumns);
                results.addAll(availableConstants);
            }

            memo.put(memoKey, results);
            return results;
        }

        // --- Recursive Rules (depth > 0) ---
        // Rule: To generate an OrderableQuery (e.g., Table, Select, Join)
        if (OrderableQuery.class.isAssignableFrom(targetType)) {
            // It can be a SelectNode applied to another OrderableQuery of a smaller depth.
            List<OrderableQuery> sources = (List<OrderableQuery>) enumerate(OrderableQuery.class, depth - 1);
            List<FilterNode> filters = (List<FilterNode>) enumerate(FilterNode.class, depth - 1);
            for (OrderableQuery source : sources) {
                for (FilterNode filter : filters) {
                    results.add(new SelectNode(source, filter));
                }
            }
        // Add rules for JoinNode here if needed.
        }

        // Rule: To generate a LimitableQuery (e.g., OrderableQuery, OrderByNode)
        if (LimitableQuery.class.isAssignableFrom(targetType)) {
            // Option 1: Any OrderableQuery of the same depth is also Limitable.
            results.addAll((List<LimitableQuery>) enumerate(OrderableQuery.class, depth));

            // Option 2: An OrderByNode applied to an OrderableQuery of a smaller depth.
            // The depth budget is now simpler: 1 for the OrderByNode, depth-1 for the source.
            // The ColumnReferenceNode is a leaf (depth 0), so it doesn't consume depth from the budget.
            if (depth > 0) {
                List<OrderableQuery> sources = (List<OrderableQuery>) enumerate(OrderableQuery.class, depth - 1);

                // Get all available columns to sort by. These are leaf nodes.
                List<ColumnReferenceNode> sortableColumns = this.availableColumns;

                for (OrderableQuery source : sources) {
                    for (ColumnReferenceNode column : sortableColumns) {
                        // Create versions for both ASC and DESC
                        results.add(new OrderByNode(source, column, SortOrder.ASC));
                        results.add(new OrderByNode(source, column, SortOrder.DESC));
                    }
                }
            }
        }

        // Rule: To generate a top-level QueryNode (e.g., LimitableQuery, LimitNode)
        if (targetType == QueryNode.class) { // Exact match for the final query type
            // Option 1: Any LimitableQuery is a valid final query.
            results.addAll((List<QueryNode>) enumerate(LimitableQuery.class, depth));

            // Option 2: A LimitNode applied to a LimitableQuery of smaller depth.
            List<LimitableQuery> sources = (List<LimitableQuery>) enumerate(LimitableQuery.class, depth - 1);
            for (LimitableQuery source : sources) {
                for (int k : List.of(1, 2, 5, 10)) { // Hardcoded limit values for enumeration
                    results.add(new LimitNode(source, k));
                }
            }
        }

        // Rules for generating ExpressionNode and FilterNode primitives.
        if (ExpressionNode.class.isAssignableFrom(targetType)) {
            generateExpressions(results, depth);
        }
        if (FilterNode.class.isAssignableFrom(targetType)) {
            generateFilters(results, depth);
        }

        memo.put(memoKey, results);
        return results;
    }

    /**
     Helper for semantic checks: Determines if an expression resolves to a vector type.
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
    
    // --- Helper methods for generating primitive expressions and filters ---
    private void generateExpressions(List<ASTNode> results, int depth) {
        if (depth > 0) {
            List<ExpressionNode> children = (List<ExpressionNode>) enumerate(ExpressionNode.class, depth - 1);
            for (ExpressionNode left : children) {
                // Combine with a leaf node to maintain the depth model (1 + max(children))
                for (ExpressionNode right : (List<ExpressionNode>) enumerate(ExpressionNode.class, 0)) {
                    if (isVector(left) && isVector(right)) {
                        for (DistanceOperator op : DistanceOperator.values()) {
                            results.add(new DistanceExpressionNode(left, op, right));
                            results.add(new DistanceExpressionNode(right, op, left)); // Symmetric
                        }
                    }
                }
            }
        }
    }

    private void generateFilters(List<ASTNode> results, int depth) {
        if (depth > 0) {
            List<ExpressionNode> children = (List<ExpressionNode>) enumerate(ExpressionNode.class, depth - 1);
            for (ExpressionNode left : children) {
                for (ExpressionNode right : (List<ExpressionNode>) enumerate(ExpressionNode.class, 0)) {
                    if (areTypesCompatibleForPredicate(left, right)) {
                        for (PredicateOperator op : PredicateOperator.values()) {
                            results.add(new PredicateNode(left, op, right));
                            results.add(new PredicateNode(right, op, left)); // Symmetric
                        }
                    }
                }
            }
        }
    }

    /**
     Helper for semantic checks: Determines if two expressions are compatible for a predicate.
     E.g., don't compare a vector to a number.
     */
    private boolean areTypesCompatibleForPredicate(ExpressionNode left, ExpressionNode right) {
        boolean leftIsVec = isVector(left);
        boolean rightIsVec = isVector(right);
        // Don't compare vectors with non-vectors using simple predicates.
        return leftIsVec == rightIsVec;
    }
}


