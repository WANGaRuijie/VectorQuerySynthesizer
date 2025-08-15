

import ast.ASTPrinter;
import ast.ASTTranslator;
import ast.QueryNode;
import database.ConnectionManager;
import database.QueryExecutor;
import model.Table;
import model.Vector;
import synthesizer.Synthesizer;
import synthesizer.TopDownEnumerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("--- Synthesizer E2E Test: Top-Down Enumeration ---");

        // --- Step 1: Prepare the Complex Input/Output Example ---

        // 1.1 Define the input table with its schema.
        List<Table.Column> inputSchema = List.of(
                new Table.Column("id", "long"),
                new Table.Column("name", "text"),
                new Table.Column("embedding", "vector")
        );
        List<List<Object>> inputRows = new ArrayList<>();
        inputRows.add(Arrays.asList(1L, "blue-sofa", new Vector(new float[]{0.1f, 0.2f, 0.9f})));
        inputRows.add(Arrays.asList(2L, "red-chair", new Vector(new float[]{0.8f, 0.1f, 0.1f})));
        inputRows.add(Arrays.asList(3L, "green-table", new Vector(new float[]{0.2f, 0.9f, 0.2f})));
        inputRows.add(Arrays.asList(4L, "red-sofa", new Vector(new float[]{0.9f, 0.2f, 0.1f})));
        Table inputTable = new Table("items", inputSchema, inputRows);


        Vector userQueryVector = new Vector(new float[]{0.1f, 0.2f, 0.9f});
        List<Vector> queryVectors = List.of(userQueryVector);

        List<Table.Column> outputSchema = List.of(
                new Table.Column("id", "long"),
                new Table.Column("name", "text"),
                new Table.Column("embedding", "vector")
        );
        List<List<Object>> outputRows = new ArrayList<>();
        outputRows.add(Arrays.asList(1L, "blue-sofa", new Vector(new float[]{0.1f, 0.2f, 0.9f})));
        Table outputTable = new Table("output", outputSchema, outputRows);


        // --- Step 2: Instantiate Core Components ---
        // We no longer need the old Enumerator.
        QueryExecutor queryExecutor = new QueryExecutor();
        // Assume you have updated the Synthesizer to use the TopDownEnumerator.
        Synthesizer synthesizer = new Synthesizer(queryExecutor);


        // --- Step 3: Run Synthesis ---
        System.out.println("\nStarting top-down synthesis process...");
        List<QueryNode> solutions = synthesizer.synthesize(
                List.of(inputTable),
                outputTable,
                queryVectors
        );


        // --- Step 4: Verify Results ---
        System.out.println("\n--- TEST VERIFICATION ---");
        if (solutions.isEmpty()) {
            System.out.println("TEST FAILED: No solutions were found.");
            // Optional: Add a debug block to manually run the target query
            String targetSql = "SELECT id, name, category, embedding FROM products WHERE category = 'chair' ORDER BY embedding <-> '[0.1,0.2,0.9]' DESC LIMIT 2";
            System.out.println("DEBUG: Manually executing the target SQL to compare...");
            try {
                Table actualResultFromDb = queryExecutor.executeQuery(targetSql);
                System.out.println("--- EXPECTED OUTPUT (from Main.java) ---");
                System.out.println(outputTable);
                System.out.println("--- ACTUAL OUTPUT (from database) ---");
                System.out.println(actualResultFromDb);
                if (outputTable.equals(actualResultFromDb)) {
                    System.out.println("DEBUG: The tables are equal. The issue is likely in the synthesizer's search/pruning logic.");
                } else {
                    System.out.println("DEBUG: The tables are NOT equal. The outputTable in Main.java may be incorrect.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("TEST PASSED: Found " + solutions.size() + " solution(s).");

            QueryNode firstSolution = solutions.get(0);
            ASTTranslator translator = new ASTTranslator();
            String finalSql = translator.translate(firstSolution);

            System.out.println("\n--- DETAILS OF FIRST SOLUTION ---");
            System.out.println("Generated SQL: \n" + finalSql);

            ASTPrinter printer = new ASTPrinter();
            System.out.println("\nAST Tree of the Solution:");
            System.out.println(firstSolution.accept(printer, 0));
        }

        ConnectionManager.closeConnection();
        System.out.println("\nDatabase connection closed. Test finished.");
    }
}