package managers;

import database.SQLConnection;

import java.io.*;
import java.sql.*;
import java.util.*;

public class PaintingManager {

    private static final String path = System.getProperty("user.dir") + "/resources/dat/";
    public static int savePainting(String painting, String iv, String title, String description, int id) {
        String paintingPath = savePaintingFile(painting, id);
        if(paintingPath.equals("Error")) {
            return 0;
        }

        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        if(c.equals(null)) {
            return 0;
        }

        String query = "INSERT INTO Painting (local_url, iv, title, description, user_id) VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, paintingPath);
            statement.setString(2, iv);
            statement.setString(3, title);
            statement.setString(4, description);
            statement.setInt(5, id);
            int rows = statement.executeUpdate();

            if(rows == 0) {
                con.closeConnection();
                System.out.println("Failed in load the painting to DB");
                return 0;
            }

            ResultSet result = statement.getGeneratedKeys();
            result.next();
            int response = result.getInt(1);
            con.closeConnection();
            return response;
        } catch (SQLException e) {
            System.out.println("Upload painting to DB error");
            return 0;
        }
    }
    public static List<Map<String, String>> getNotEvaluatedPainting(int judgeId) {
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        List<Map<String, String>> paintings = new ArrayList<>();

        if(c.equals(null)) {
            return paintings;
        }

        String query = "SELECT p.id as id, p.title as title, p.description as description, p.local_url as local_url, p.iv as iv FROM Painting p LEFT JOIN Evaluation e ON p.id = e.painting_id AND e.judge_id = ? WHERE e.painting_id is null";


        try {
            PreparedStatement statement = c.prepareStatement(query);
            statement.setInt(1, judgeId);
            ResultSet result = statement.executeQuery();


            while (result.next()) {
                Map<String, String> painting = new HashMap<>();
                painting.put("id", String.valueOf(result.getInt("id")));
                painting.put("title", result.getString("title"));
                painting.put("description", result.getString("description"));
                painting.put("painting", loadPaintingFromFile(result.getString("local_url")));
                painting.put("iv", result.getString("iv"));
                paintings.add(painting);
            }

            con.closeConnection();
            return paintings;
        } catch (SQLException e) {
            System.out.println("Getting painting from DB error");
            return paintings;
        }

    }

    private static String savePaintingFile(String painting, int id) {
        String filename = String.format("%d_%d.dat", System.currentTimeMillis(), id);
        String filepath = path + filename;

        byte[] paintingBytes = Base64.getDecoder().decode(painting);
        try (FileOutputStream fos = new FileOutputStream(filepath)) {
            fos.write(paintingBytes);
            return filename;
        } catch (IOException e) {
            return "Error";
        }
    }
    private static String loadPaintingFromFile(String url) {
        try {
            String filepath = path + url;
            File file = new File(filepath);
            FileInputStream fis = new FileInputStream(file);
            byte[] encryptedPainting = new byte[(int) file.length()];
            fis.read(encryptedPainting);
            fis.close();

            return Base64.getEncoder().encodeToString(encryptedPainting);
        } catch (IOException e) {
            System.out.println("Painting file not found");
            return "Error";
        }

    }
}
