package model;


import java.util.*;

public class Table {

    private final List<String> columnNames;
    private final List<List<Object>> rows;
    private final Set<Integer> rowHashSet;

    /**
     * Constructs a Table with the specified column names and rows.
     * The column names and rows are stored in an immutable format to ensure
     * that the Table object is effectively immutable after construction.
     *
     * @param columnNames The names of the columns in the table. Cannot be null or empty.
     * @param rows        The data rows of the table. Each row is a list of objects.
     *                    Cannot be null, but can be empty.
     */
    public Table(List<String> columnNames, List<List<Object>> rows) {
        this.columnNames = Collections.unmodifiableList(new ArrayList<>(columnNames));
        this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
        this.rowHashSet = new HashSet<>();
        for (List<Object> row : this.rows) {
            this.rowHashSet.add(Objects.hash(row.toArray()));
        }
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    /**
     * Redefines the equals method to compare two Table objects.
     * Two Table objects are considered equal if they meet the following criteria:
     * 1. Dimensions (row count and column count) are the same;
     * 2. Row column names are the same (ignoring order);
     * 3. Row data is the same (ignoring order).
     *
     * @param o Another object to compare with this Table.
     * @return true if the two tables are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table other = (Table) o;

        // Examine the dimensions:
        if (this.getRowCount() != other.getRowCount() || this.getColumnCount() != other.getColumnCount()) {
            return false;
        }

        // Examine the column names:
        if (!new HashSet<>(this.columnNames).equals(new HashSet<>(other.columnNames))) {
            return false;
        }

        // Examine the row data:
        return this.rowHashSet.equals(other.rowHashSet);
    }

    /**
     * Redefines the hashCode method to ensure that it is consistent with the equals method.
     */
    @Override
    public int hashCode() {
        return Objects.hash(new HashSet<>(columnNames), rowHashSet);
    }

    /**
     * Provides a string representation of the Table object.
     * The format is:
     * - First line: Column names separated by tab characters.
     * - Second line: A separator line made of dashes.
     * - Subsequent lines: Each row's data, with cells separated by tab characters.
     * If there are no rows, it will indicate "(No rows)" after the column names.
     *
     * @return A formatted string representing the table.
     */
    @Override
    public String toString() {
        if (rows.isEmpty()) {
            return String.join("\t|\t", columnNames) + "\n(No rows)";
        }

        StringBuilder sb = new StringBuilder();
        // 打印表头
        sb.append(String.join("\t|\t", columnNames)).append("\n");
        sb.append("-".repeat(columnNames.size() * 10)).append("\n"); // 分隔线

        // 打印行
        for (List<Object> row : rows) {
            List<String> rowStrings = new ArrayList<>();
            for (Object cell : row) {
                rowStrings.add(cell == null ? "NULL" : cell.toString());
            }
            sb.append(String.join("\t|\t", rowStrings)).append("\n");
        }
        return sb.toString();
    }
}
