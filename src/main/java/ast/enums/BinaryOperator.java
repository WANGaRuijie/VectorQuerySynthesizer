package ast.enums;

/**
 * Represents standard binary arithmetic operators.
 */
public enum BinaryOperator {

    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/");

    private final String symbol;

    BinaryOperator(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
