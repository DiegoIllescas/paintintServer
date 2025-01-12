package managers;

import database.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EvaluationManager {

    public static boolean saveEvaluation(int paintingId, int judgeId, int stars, String coments, String blindMessage, String inv) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();

        if(c.equals(null)) {
            return false;
        }

        String query = "INSERT INTO Evaluation (judge_id, painting_id, stars, comments, blind_message, inv) VALUES (?, ?, ? ,?, ?, ?)";
        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setInt(1, judgeId);
            statement.setInt(2, paintingId);
            statement.setInt(3, stars);
            statement.setString(4, coments);
            statement.setString(5, blindMessage);
            statement.setString(6, inv);
            int rows = statement.executeUpdate();

            if(rows == 0) {
                return false;
            }
            con.closeConnection();
            return true;
        } catch (SQLException e) {
            System.out.println("Upload painting evaluation to DB error");
            return false;
        }
    }
}
