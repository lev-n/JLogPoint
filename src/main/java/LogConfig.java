import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LogConfig {
    private String classPattern = ".*"; // Default: all classes
    private String methodPattern = ".*"; // Default: all methods
    private String logLevel = "INFO";
    private int apiPort = 8080; // HTTP API port
    
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
            this.classPattern = props.getProperty("classPattern", this.classPattern);
            this.methodPattern = props.getProperty("methodPattern", this.methodPattern);
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
                        this.classPattern = kv[1].trim();
                        break;
                    case "methodPattern":
                        this.methodPattern = kv[1].trim();
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
}
