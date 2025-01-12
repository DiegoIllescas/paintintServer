import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final String KEY_ENDPOINT = "/key";
    private static final String CONSENT_ENDPOINT = "/consent";
    private static final String USER_ENDPOINT = "/user";
    private static final String JUDGE_ENDPOINT = "/judge";
    private static final String PAINTING_ENDPOINT = "/painting";
    private static final String EVALUATION_ENDPOINT = "/evaluation";
    private final int port;

    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8000;
        if(args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }
        Main mainServer = new Main(serverPort);
        mainServer.start();

        System.out.println("Server listening on port " + serverPort);
    }

    public Main(int port) {
        this.port = port;
    }

    private void start() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext keyContext = server.createContext(KEY_ENDPOINT);
        HttpContext consentContext = server.createContext(CONSENT_ENDPOINT);
        HttpContext userContext = server.createContext(USER_ENDPOINT);
        HttpContext judgeContext = server.createContext(JUDGE_ENDPOINT);
        HttpContext paintingContext = server.createContext(PAINTING_ENDPOINT);
        HttpContext evaluationContext = server.createContext(EVALUATION_ENDPOINT);

        keyContext.setHandler(new KeyRequestHandler());
        consentContext.setHandler(new TermsHandler());
        userContext.setHandler(new UserHandler());
        judgeContext.setHandler(new JudgeHandler());
        paintingContext.setHandler(new PaintingHandler());
        evaluationContext.setHandler(new EvaluationHandler());

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }
}