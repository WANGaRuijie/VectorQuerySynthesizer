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

    /**
     * 一个私有的辅助方法，负责将 ResultSet 转换为 Table。
     * @param rs 数据库查询返回的结果集。
     * @return 一个填充了数据的 Table 对象。
     * @throws SQLException
     */
    private Table convertResultSetToTable(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 1. 提取列名
        List<String> columnNames = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        // 2. 提取所有行数据
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Object obj = rs.getObject(i);

                // 特别处理 pgvector 类型
                if (obj instanceof PGvector) {
                    // 将 pgvector-java 的 PGvector 对象转换为我们自己的 model.Vector 对象
                    row.add(new Vector(((PGvector) obj).toArray()));
                } else {
                    row.add(obj);
                }
            }
            rows.add(row);
        }

        // 3. 创建并返回我们自己的 Table 对象
        return new Table(columnNames, rows);
    }
}