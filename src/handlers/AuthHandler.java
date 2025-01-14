package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.AuthManager;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AuthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        if(!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            response.put("error", "Request method not allow");
            sendResponse(405, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        JSONObject request = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
        System.out.println("auth request: " + request.toString());
        if(!request.has("email") || !request.has("password")) {
            response.put("error", "Missing parameters!");
            sendResponse(400, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        String email = request.getString("email");
        String password = request.getString("password");

        String data[] = AuthManager.auth(email, password);
        if(data.length == 0 ) {
            response.put("error", "User not found or incorrect password");
            sendResponse(404, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        response.put("token", data[0]);
        response.put("type", data[1]);
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
