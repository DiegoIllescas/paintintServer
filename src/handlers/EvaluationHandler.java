package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import cryptography.BlindSignature;
import cryptography.RSA;
import managers.AuthManager;
import managers.EvaluationManager;
import managers.PublicKeyManager;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

public class EvaluationHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        if(!exchange.getRequestMethod().equalsIgnoreCase("post") && !exchange.getRequestMethod().equalsIgnoreCase("get") && !exchange.getRequestMethod().equalsIgnoreCase("update")) {
            response.put("error", "Request method not allow");
            sendResponse(405, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        Map<String, List<String>> headers = exchange.getRequestHeaders();
        if(!headers.containsKey("Authorization")) {
            response.put("error", "Unauthorized");
            sendResponse(401, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        //Example of Authorization header: Bearer ee121e12e12n1uf37.2312dn71he188f12fjhYI...
        String token = headers.get("Authorization").get(0).split(" ")[1];
        int id = AuthManager.validateToken(token);
        if(id == 0) {
            response.put("error", "Unauthorized");
            sendResponse(401, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        if(exchange.getRequestMethod().equalsIgnoreCase("post")) {
            JSONObject request = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
            if(!request.has("id") || !request.has("stars") || !request.has("comments")) {
                response.put("error", "Missing parameters!");
                sendResponse(400, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
                return;
            }

            int paintingId = request.getInt("id");
            int stars = request.getInt("stars");
            String comments = request.getString("comments");

            String evaluation = String.format("%d;%d;%s", paintingId, stars, comments);
            String chairmanPublicKey = PublicKeyManager.getChairmanKey();
            PublicKey chairmanKey = RSA.getPublicKeyFromBase64(chairmanPublicKey);
            if(chairmanKey.equals(null)) {
                response.put("error", "Parsing Base64 to PublicKey error");
                sendResponse(500, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
                return;
            }

            try{
                String[] blindData = BlindSignature.blind(evaluation, (RSAPublicKey) chairmanKey);
                String blindedMessage = blindData[0];
                String inv = blindData[1];

                if(!EvaluationManager.saveEvaluation(paintingId, id, stars, comments, blindedMessage, inv)) {
                    response.put("error", "Upload painting evaluation to DB error");
                    sendResponse(500, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
                    return;
                }

                sendResponse(200, new byte[0], exchange);
                return;
            } catch (Exception e) {
                response.put("error", "Blinding message error");
                sendResponse(500, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
                return;
            }

        }

        if(exchange.getRequestMethod().equalsIgnoreCase("get")) {

        }

        if(exchange.getRequestMethod().equalsIgnoreCase("update")) {

        }
    }

    private void sendResponse(int status, byte[] payload, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(status, payload.length);
        if(payload.length == 0) {
            exchange.close();
            return;
        }

        OutputStream os = exchange.getResponseBody();
        os.write(payload);
        os.flush();
        os.close();
        exchange.close();
        return;
    }
}
