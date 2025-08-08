package synthesizer;


import ast.*;
import ast.nodes.*;
import database.QueryExecutor;
import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Synthesizer {

    private final Enumerator enumerator;
    private final QueryExecutor queryExecutor;
    private final ASTTranslator astTranslator;

    /**
     * Constructor for the Synthesizer.
     * @param enumerator The component that generates AST node parts.
     * @param queryExecutor The component that executes queries.
     */
    public Synthesizer(Enumerator enumerator, QueryExecutor queryExecutor) {
        this.enumerator = enumerator;
        this.queryExecutor = queryExecutor;
        this.astTranslator = new ASTTranslator(); // Visitor for converting AST to String
    }

    /**
     * The main synthesis method.
     * @return A list of QueryNode ASTs that successfully produce the output table.
     */
    public List<QueryNode> synthesize(List<Table> inputTables, Table outputTable, List<Vector> queryVectors) {

        System.out.println("Starting synthesis...");
        List<QueryNode> solutions = new ArrayList<>();

        // 1. Generate all building blocks at once
        Map<Enumerator.ExpressionType, List<? extends ASTNode>> pool =
                enumerator.generateBuildingBlocks(inputTables, queryVectors);

        // Extract the parts we need for our first simple template
        List<DistanceExpressionNode> distanceExpressions =
                (List<DistanceExpressionNode>) pool.get(Enumerator.ExpressionType.DISTANCE_EXPRESSION);

        // Assume we're working with the first table for now
        if (inputTables.isEmpty()) return solutions;
        Table primaryInputTable = inputTables.get(0);
        QueryNode sourceTableNode = new TableNode(primaryInputTable.getTableName()); // Base of our query

        // 2. Assembly loop for the "Basic Vector Search" template
        System.out.println("Trying Template: Basic Vector Search (SELECT-FROM-ORDER_BY-LIMIT)...");
        for (DistanceExpressionNode distExpr : distanceExpressions) {

            int limitK = outputTable.getRowCount();
            if (limitK == 0) continue; // Cannot synthesize for empty output yet

            // 2.1 Assemble the OrderByNode and LimitNode
            // The structure is Query -> Limit -> OrderBy -> Source
            OrderByNode orderByNode = new OrderByNode(
                    sourceTableNode,
                    List.of(new SortExpression(distExpr)) // Default is ASC, which is what we want for distance
            );
            LimitNode limitNode = new LimitNode(orderByNode, limitK);

            // 2.2 Assemble the final ProjectionNode
            // For now, let's assume SELECT *
            // To do SELECT *, we can select all columns from the source table.
            List<AliasedExpression> selectAllColumns = new ArrayList<>();
            for(String colName : primaryInputTable.getColumnNames()) {
                selectAllColumns.add(new AliasedExpression(new ColumnReferenceNode(colName)));
            }
            QueryNode candidateAST = new ProjectionNode(limitNode, selectAllColumns);


            // 3. Evaluate and Verify
            try {
                // Use the ASTPrinter visitor to convert the AST to a SQL string
                String sql = candidateAST.accept(astTranslator, null);
                Table resultTable = queryExecutor.executeQuery(sql);

                if (resultTable.equals(outputTable)) {
                    System.out.println("SUCCESS: Found a matching query!");
                    System.out.println("SQL: " + sql);
                    solutions.add(candidateAST);
                }
            } catch (Exception e) {
                System.err.println("Evaluation failed for a candidate query.");
                // e.printStackTrace(); // Uncomment for detailed debugging
            }
        }

        // You would add more loops here for other templates, e.g., FilteredSearchTemplate

        System.out.println("Synthesis finished. Found " + solutions.size() + " solution(s).");
        return solutions;
    }
}