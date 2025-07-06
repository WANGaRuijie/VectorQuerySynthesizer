package queryast.ast.enums;

public enum DataType {

    INT("int"), TEXT("text"), BOOLEAN("boolean"), DATE("date"), VECTOR("vector");
    private final String sqlName;

    /**
     * Constructor for DataType enum.
     * @param sqlName The String representation of the SQL data type.
     */
    DataType(String sqlName) {
        this.sqlName = sqlName;
    }

    /**
     * Returns the lowercase string representation of the data type.
     *
     * @return The name of the data type (e.g., "int", "text").
     */
    @Override
    public String toString() {
        return this.sqlName;
    }

    /**
     * A utility method to find a DataType from a given string, ignoring case.
     *
     * @param text The string representation of the data type (e.g., "INT", "text").
     * @return The corresponding DataType enum constant.
     * @throws IllegalArgumentException if no matching data type is found.
     */
    public static DataType fromString(String text) {
        for (DataType dt : DataType.values()) {
            if (dt.sqlName.equalsIgnoreCase(text)) {
                return dt;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found in DataType enum");
    }
}
