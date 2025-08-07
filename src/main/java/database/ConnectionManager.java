package database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {

    private static volatile Connection connection;
    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            synchronized (ConnectionManager.class) {
                if (connection == null || connection.isClosed()) {
                    try {
                        Properties props = new Properties();
                        try (InputStream input = ConnectionManager.class.getClassLoader().getResourceAsStream("db.properties")) {
                            if (input == null) {
                                System.out.println("Sorry, unable to find db.properties");
                                throw new RuntimeException("db.properties not found in classpath");
                            }
                            props.load(input);
                        }

                        Class.forName("org.postgresql.Driver");

                        connection = DriverManager.getConnection(
                                props.getProperty("db.url"),
                                props.getProperty("db.user"),
                                props.getProperty("db.password")
                        );
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException("Failed to load database configuration", e);
                    }
                }
            }
        }
        return connection;
    }

    public static void closeConnection() {
        synchronized (ConnectionManager.class) {
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing the database connection.");
                    e.printStackTrace();
                } finally {
                    connection = null; // 显式设置为null
                }
            }
        }
    }
}
