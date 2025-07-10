import ast.ASTPrinter;
import ast.QueryNode;
import ast.Visitor;
import ast.enums.AggregateFunction;
import ast.enums.DistanceOperator;
import ast.enums.PredicateOperator;
import ast.enums.SortOrder;
import ast.nodes.*;

import java.util.Arrays;
import java.util.Collections;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Building the full AST for the complex query using the new nodes ---");

        // Step 1: Build the AST for the scalar subquery inside the CTE's ORDER BY.
        // SQL: (SELECT binary_quantize(embedding) FROM simple_wikipedia WHERE title = 'Art')
        QueryNode scalarQueryForCte = new ProjectionNode(
                new SelectNode(
                        new TableNode("simple_wikipedia"),
                        new PredicateNode(
                                new ColumnReferenceNode("title"),
                                PredicateOperator.EQ,
                                new ConstantValueNode("Art")
                        )
                ),
                Collections.singletonList(
                        new AliasedExpression(
                                new FunctionCallNode("binary_quantize", Collections.singletonList(new ColumnReferenceNode("embedding")))
                        )
                )
        );
        ScalarSubqueryNode scalarSubqueryForCte = new ScalarSubqueryNode(scalarQueryForCte);


        // Step 2: Build the complete AST for the CTE body named 'initial_candidates'.
        // SQL: SELECT id, title, embedding FROM ... ORDER BY ... LIMIT 4
        QueryNode initialCandidatesAst = new LimitNode(
                new OrderByNode(
                        new ProjectionNode(
                                new TableNode("simple_wikipedia"),
                                Arrays.asList(
                                        new AliasedExpression(new ColumnReferenceNode("id")),
                                        new AliasedExpression(new ColumnReferenceNode("title")),
                                        new AliasedExpression(new ColumnReferenceNode("embedding"))
                                )
                        ),
                        Collections.singletonList(
                                new SortExpression(
                                        new DistanceExpressionNode(
                                                new FunctionCallNode("binary_quantize", Collections.singletonList(new ColumnReferenceNode("embedding"))),
                                                DistanceOperator.HAMMING_DISTANCE, // <~> operator
                                                scalarSubqueryForCte
                                        ),
                                        SortOrder.ASC // Default sort order
                                )
                        )
                ),
                4
        );


        // Step 3: Build the AST for the scalar subquery in the main query's SELECT list.
        // SQL: (SELECT embedding FROM simple_wikipedia WHERE title = 'Art')
        QueryNode scalarQueryForMain = new ProjectionNode(
                new SelectNode(
                        new TableNode("simple_wikipedia"),
                        new PredicateNode(
                                new ColumnReferenceNode("title"),
                                PredicateOperator.EQ,
                                new ConstantValueNode("Art")
                        )
                ),
                Collections.singletonList(new AliasedExpression(new ColumnReferenceNode("embedding")))
        );
        ScalarSubqueryNode scalarSubqueryForMain = new ScalarSubqueryNode(scalarQueryForMain);


        // Step 4: Build the AST for the main query body.
        // SQL: SELECT ... FROM initial_candidates ORDER BY cosine_distance LIMIT 3
        QueryNode mainQueryBodyAst = new LimitNode(
                new OrderByNode(
                        new ProjectionNode(
                                // The 'FROM' clause now correctly references the CTE name.
                                new TableNode("initial_candidates"),
                                Arrays.asList(
                                        new AliasedExpression(new ColumnReferenceNode("id")),
                                        new AliasedExpression(new ColumnReferenceNode("title")),
                                        // This is the aliased expression: ... AS cosine_distance
                                        new AliasedExpression(
                                                new DistanceExpressionNode(
                                                        new ColumnReferenceNode("embedding"),
                                                        DistanceOperator.COSINE_DISTANCE, // <=> operator
                                                        scalarSubqueryForMain
                                                ),
                                                "cosine_distance" // The alias
                                        )
                                )
                        ),
                        // ORDER BY clause can refer to the alias.
                        Collections.singletonList(new SortExpression(new ColumnReferenceNode("cosine_distance"), SortOrder.ASC))
                ),
                3
        );


        // Step 5: Assemble the final AST using the top-level WithNode.
        Map<String, QueryNode> ctes = new HashMap<>();
        ctes.put("initial_candidates", initialCandidatesAst);

        QueryNode finalAst = new WithNode(ctes, mainQueryBodyAst);


        // Step 6: Print the complete, unified AST using the updated ASTPrinter.
        System.out.println("\n--- Full AST Structure (using updated ASTPrinter) ---");
        Visitor<String, Integer> printer = new ASTPrinter();
        String astStructure = finalAst.accept(printer, 0);
        System.out.println(astStructure);
    }
}