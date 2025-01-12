package managers;

import database.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PublicKeyManager {

    public static boolean save(String email, String publicKey) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        if(c.equals(null)) {
            return false;
        }

        String query = "INSERT INTO PublicKey (email, publickey) VALUES (?, ?)";
        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, publicKey);
            int rows = statement.executeUpdate();
            con.closeConnection();
            if(rows == 0) {
                System.out.println("Upload public key to DB error");
                return false;
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Prepare SQL statement error");
            return false;
        }
    }

    public static String get(String email) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        if(c.equals(null)) {
            return "Error";
        }

        String query = "SELECT publickey FROM PublicKey WHERE email = ?";
        try{
            PreparedStatement statement = c.prepareStatement(query);
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                con.closeConnection();
                return "Error";
            }

            String publickey = result.getString("publickey");
            con.closeConnection();
            return publickey;
        } catch (SQLException e) {
            return "Error";
        }
    }

    public static Map<Integer, String> getJuryKeys() {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        if(c.equals(null)) {
            return null;
        }

        String query = "SELECT p.publickey, u.id FROM PublicKey p INNER JOIN User u ON p.email = u.email WHERE u.type = 'judge'";

        try {
            PreparedStatement statement = c.prepareStatement(query);
            ResultSet result = statement.executeQuery();
            Map<Integer, String> publickeys = new HashMap<>();

            while (result.next()) {
                publickeys.put(result.getInt("u.id"), result.getString("p.publickey"));
            }
            con.closeConnection();
            return publickeys;
        } catch (SQLException e) {
            System.out.println("Getting judges public keys error");
            return null;
        }
    }

    public static String getChairmanKey() {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        if(c.equals(null)) {
            return "Error";
        }

        String query = "SELECT p.publickey as publickey FROM PublicKey p INNER JOIN User u ON p.email = u.email WHERE u.type = 'chairman'";

        try {
            PreparedStatement statement = c.prepareStatement(query);
            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                return "Error";
            }
            String publickey = result.getString("publickey");
            con.closeConnection();
            return publickey;
        } catch (SQLException e) {
            System.out.println("Getting chairman public key error");
            return "Error";
        }
    }
}
