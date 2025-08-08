// Make sure all necessary classes are imported.
// You might need to adjust the package paths if they are different in your project.
import ast.ASTPrinter;
import ast.ASTTranslator;
import ast.QueryNode;
import database.ConnectionManager;
import database.QueryExecutor;
import model.Table;
import model.Vector;
import synthesizer.Enumerator;
import synthesizer.Synthesizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("--- Synthesizer End-to-End Test for Week 3 ---");

        // --- Step 1: Prepare Input/Output Examples ---
        // This data should perfectly match what's in your test database.

        // 1.1 Define the input table that mirrors the 'items' table in the database.
        List<String> inputColumnNames = Arrays.asList("id", "name", "embedding");
        List<List<Object>> inputRows = new ArrayList<>();
        // Use 'L' after numbers to ensure they are of type Long for consistent comparison.
        inputRows.add(Arrays.asList(1L, "blue-sofa", "sofa", new Vector(new float[]{0.1f, 0.2f, 0.9f})));
        inputRows.add(Arrays.asList(2L, "red-chair", "chair", new Vector(new float[]{0.8f, 0.1f, 0.1f})));
        inputRows.add(Arrays.asList(3L, "green-table", "table", new Vector(new float[]{0.2f, 0.9f, 0.2f})));
        inputRows.add(Arrays.asList(4L, "red-sofa", "sofa", new Vector(new float[]{0.9f, 0.2f, 0.1f})));
        inputRows.add(Arrays.asList(5L, "blue-chair", "chair", new Vector(new float[]{0.2f, 0.3f, 0.8f})));
        Table inputTable = new Table("products", inputColumnNames, inputRows);



        // 1.2 The query vector remains the same.
        Vector userQueryVector = new Vector(new float[]{1.0f, 0.0f, 0.0f});
        List<Vector> queryVectors = List.of(userQueryVector);

        // 1.3 Define the new, very specific expected output.
        // The ONLY correct answer is the single 'red-sofa'.
        List<String> outputColumnNames = Arrays.asList("id", "name", "category", "embedding");
        List<List<Object>> outputRows = new ArrayList<>();
        outputRows.add(Arrays.asList(4L, "red-sofa", "sofa", new Vector(new float[]{0.9f, 0.2f, 0.1f})));
        Table outputTable = new Table("output", outputColumnNames, outputRows);



        // --- Step 2: Instantiate the Core Components ---
        Enumerator enumerator = new Enumerator();
        QueryExecutor queryExecutor = new QueryExecutor();
        Synthesizer synthesizer = new Synthesizer(enumerator, queryExecutor);


        // --- Step 3: Run the Synthesis Process ---
        System.out.println("\nStarting synthesis process...");
        List<QueryNode> solutions = synthesizer.synthesize(
                List.of(inputTable),
                outputTable,
                queryVectors
        );


        // --- Step 4: Verify the Results ---
        System.out.println("\n--- TEST VERIFICATION ---");
        if (solutions.isEmpty()) {
            System.out.println("TEST FAILED: No solutions were found.");
            // Add a debug block to see why it failed
            System.out.println("DEBUG: Running the target query manually to see what the database returns...");
            try {
                String targetSql = "SELECT id, name, embedding FROM items ORDER BY embedding <-> '[1.0,0.0,0.0]' ASC LIMIT 2";
                Table actualResultFromDb = queryExecutor.executeQuery(targetSql);
                System.out.println("--- EXPECTED OUTPUT (from Main.java) ---");
                System.out.println(outputTable);
                System.out.println("--- ACTUAL OUTPUT (from database) ---");
                System.out.println(actualResultFromDb);
                if (outputTable.equals(actualResultFromDb)) {
                    System.out.println("DEBUG: The two tables are equal. The issue might be in the synthesizer's logic.");
                } else {
                    System.out.println("DEBUG: The two tables are NOT equal. The outputTable in Main.java is incorrect.");
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Manual query execution failed.");
                e.printStackTrace();
            }

        } else {
            System.out.println("TEST PASSED: Found " + solutions.size() + " solution(s).");

            QueryNode firstSolution = solutions.get(0);
            ASTPrinter printer = new ASTPrinter(); // For pretty-printing the AST
            ASTTranslator translator = new ASTTranslator(); // For generating executable SQL

            String finalSql = firstSolution.accept(translator, null);

            System.out.println("\n--- DETAILS OF FIRST SOLUTION ---");
            System.out.println("Generated SQL: " + finalSql);
            System.out.println("AST Tree:");
            System.out.println(firstSolution.accept(printer, 0));
        }

        // --- Final Step: Clean up database connection ---
        ConnectionManager.closeConnection();
        System.out.println("\nDatabase connection closed. Test finished.");
    }
}