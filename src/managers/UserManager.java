package managers;

import cryptography.Hash;
import database.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserManager {
    private static final String type = "painter";

    public static boolean register(String email, String name, String password) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        if(c.equals(null)) {
            return false;
        }

        String query = "INSERT INTO User(email, password, type, name) VALUES (?, ?, ?, ?)";
        String hashedPassword = Hash.sha256(password);

        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, hashedPassword);
            statement.setString(3, type);
            statement.setString(4, name);
            int rows = statement.executeUpdate();

            con.closeConnection();
            if (rows == 0) {
                System.out.println("Upload user to DB error");
                return false;
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Prepare SQL statement error");
            return false;
        }
    }

    public static boolean register(String email, String name, String password, String type) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        if(c.equals(null)) {
            return false;
        }

        String query = "INSERT INTO User(email, name, password, type) VALUES (?, ?, ?, ?)";
        String hashedPassword = Hash.sha256(password);

        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, name);
            statement.setString(3, hashedPassword);
            statement.setString(4, type);
            int rows = statement.executeUpdate();

            con.closeConnection();
            if (rows == 0) {
                System.out.println("Upload user to DB error");
                return false;
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Prepare SQL statement error");
            return false;
        }
    }
}
