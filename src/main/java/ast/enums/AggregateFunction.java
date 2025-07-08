package ast.enums;

public enum AggregateFunction {

    SUM("SUM"), COUNT("COUNT"), AVG("AVG"), MIN("MIN"), MAX("MAX");
    private final String sqlName;

    AggregateFunction(String sqlName) {
        this.sqlName = sqlName;
    }

    /**
     * Returns the SQL name of the aggregate function.
     * @return The SQL name of the aggregate function (e.g., "SUM", "COUNT").
     */
    @Override
    public String toString() {
        return this.sqlName;
    }
}
