import ast.ASTPrinter;
import ast.QueryNode;
import ast.Visitor;
import ast.enums.AggregateFunction;
import ast.enums.PredicateOperator;
import ast.enums.SortOrder;
import ast.nodes.*;

import java.util.Arrays;
import java.util.Collections;

public class Main {

        public static void main(String[] args) {
            // We will build an AST for a complex query:
            // SELECT
            //   department,
            //   AVG(salary) as avg_salary
            // FROM employees
            // WHERE city = 'New York'
            // GROUP BY department
            // ORDER BY avg_salary DESC
            // LIMIT 5;

            System.out.println("Building a complex AST...");

            // Aliases are tricky in a simple AST. We'll order by the aggregate expression itself.
            AggregateExpressionNode avgSalary = new AggregateExpressionNode(AggregateFunction.AVG, new ColumnReferenceNode("salary"));
            AggregateExpressionNode countStar = AggregateExpressionNode.createCountStar();

            QueryNode rootNode =
                    new ProjectionNode( // SELECT department, AVG(salary)
                            new LimitNode( // LIMIT 5
                                    new OrderByNode( // ORDER BY avg_salary DESC
                                            new AggregationNode( // GROUP BY department HAVING COUNT(*) > 10
                                                    new SelectNode( // WHERE city = 'New York'
                                                            new TableNode("employees"), // FROM employees
                                                            new PredicateNode(
                                                                    new ColumnReferenceNode("city"),
                                                                    PredicateOperator.EQ,
                                                                    new ConstantValueNode("New York")
                                                            )
                                                    ),
                                                    Collections.singletonList(new ColumnReferenceNode("department")), // GROUP BY keys
                                                    Arrays.asList(avgSalary, countStar), // Aggregates to compute
                                                    new PredicateNode( // HAVING clause
                                                            countStar,
                                                            PredicateOperator.GT,
                                                            new ConstantValueNode(10)
                                                    )
                                            ),
                                            Collections.singletonList(new SortExpression(avgSalary, SortOrder.DESC)) // ORDER BY expressions
                                    ), 5
                            ),
                            Arrays.asList(new ColumnReferenceNode("department"), avgSalary) // Final SELECT list
                    );

            System.out.println("\n--- AST Structure (using AstPrinterVisitor) ---");

            // Create the visitor instance
            Visitor<String, Integer> printer = new ASTPrinter();

            // Start the visiting process from the root node with an initial indentation level of 0
            String astStructure = rootNode.accept(printer, 0);

            // Print the result
            System.out.println(astStructure);
        }

}