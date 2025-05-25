import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class HttpApiServer {
    private final LogPointManager logPointManager;
    private final int port;
    private HttpServer server;
    
    public HttpApiServer(LogPointManager logPointManager, int port) {
        this.logPointManager = logPointManager;
        this.port = port;
    }
    
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // API endpoints
            server.createContext("/api/logpoints", new LogPointsHandler());
            server.createContext("/api/logpoints/add", new AddLogPointHandler());
            server.createContext("/api/logpoints/remove", new RemoveLogPointHandler());
            server.createContext("/api/logpoints/enable", new EnableLogPointHandler());
            server.createContext("/api/logpoints/disable", new DisableLogPointHandler());
            server.createContext("/api/logpoints/clear", new ClearLogPointsHandler());
            server.createContext("/", new StatusHandler());
            
            server.setExecutor(null);
            server.start();
            
            System.out.println("HTTP API server started on port " + port);
        } catch (IOException e) {
            System.err.println("Failed to start HTTP API server: " + e.getMessage());
        }
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
    
    private class LogPointsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, LogPointManager.LogPoint> logPoints = logPointManager.getAllLogPoints();
                StringBuilder response = new StringBuilder("{\n  \"logPoints\": [\n");
                
                boolean first = true;
                for (Map.Entry<String, LogPointManager.LogPoint> entry : logPoints.entrySet()) {
                    if (!first) response.append(",\n");
                    LogPointManager.LogPoint lp = entry.getValue();
                    response.append(String.format("    {\"method\": \"%s\", \"enabled\": %s, \"createdAt\": %d}",
                        lp.getMethodKey(), lp.isEnabled(), lp.getCreatedAt()));
                    first = false;
                }
                
                response.append("\n  ]\n}");
                sendResponse(exchange, 200, response.toString());
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }
    
    private class AddLogPointHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                String methodKey = params.get("method");
                boolean enabled = Boolean.parseBoolean(params.getOrDefault("enabled", "true"));
                
                if (methodKey != null && !methodKey.trim().isEmpty()) {
                    logPointManager.addLogPoint(methodKey, enabled);
                    sendResponse(exchange, 200, "{\"status\": \"success\", \"message\": \"Log point added\"}");
                } else {
                    sendResponse(exchange, 400, "{\"status\": \"error\", \"message\": \"Missing method parameter\"}");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }
    
    private class RemoveLogPointHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                String methodKey = params.get("method");
                
                if (methodKey != null) {
                    logPointManager.removeLogPoint(methodKey);
                    sendResponse(exchange, 200, "{\"status\": \"success\", \"message\": \"Log point removed\"}");
                } else {
                    sendResponse(exchange, 400, "{\"status\": \"error\", \"message\": \"Missing method parameter\"}");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }
    
    private class EnableLogPointHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                String methodKey = params.get("method");
                
                if (methodKey != null) {
                    logPointManager.enableLogPoint(methodKey);
                    sendResponse(exchange, 200, "{\"status\": \"success\", \"message\": \"Log point enabled\"}");
                } else {
                    sendResponse(exchange, 400, "{\"status\": \"error\", \"message\": \"Missing method parameter\"}");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }
    
    private class DisableLogPointHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                String methodKey = params.get("method");
                
                if (methodKey != null) {
                    logPointManager.disableLogPoint(methodKey);
                    sendResponse(exchange, 200, "{\"status\": \"success\", \"message\": \"Log point disabled\"}");
                } else {
                    sendResponse(exchange, 400, "{\"status\": \"error\", \"message\": \"Missing method parameter\"}");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }
    
    private class ClearLogPointsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                logPointManager.clearAll();
                sendResponse(exchange, 200, "{\"status\": \"success\", \"message\": \"All log points cleared\"}");
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }
    
    private class StatusHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Log Injection Agent HTTP API\n\n" +
                "Endpoints:\n" +
                "GET  /api/logpoints - List all log points\n" +
                "POST /api/logpoints/add - Add log point (method=com.example.Class.method&enabled=true)\n" +
                "POST /api/logpoints/remove - Remove log point (method=com.example.Class.method)\n" +
                "POST /api/logpoints/enable - Enable log point (method=com.example.Class.method)\n" +
                "POST /api/logpoints/disable - Disable log point (method=com.example.Class.method)\n" +
                "POST /api/logpoints/clear - Clear all log points\n";
            
            sendResponse(exchange, 200, response);
        }
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String body = reader.readLine();
            if (body != null) {
                String[] pairs = body.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2) {
                        params.put(kv[0], java.net.URLDecoder.decode(kv[1], "UTF-8"));
                    }
                }
            }
        }
        return params;
    }
}
