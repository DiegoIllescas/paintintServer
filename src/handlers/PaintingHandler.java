package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.AESKeyManager;
import managers.AuthManager;
import managers.PaintingManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class PaintingHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject response = new JSONObject();
        if(!exchange.getRequestMethod().equalsIgnoreCase("post") && !exchange.getRequestMethod().equalsIgnoreCase("get")) {
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
            if(!request.has("painting") || !request.has("iv") || !request.has("encrypted_keys") || !request.has("title") || !request.has("description")) {
                response.put("error", "Missing parameters!");
                sendResponse(400, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
                return;
            }

            String painting = request.getString("painting");
            String iv = request.getString("iv");
            JSONObject keysObject = request.getJSONObject("keys");
            String title = request.getString("title");
            String description = request.getString("description");

            int paintingId = PaintingManager.savePainting(painting, iv, title, description, id);
            if(paintingId == 0) {
                response.put("error", "Upload painting to DB error");
                sendResponse(500, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
                return;
            }

            for (String key : keysObject.keySet()) {
                AESKeyManager.saveAesKeys(paintingId, Integer.parseInt(key), keysObject.getString(key));
            }

            sendResponse(200, new byte[0], exchange);
            return;
        }

        if(exchange.getRequestMethod().equalsIgnoreCase("get")) {
            List<Map<String, String>> paintings = PaintingManager.getNotEvaluatedPainting(id);
            for(Map<String, String> painting : paintings) {
                painting.put("aesKey", AESKeyManager.getKey(Integer.parseInt(painting.get("id")), id));
            }

            JSONArray paintingArray = new JSONArray();
            for(Map<String, String> painting : paintings) {
                JSONObject paintingObject = new JSONObject();
                for(String key: painting.keySet()) {
                    paintingObject.put(key, painting.get(key));
                }
                paintingArray.put(paintingObject);
            }

            response.put("paintings", paintingArray);
            sendResponse(200, response.toString().getBytes(StandardCharsets.UTF_8), exchange);
            return;
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
