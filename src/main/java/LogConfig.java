import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LogConfig {
    // Match all classes and methods by default so that log points added
    // via the HTTP API are properly instrumented even when no patterns
    // are supplied through agent arguments.
    private String classPattern = ".*"; // Default: all classes
    private String methodPattern = ".*"; // Default: all methods
    private String logLevel = "INFO";
    private int apiPort = 8081; // HTTP API port
    
    public static LogConfig load(String agentArgs) {
        LogConfig config = new LogConfig();
        
        if (agentArgs != null && !agentArgs.trim().isEmpty()) {
            // Parse agent arguments - format: config=/path/to/config.properties
            if (agentArgs.startsWith("config=")) {
                String configPath = agentArgs.substring("config=".length());
                config.loadFromFile(configPath);
            } else {
                // Parse inline args - format: classPattern=com.example.*,methodPattern=get.*
                config.parseInlineArgs(agentArgs);
            }
        }
        
        return config;
    }
    
    private void loadFromFile(String configPath) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
            this.classPattern = normalizePattern(props.getProperty("classPattern", this.classPattern));
            this.methodPattern = normalizePattern(props.getProperty("methodPattern", this.methodPattern));
            this.logLevel = props.getProperty("logLevel", this.logLevel);
            this.apiPort = Integer.parseInt(props.getProperty("apiPort", String.valueOf(this.apiPort)));
        } catch (IOException e) {
            System.err.println("Failed to load config from " + configPath + ": " + e.getMessage());
        }
    }
    
    private void parseInlineArgs(String args) {
        String[] pairs = args.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                switch (kv[0].trim()) {
                    case "classPattern":
                        this.classPattern = normalizePattern(kv[1].trim());
                        break;
                    case "methodPattern":
                        this.methodPattern = normalizePattern(kv[1].trim());
                        break;
                    case "logLevel":
                        this.logLevel = kv[1].trim();
                        break;
                    case "apiPort":
                        this.apiPort = Integer.parseInt(kv[1].trim());
                        break;
                }
            }
        }
    }
    
    public String getClassPattern() { return classPattern; }
    public String getMethodPattern() { return methodPattern; }
    public String getLogLevel() { return logLevel; }
    public int getApiPort() { return apiPort; }

    /**
     * Return a regex pattern that matches all when the provided value is blank.
     */
    private String normalizePattern(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ".*";
        }
        return value.trim();
    }
}
