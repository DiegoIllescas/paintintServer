package managers;

import database.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<Map<String, String>> getBlindedEvaluations() {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();;

        if(c.equals(null)) {
            return null;
        }

        String query = "SELECT id, blind_message FROM Evaluation WHERE signature is null";

        try{
            PreparedStatement statement = c.prepareStatement(query);
            ResultSet result = statement.executeQuery();

            List<Map<String, String>> blindedEvaluations = new ArrayList<>();
            while (result.next()) {
                Map<String, String> blindEvaluation = new HashMap<>();
                blindEvaluation.put("id", String.valueOf(result.getInt("id")));
                blindEvaluation.put("blind_message", result.getString("blind_message"));
                blindedEvaluations.add(blindEvaluation);
            }

            con.closeConnection();
            return blindedEvaluations;
        } catch (SQLException e) {
            System.out.println("Getting Blinded Evaluation from DB error");
            return null;
        }
    }

    public static String getEvaluationById(int id) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();;

        if(c.equals(null)) {
            return "Error";
        }

        String query = "SELECT painting_id, stars, comments FROM Evaluation WHERE id = ?";

        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                System.out.println("Cannot found evaluation error");
                return "Error";
            }

            String evaluation = String.format("%d:%d:%s", result.getInt("painting_id"), result.getInt("stars"), result.getString("comments"));
            con.closeConnection();
            return evaluation;
        } catch (SQLException e) {
            System.out.println("Getting Evaluation by Id from DB error");
            return "Error";
        }
    }

    public static String getInvById(int evaluationId) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();;

        if(c.equals(null)) {
            return "Error";
        }

        String query = "SELECT inv FROM Evaluation WHERE id = ?";

        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setInt(1, evaluationId);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                System.out.println("Cannot found evaluation error");
                return "Error";
            }

            String inv = result.getString("inv");
            con.closeConnection();
            return inv;
        } catch (SQLException e) {
            System.out.println("Getting Evaluation by Id from DB error");
            return "Error";
        }
    }

    public static boolean saveSignature(int evaluationId, String signature) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();;

        if(c.equals(null)) {
            return false;
        }

        String query = "UPDATE Evaluation SET signature = ? WHERE id = ?";

        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setString(1, signature);
            statement.setInt(2, evaluationId);
            int rows = statement.executeUpdate();

            con.closeConnection();
            if(rows == 0) {
                return false;
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Update signature to DB error");
            return false;
        }
    }
}
