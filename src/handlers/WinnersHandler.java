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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WinnersHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        if(!exchange.getRequestMethod().equalsIgnoreCase("get")) {
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

        List<Map<String, String>> evaluations = EvaluationManager.getSignedEvaluations();
        Map<Integer, Integer> starCount = new HashMap<>();
        String chairmanPKBase64 = PublicKeyManager.getChairmanKey();
        PublicKey chairmanPK = RSA.getPublicKeyFromBase64(chairmanPKBase64);

        for(Map<String, String> evaluation: evaluations) {
            int stars = Integer.parseInt(evaluation.get("stars"));
            int paintingId = Integer.parseInt(evaluation.get("painting_id"));
            String message = evaluation.get("message");
            String signatureBase64 = evaluation.get("signature");
            try {
                if(BlindSignature.RSASSA_PSS_Verify((RSAPublicKey) chairmanPK, message, signatureBase64).equals("consistent")) {
                    if(starCount.containsKey(paintingId)) {
                        starCount.replace(paintingId, starCount.get(paintingId) + stars);
                    }else{
                        starCount.put(paintingId, stars);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error setting EMSA parameters");
            }
        }
        if(starCount.entrySet().size() <= 3) {
            response.put("results", starCount);
            sendResponse(200, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        //ordenar
        System.out.println(response.toString());
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
