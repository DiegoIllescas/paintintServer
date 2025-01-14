package handlers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.AuthManager;
import managers.PublicKeyManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class JudgeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        if(!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            response.put("error", "Request method not allow");
            sendResponse(405, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }
        System.out.println("Get judges public keys request...");
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

        Map<Integer, String> keys = PublicKeyManager.getJuryKeys();
        if(keys.equals(null)) {
            response.put("error", "Getting judges public keys error");
            sendResponse(500, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        JSONArray judgeKeys = new JSONArray();
        for(int key: keys.keySet()) {
            JSONObject judge = new JSONObject();
            judge.put("id", key);
            judge.put("publicKey", keys.get(key));
            judgeKeys.put(judge);
        }

        response.put("keys", judgeKeys);
        System.out.println("Response: " + response.toString());
        sendResponse(200, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
        return;
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
