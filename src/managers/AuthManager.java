package managers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import cryptography.Hash;
import database.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AuthManager {
    private static final String secret = "24c35fceb2a0eb6e9e026b72227a15599940520bf17bab845ee09b3599341a27";

    public static String auth(String email, String password) {
        String hashedPassword = Hash.sha256(password);
        SQLConnection con = new SQLConnection();
        Connection c = con.getSQLConnection();
        if(c.equals(null)) {
            return "Error";
        }

        String query = "SELECT id, type FROM User WHERE email = ? and password = ?";
        try{
            PreparedStatement statement = c.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, hashedPassword);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                return "Not Found";
            }

            int userId = result.getInt("id");
            String type = result.getString("type");
            con.closeConnection();
            return String.format("%s_%s", generateToken(userId), type);

        } catch (SQLException e) {
            return "Error";
        }
    }

    public static int validateToken(String token) {
        DecodedJWT decodedJWT;
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaimPresence("id")
                    .withIssuer("auth0")
                    .build();
            decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim("id").asInt();
        }catch (JWTVerificationException e) {
            System.out.println("Invalid token");
            return 0;
        }
    }

    private static String generateToken(int userId) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withClaim("id", userId)
                    .withIssuer("auth0")
                    .sign(algorithm);
        }catch (JWTCreationException e) {
            return "Error";
        }
    }
}
