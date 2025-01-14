package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.UserManager;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AdminHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        if(!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            response.put("error", "Request method not allow");
            sendResponse(405, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        JSONObject request = new JSONObject(new String(exchange.getRequestBody().readAllBytes()));
        System.out.println("Register new judge/chairman request: " + request.toString());
        if(!request.has("type") || !request.has("email") || !request.has("password") || !request.has("name")) {
            response.put("error", "Missing parameters!");
            sendResponse(400, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        String type = request.getString("type");
        String email = request.getString("email");
        String password = request.getString("password");
        String name = request.getString("name");

        if(!type.equalsIgnoreCase("chairman") && !type.equalsIgnoreCase("judge")) {
            response.put("error", "Invalid roles!");
            sendResponse(400, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
        }

        if(!UserManager.register(email, name, password, type)) {
            response.put("error", "Upload user to BD error");
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
