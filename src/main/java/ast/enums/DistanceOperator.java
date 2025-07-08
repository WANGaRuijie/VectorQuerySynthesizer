package ast.enums;

public enum DistanceOperator {

    /**
     * L2 Distance (Euclidean Distance).
     * Represents the straight-line distance between two points in Euclidean space.
     * Symbol: {@code <->}
     */
    L2_DISTANCE("<->"),

    /**
     * Cosine Distance.
     * Measures the cosine of the angle between two vectors, which is a measure of orientation, not magnitude.
     * Cosine Distance = 1 - Cosine Similarity.
     * Symbol: {@code <=>}
     */
    COSINE_DISTANCE("<=>"),

    /**
     * Negative Inner Product (Max Inner Product Search).
     * Often used to find the vector with the maximum inner product similarity,
     * which is equivalent to finding the minimum negative inner product.
     * Symbol: {@code <#>}
     */
    NEGATIVE_INNER_PRODUCT("<#>"),

    /**
     * L1 Distance (Manhattan Distance).
     * Measures distance between two points by summing the absolute differences of their coordinates.
     * Symbol: {@code <+>}
     */
    L1_DISTANCE("<+>"),

    /**
     * Hamming Distance.
     * Measures the number of positions at which the corresponding symbols are different.
     * Typically used for binary vectors (vectors of 0s and 1s).
     * Symbol: {@code <~>}
     */
    HAMMING_DISTANCE("<~>"),

    /**
     * Jaccard Distance.
     * Measures dissimilarity between sample sets. For binary vectors, it is calculated as
     * 1 - (size of intersection / size of union).
     * Typically used for binary vectors.
     * Symbol: {@code <%>}
     */
    JACCARD_DISTANCE("<%>");

    private final String symbol;

    /**
     * Private constructor for the enum.
     *
     * @param symbol The string representation of the operator used in the query language.
     */
    DistanceOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the string symbol for the operator.
     *
     * <p>For example, {@code DistanceOperator.L2_DISTANCE.toString()} will return "<->".
     * This is useful when generating SQL strings from the AST.
     *
     * @return The operator's symbol as a string.
     */
    @Override
    public String toString() {
        return this.symbol;
    }
}
