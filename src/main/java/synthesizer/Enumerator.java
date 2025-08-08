package synthesizer;

import ast.*;
import ast.enums.*;
import ast.nodes.ColumnReferenceNode;
import ast.nodes.ConstantValueNode;
import ast.nodes.DistanceExpressionNode;
import ast.nodes.PredicateNode;
import model.Table;
import model.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Enumerator {

    /**
     * An enum to use as a typed key for our expression map.
     * This is cleaner than using raw strings.
     */
    public enum ExpressionType {
        COLUMN_REFERENCE,
        CONSTANT_VALUE,
        DISTANCE_EXPRESSION,
        PREDICATE_FILTER,
        // You can add more types here later, e.g., FUNCTION_CALL
    }

    /**
     * Generates a pool of basic expression and filter nodes.
     * This method will be the core of the Enumerator's functionality.
     *
     * @param inputTables The list of input tables to extract columns and constants from.
     * @param queryVectors The list of user-provided vectors.
     * @return A map where keys are types and values are lists of corresponding AST nodes.
     */
    public Map<ExpressionType, List<? extends ASTNode>> generateBuildingBlocks (List<Table> inputTables, List<Vector> queryVectors) {

        Map<ExpressionType, List<? extends ASTNode>> pool = new HashMap<>();

        // 1. Enumerate all ColumnReferenceNodes
        List<ColumnReferenceNode> columnNodes = new ArrayList<>();
        // 2. Enumerate all ConstantValueNodes from table data
        List<ConstantValueNode> constantNodes = new ArrayList<>();

        for (Table table : inputTables) {
            for (String columnName : table.getColumnNames()) {
                columnNodes.add(new ColumnReferenceNode(columnName));
            }
            for (List<Object> row : table.getRows()) {
                for (Object cellValue : row) {
                    if (!(cellValue instanceof Vector)) { // Don't treat vectors as regular constants
                        constantNodes.add(new ConstantValueNode(cellValue));
                    }
                }
            }
        }
        pool.put(ExpressionType.COLUMN_REFERENCE, columnNodes);

        // 3. Add user-provided vectors as ConstantValueNodes
        for (Vector vector : queryVectors) {
            constantNodes.add(new ConstantValueNode(vector));
        }
        pool.put(ExpressionType.CONSTANT_VALUE, constantNodes);


        // 4. Enumerate DistanceExpressionNodes
        List<DistanceExpressionNode> distanceExpressions = new ArrayList<>();
        List<ConstantValueNode> vectorConstants = constantNodes.stream()
                .filter(c -> c.getValue() instanceof Vector)
                .collect(Collectors.toList());

        for (ColumnReferenceNode col : columnNodes) {
            // This is a crucial check to ensure we only apply vector operators to vector columns.
            // For now, we hardcode the check against the column name.
            if (!col.getColumnName().equalsIgnoreCase("embedding")) {
                continue; // Skip non-vector columns like 'id' and 'name'.
            }

            for (ConstantValueNode vecConst : vectorConstants) {
                // Now, this loop only runs for the 'embedding' column.
                distanceExpressions.add(new DistanceExpressionNode(col, DistanceOperator.L2_DISTANCE, vecConst));
            }
        }
        pool.put(ExpressionType.DISTANCE_EXPRESSION, distanceExpressions);



        // 5. Enumerate PredicateNodes (for WHERE clauses)
        List<PredicateNode> predicateNodes = new ArrayList<>();
        List<ConstantValueNode> nonVectorConstants = constantNodes.stream()
                .filter(c -> !(c.getValue() instanceof Vector))
                .collect(Collectors.toList());

        for (ColumnReferenceNode col : columnNodes) {
            for (ConstantValueNode cnst : nonVectorConstants) {
                // Generates "column = constant", "column > constant", etc.
                for (PredicateOperator op : PredicateOperator.values()) {
                    predicateNodes.add(new PredicateNode(col, op, cnst));
                }
            }
        }
        pool.put(ExpressionType.PREDICATE_FILTER, predicateNodes);

        return pool;
    }
}