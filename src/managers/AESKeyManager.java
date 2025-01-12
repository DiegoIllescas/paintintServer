package managers;

import database.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AESKeyManager {
    public static void saveAesKeys(int paintingId, int judgeId, String key) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();

        if(c.equals(null)) {
            return;
        }

        String query = "INSERT INTO AESKey (painting_id, judge_id, aeskey) VALUES (?, ? ,?)";
        try{
            PreparedStatement statement = c.prepareStatement(query);
            statement.setInt(1, paintingId);
            statement.setInt(2, judgeId);
            statement.setString(3, key);
            int rows = statement.executeUpdate();

            con.closeConnection();
            return;
        } catch (SQLException e) {
            return;
        }
    }

    public static String getKey(int paintingId, int judgeId) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();;

        if(c.equals(null)) {
            return "Error";
        }

        String query = "SELECT aeskey FROM AESKey WHERE painting_id = ? AND judge_id = ?";

        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setInt(1, paintingId);
            statement.setInt(2, judgeId);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                return "Error";
            }

            String aesKey = result.getString("aeskey");
            con.closeConnection();

            return aesKey;
        } catch (SQLException e) {
            return "Error";
        }
    }
}
