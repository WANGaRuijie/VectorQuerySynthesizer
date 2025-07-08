package ast.enums;

public enum PredicateOperator {

    EQ("="), GT(">"), LT("<"), GTE(">="), LTE("<="), NEQ("!=");
    private final String symbol;

    /**
     * Constructor for PredicateOperator enum.
     *
     * @param symbol The String representation of the operator.
     */
    PredicateOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the string representation of the operator.
     *
     * @return The symbol of the operator (e.g., "=", ">", "<", ">=", "<=", "!=").
     */
    @Override
    public String toString() {
        return this.symbol;
    }

}
