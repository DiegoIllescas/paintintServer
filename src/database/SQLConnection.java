package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    private final String host = "localhost";
    private final int port = 3306;

    private final String db = "painting";
    private final String url = String.format("jdbc:mysql://%s:%d/db", host, port, db);

    private final String user = "root";
    private final String pass = "";

    Connection connection;

    public Connection getSQLConnection() {
        try {
            connection = DriverManager.getConnection(url, user, pass);
            return connection;
        } catch (SQLException e) {
            System.out.println("DB Connection error: " + e.getMessage());
            return null;
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Error closing DB Connection: " + e.getMessage());
        }
        return;
    }
}