package database;

import com.pgvector.PGvector;
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

        System.out.println("Executing SQL: " + sql); // 打印日志，方便调试

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // 1. 获取数据库连接
            conn = ConnectionManager.getConnection();
            stmt = conn.createStatement();

            // 2. 执行查询
            rs = stmt.executeQuery(sql);

            // 3. 将 ResultSet 转换为 Table 对象
            return convertResultSetToTable(rs);

        } catch (SQLException e) {
            System.err.println("SQL execution failed for query: " + sql);
            // 抛出一个自定义的运行时异常，可以包含失败的SQL，方便上层调试
            throw new RuntimeException("Database query execution failed", e);
        } finally {
            // 4. 在 finally 块中确保资源被关闭
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // 注意：我们不在这里关闭连接，因为连接是共享的。
                // 连接将在程序退出时由 ConnectionManager.closeConnection() 统一关闭。
            } catch (SQLException e) {
                System.err.println("Error closing statement or result set.");
                e.printStackTrace();
            }
        }
    }

    private Table convertResultSetToTable(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        List<String> columnNames = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Object obj = rs.getObject(i);

                // --- FIX STARTS HERE: Data Normalization ---
                if (obj instanceof PGvector) {
                    row.add(new Vector(((PGvector) obj).toArray()));
                } else if (obj instanceof Number) {
                    // Normalize all integer-like numbers to Long for consistent comparison.
                    if (obj instanceof Double || obj instanceof Float || obj instanceof java.math.BigDecimal) {
                        row.add(((Number) obj).doubleValue());
                    } else {
                        row.add(((Number) obj).longValue());
                    }
                } else {
                    row.add(obj);
                }
                // --- FIX ENDS HERE ---
            }
            rows.add(row);
        }

        return new Table(null, columnNames, rows);
    }

}