package synthesizer;

import ast.QueryNode;
import ast.nodes.*;
import ast.ASTTranslator;
import database.QueryExecutor;
import model.Table;
import model.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Synthesizer {

    private final QueryExecutor queryExecutor;
    private final ASTTranslator sqlTranslator;

    public Synthesizer(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
        this.sqlTranslator = new ASTTranslator(); // Instantiate the translator
    }

    /**
     * Synthesizes queries using a top-down enumerative search.
     */
    public List<QueryNode> synthesize(List<Table> inputTables, Table outputTable, List<Vector> queryVectors) {

        System.out.println("Starting top-down synthesis process...");
        List<QueryNode> solutions = new ArrayList<>();

        if (inputTables.isEmpty()) {
            return solutions;
        }
        Table primaryTable = inputTables.get(0);

        TopDownEnumerator enumerator = new TopDownEnumerator(inputTables, queryVectors);

        // Pre-create the "SELECT *" part for final assembly
        List<AliasedExpression> selectAllColumns = primaryTable.getColumnNames().stream()
                .map(colName -> new AliasedExpression(new ColumnReferenceNode(colName)))
                .collect(Collectors.toList());

        for (int depth = 1; depth <= 5; depth++) { // Try up to a reasonable depth
            System.out.println("\n--- Enumerating queries at depth: " + depth + " ---");

            // 1. Generate all possible query BODIES (e.g., TableNode, OrderByNode, SelectNode)
            List<QueryNode> candidateBodies = enumerator.enumerate(depth);

            System.out.println("Generated " + candidateBodies.size() + " candidate query bodies.");

            // 2. Evaluate each candidate by WRAPPING it in a final ProjectionNode
            for (QueryNode body : candidateBodies) {

                // --- THIS IS THE CRITICAL FIX ---
                // Every generated body must be wrapped in a ProjectionNode to form a complete,
                // executable SELECT statement.
                QueryNode finalCandidateAST = new ProjectionNode(body, selectAllColumns);
                // --- END OF FIX ---

                try {
                    String sql = sqlTranslator.translate(finalCandidateAST);
                    Table resultTable = queryExecutor.executeQuery(sql);

                    if (resultTable.equals(outputTable)) {
                        System.out.println("SUCCESS: Found a matching query!");
                        System.out.println("SQL: " + sql);
                        solutions.add(finalCandidateAST);
                    }
                } catch (UnsupportedOperationException e) {
                    // This can happen if the translator doesn't support a node type yet.
                    // System.err.println("Translation not supported for a candidate: " + e.getMessage());
                } catch (RuntimeException e) {
                    // This catches SQL execution errors from the database.
                    // This is expected for semantically incorrect queries.
                    // System.err.println("Evaluation failed for a candidate.");
                }
            }

            if (!solutions.isEmpty()) {
                System.out.println("Solutions found at depth " + depth + ". Stopping search.");
                break;
            }
        }

        System.out.println("Synthesis finished. Found " + solutions.size() + " solution(s).");
        return solutions;
    }
}