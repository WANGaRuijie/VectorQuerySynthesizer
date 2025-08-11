package database;

import com.pgvector.PGvector; // Correct import for the library
import model.Table;
import model.Vector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QueryExecutor {

    /**
     * Executes a SQL query and returns the results as a Table object.
     * @param sql SQL query string to be executed.
     * @return a table of the result.
     */
    public Table executeQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL query cannot be null or empty.");
        }

        System.out.println("Executing SQL: " + sql);

        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // The main logic is now to convert the ResultSet to our rich Table object.
            // Using try-with-resources ensures all resources (conn, stmt, rs) are closed automatically.
            // Note: We get a new connection each time for simplicity in this model,
            // which is fine for a synthesizer but different from the singleton ConnectionManager.
            // Let's stick to the ConnectionManager for consistency.
            // Reverting the try-with-resources for the connection.
        } catch (SQLException e) {
            // Exception handling needs to be outside the try-with-resources if conn is not declared there
        }

        // Sticking to the original structure for consistency with ConnectionManager
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionManager.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            // This now returns a Table object with full schema information.
            return convertResultSetToTable(rs);

        } catch (SQLException e) {
            System.err.println("SQL execution failed for query: " + sql);
            throw new RuntimeException("Database query execution failed", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing statement or result set.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Converts a JDBC ResultSet to our custom Table object, including rich column metadata.
     * @param rs The ResultSet from the database query.
     * @return A new Table object populated with schema and data.
     * @throws SQLException
     */
    private Table convertResultSetToTable(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 1. Extract schema information (name AND type) into a list of Column objects.
        List<Table.Column> schema = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            String columnTypeName = metaData.getColumnTypeName(i); // e.g., "varchar", "int4", "vector"

            // We can simplify the DB type to our internal type system.
            String internalType = convertDbTypeNameToInternalType(columnTypeName);
            schema.add(new Table.Column(columnName, internalType));
        }

        // 2. Extract all row data, normalizing types as we go.
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Object obj = rs.getObject(i);

                if (obj instanceof PGvector) {
                    row.add(new Vector(((PGvector) obj).toArray()));
                } else if (obj instanceof Number) {
                    if (isFloatingPoint((Number) obj)) {
                        row.add(((Number) obj).doubleValue());
                    } else {
                        row.add(((Number) obj).longValue());
                    }
                } else {
                    row.add(obj);
                }
            }
            rows.add(row);
        }

        // 3. Create and return the new Table object using the updated constructor.
        // The table name is not known from a ResultSet, so we can pass null or a generic name.
        return new Table("result_table", schema, rows);
    }

    /**
     * A helper to convert database-specific type names to a simplified, internal representation.
     * @param dbTypeName The type name from ResultSetMetaData (e.g., "int4", "varchar").
     * @return Our simplified internal type name (e.g., "long", "text", "vector").
     */
    private String convertDbTypeNameToInternalType(String dbTypeName) {
        // This mapping can be expanded as needed.
        switch (dbTypeName.toLowerCase()) {
            case "vector":
                return "vector";
            case "text":
            case "varchar":
            case "char":
                return "text";
            case "int4": // PostgreSQL's name for integer
            case "serial":
            case "int8": // PostgreSQL's name for bigint
            case "bigserial":
                return "long";
            case "float4": // real
            case "float8": // double precision
            case "numeric":
                return "double";
            case "bool":
                return "boolean";
            default:
                return "unknown"; // Fallback for unhandled types
        }
    }

    /**
     * Helper to check if a Number object represents a floating-point value.
     */
    private boolean isFloatingPoint(Number n) {
        return n instanceof Double || n instanceof Float || n instanceof java.math.BigDecimal;
    }
}