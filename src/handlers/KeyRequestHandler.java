package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.PublicKeyManager;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class KeyRequestHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        if(!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            response.put("error","Request method not allow.");
            sendResponse(405, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        JSONObject request = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
        if(!request.has("email") || !request.has("publicKey")) {
            response.put("error", "Missing parameters!");
            sendResponse(400, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        String email = request.getString("email");
        String publicKeyBase64 = request.getString("publicKey");

        if(!PublicKeyManager.save(email, publicKeyBase64)) {
            response.put("error", "Upload public key to DB error");
            sendResponse(500, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        sendResponse(200, new byte[0], exchange);
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
