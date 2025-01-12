package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import cryptography.ECDSA;
import managers.UserManager;
import managers.PublicKeyManager;
import managers.TermsManager;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class UserHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        if(!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            response.put("error", "Request method not allow");
            sendResponse(405, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        JSONObject request = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
        if(!request.has("email") || !request.has("password") || !request.has("name") || !request.has("signature")) {
            response.put("error", "Missing parameters!");
            sendResponse(409, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        String email = request.getString("email");
        String encodedSignature = request.getString("signature");
        String terms = TermsManager.getConsentForm();
        if(!ECDSA.verify(terms, encodedSignature, PublicKeyManager.get(email))) {
            response.put("error", "Invalid Signature");
            sendResponse(400, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        String password = request.getString("password");
        String name = request.getString("name");

        if(!UserManager.register(email, name, password)) {
            response.put("error", "Upload user to DB error");
            sendResponse(500, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        sendResponse(200, new byte[0], exchange);
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
