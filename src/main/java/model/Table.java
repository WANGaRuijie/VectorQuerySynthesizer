package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Table {

    private final String name;
    private final List<String> columnNames;
    private final List<List<Object>> rows;

    public Table(String name, List<String> columnNames, List<List<Object>> rows) {
        this.name = name;
        this.columnNames = List.copyOf(columnNames);
        this.rows = List.copyOf(rows);
    }

    // ... [getters and other methods remain the same] ...
    public String getTableName() { return name; }
    public List<String> getColumnNames() { return columnNames; }
    public List<List<Object>> getRows() { return rows; }
    public int getRowCount() { return rows.size(); }
    public int getColumnCount() { return columnNames.size(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table other = (Table) o;

        if (this.getRowCount() != other.getRowCount() || this.getColumnCount() != other.getColumnCount()) {
            return false;
        }

        if (!new HashSet<>(this.columnNames).equals(new HashSet<>(other.columnNames))) {
            return false;
        }

        boolean[] otherRowsMatched = new boolean[other.getRowCount()];

        for (List<Object> thisRow : this.getRows()) {
            boolean foundMatchForThisRow = false;
            for (int i = 0; i < other.getRowCount(); i++) {
                if (otherRowsMatched[i]) {
                    continue;
                }
                List<Object> otherRow = other.getRows().get(i);
                if (rowsAreEquivalent(thisRow, this.columnNames, otherRow, other.columnNames)) {
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

    /**
     * Helper method with extensive debugging printouts to find the exact point of failure.
     */
    private boolean rowsAreEquivalent(List<Object> row1, List<String> cols1, List<Object> row2, List<String> cols2) {
        // This method now has detailed logging.
        Map<String, Object> map1 = new HashMap<>();
        for (int i = 0; i < cols1.size(); i++) map1.put(cols1.get(i), row1.get(i));

        Map<String, Object> map2 = new HashMap<>();
        for (int i = 0; i < cols2.size(); i++) map2.put(cols2.get(i), row2.get(i));

        for (String key : map1.keySet()) {
            Object val1 = map1.get(key);
            Object val2 = map2.get(key);

            // --- START OF DETAILED DEBUGGING BLOCK ---
            System.out.println("DEBUG_EQUALS: Comparing column '" + key + "'...");
            System.out.println("  - Value 1: " + val1 + " (Type: " + (val1 == null ? "null" : val1.getClass().getName()) + ")");
            System.out.println("  - Value 2: " + val2 + " (Type: " + (val2 == null ? "null" : val2.getClass().getName()) + ")");
            // --- END OF DETAILED DEBUGGING BLOCK ---

            boolean areEqual;
            if (val1 instanceof Vector && val2 instanceof Vector) {
                areEqual = vectorEqualsWithTolerance((Vector) val1, (Vector) val2, 1e-5f);
                System.out.println("  - Comparison Method: Vector with Tolerance -> " + areEqual);
            } else if (val1 instanceof Number && val2 instanceof Number) {
                Number n1 = (Number) val1;
                Number n2 = (Number) val2;
                if (isFloatingPoint(n1) || isFloatingPoint(n2)) {
                    areEqual = Math.abs(n1.doubleValue() - n2.doubleValue()) <= 1e-9;
                } else {
                    areEqual = n1.longValue() == n2.longValue();
                }
                System.out.println("  - Comparison Method: Normalized Number -> " + areEqual);
            } else {
                areEqual = Objects.equals(val1, val2);
                System.out.println("  - Comparison Method: Objects.equals -> " + areEqual);
            }

            if (!areEqual) {
                System.out.println("  - MISMATCH FOUND!");
                return false;
            }
        }
        System.out.println("DEBUG_EQUALS: All columns in the row matched.");
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
        return Objects.hash(new HashSet<>(columnNames), getRowCount());
    }

    @Override
    public String toString() {
        if (rows.isEmpty()) {
            return String.join("\t|\t", columnNames) + "\n(No rows)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\t|\t", columnNames)).append("\n");
        sb.append("-".repeat(columnNames.size() * 10)).append("\n");
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
