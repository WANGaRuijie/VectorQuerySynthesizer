package ast.enums;

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
     * @return The name of the data type (e.g., "int", "text").
     */
    @Override
    public String toString() {
        return this.sqlName;
    }

}
