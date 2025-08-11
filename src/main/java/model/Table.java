package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Table {

    private final String name;
    private final List<Column> columns;
    private final List<List<Object>> rows;
    // For faster column lookup by name
    private final Map<String, Column> columnMap;

    /**
     * Inner static class to represent a table column with a name and a type.
     */
    public static class Column {
        private final String name;
        private final String type; // Using String for type, e.g., "vector", "text", "integer"

        public Column(String name, String type) {
            this.name = Objects.requireNonNull(name, "Column name cannot be null.");
            this.type = Objects.requireNonNull(type, "Column type cannot be null.");
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public boolean isVector() {
            return "vector".equalsIgnoreCase(this.type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Column column = (Column) o;
            return name.equals(column.name) && type.equals(column.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
    }

    /**
     * Constructor for the Table class using the new Column structure.
     * @param name The name of the table.
     * @param columns A list of Column objects defining the schema.
     * @param rows The data rows.
     */
    public Table(String name, List<Column> columns, List<List<Object>> rows) {
        this.name = name;
        this.columns = List.copyOf(columns);
        this.rows = List.copyOf(rows);

        // Create a map for efficient column lookup by name
        this.columnMap = new HashMap<>();
        for (Column col : columns) {
            this.columnMap.put(col.getName(), col);
        }
    }

    // --- NEW and MODIFIED Getters and Helpers ---

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    /**
     * A helper to get a column object by its name.
     * @param columnName The name of the column to find.
     * @return An Optional containing the Column if found, otherwise empty.
     */
    public Optional<Column> getColumn(String columnName) {
        return Optional.ofNullable(this.columnMap.get(columnName));
    }

    /**
     * A convenience method to get a list of just the column names.
     * @return A list of column name strings.
     */
    public List<String> getColumnNames() {
        return this.columns.stream()
                .map(Column::getName)
                .collect(Collectors.toList());
    }

    // --- Unchanged Getters ---

    public List<List<Object>> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columns.size();
    }

    // --- equals and its helpers, now updated to use the new structure ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table other = (Table) o;

        if (this.getRowCount() != other.getRowCount() || this.getColumnCount() != other.getColumnCount()) {
            return false;
        }

        // Compare the set of columns (name and type). Order doesn't matter.
        if (!new HashSet<>(this.columns).equals(new HashSet<>(other.columns))) {
            return false;
        }

        boolean[] otherRowsMatched = new boolean[other.getRowCount()];
        for (List<Object> thisRow : this.getRows()) {
            boolean foundMatchForThisRow = false;
            for (int i = 0; i < other.getRowCount(); i++) {
                if (otherRowsMatched[i]) continue;
                List<Object> otherRow = other.getRows().get(i);
                // Pass the full Column list to the helper
                if (rowsAreEquivalent(thisRow, this.columns, otherRow, other.columns)) {
                    otherRowsMatched[i] = true;
                    foundMatchForThisRow = true;
                    break;
                }
            }
            if (!foundMatchForThisRow) {
                return false;
            }
        }
        return true;
    }

    private boolean rowsAreEquivalent(List<Object> row1, List<Column> cols1, List<Object> row2, List<Column> cols2) {
        if (row1.size() != row2.size()) return false;

        Map<String, Object> map1 = new HashMap<>();
        for (int i = 0; i < cols1.size(); i++) map1.put(cols1.get(i).getName(), row1.get(i));

        Map<String, Object> map2 = new HashMap<>();
        for (int i = 0; i < cols2.size(); i++) map2.put(cols2.get(i).getName(), row2.get(i));

        if (!map1.keySet().equals(map2.keySet())) return false;

        for (String key : map1.keySet()) {
            Object val1 = map1.get(key);
            Object val2 = map2.get(key);

            if (val1 instanceof Vector && val2 instanceof Vector) {
                if (!vectorEqualsWithTolerance((Vector) val1, (Vector) val2, 1e-5f)) return false;
            } else if (val1 instanceof Number && val2 instanceof Number) {
                Number n1 = (Number) val1; Number n2 = (Number) val2;
                if (isFloatingPoint(n1) || isFloatingPoint(n2)) {
                    if (Math.abs(n1.doubleValue() - n2.doubleValue()) > 1e-9) return false;
                } else {
                    if (n1.longValue() != n2.longValue()) return false;
                }
            } else if (!Objects.equals(val1, val2)) {
                return false;
            }
        }
        return true;
    }

    private boolean isFloatingPoint(Number n) {
        return n instanceof Double || n instanceof Float || n instanceof java.math.BigDecimal;
    }

    private boolean vectorEqualsWithTolerance(Vector v1, Vector v2, float tolerance) {
        if (v1 == null || v2 == null || v1.getDimensions() != v2.getDimensions()) return false;
        float[] data1 = v1.getData();
        float[] data2 = v2.getData();
        for (int i = 0; i < data1.length; i++) {
            if (Math.abs(data1[i] - data2[i]) > tolerance) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        // hashCode should now depend on the set of Columns, not just names.
        return Objects.hash(new HashSet<>(this.columns), getRowCount());
    }

    @Override
    public String toString() {
        List<String> colNames = getColumnNames(); // Use the helper to get names
        if (rows.isEmpty()) {
            return String.join("\t|\t", colNames) + "\n(No rows)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\t|\t", colNames)).append("\n");
        sb.append("-".repeat(colNames.size() * 10)).append("\n");
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
