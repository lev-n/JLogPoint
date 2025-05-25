import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.instrument.Instrumentation;

public class LogInjectionAgent {
    private static LogPointManager logPointManager;
    private static HttpApiServer apiServer;
    
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Starting Log Injection Agent...");
        
        // Load configuration
        LogConfig config = LogConfig.load(agentArgs);
        
        // Initialize log point manager
        logPointManager = new LogPointManager();
        
        // Start HTTP API server
        apiServer = new HttpApiServer(logPointManager, config.getApiPort());
        apiServer.start();
        
        new AgentBuilder.Default()
            .type(ElementMatchers.nameMatches(config.getClassPattern()))
            .transform((builder, typeDescription, classLoader, module) ->
                builder.method(ElementMatchers.nameMatches(config.getMethodPattern()))
                    .intercept(MethodDelegation.to(LogInterceptor.class))
            )
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .installOn(inst);
            
        System.out.println("Log Injection Agent installed successfully");
        System.out.println("HTTP API available at http://localhost:" + config.getApiPort());
    }
    
    public static void agentmain(String agentArgs, Instrumentation inst) {
        // Support for attach API
        premain(agentArgs, inst);
    }
    
    public static LogPointManager getLogPointManager() {
        return logPointManager;
    }
}
