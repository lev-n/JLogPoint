import net.bytebuddy.implementation.bind.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

public class LogInterceptor {
    private static final Logger logger = Logger.getLogger("LogInjection");
    
    @RuntimeType
    public static Object intercept(@This Object target,
                                 @Origin Method method,
                                 @AllArguments Object[] args,
                                 @SuperCall java.util.concurrent.Callable<?> callable) throws Exception {
        
        String className = target.getClass().getName();
        String methodName = method.getName();
        String methodKey = className + "." + methodName;
        
        // Check if this method has an active log point
        LogPointManager manager = LogInjectionAgent.getLogPointManager();
        if (manager != null && !manager.isLogPointActive(methodKey)) {
            // No active log point, just execute normally
            return callable.call();
        }
        
        // Log method entry
        logger.info(String.format("[ENTRY] %s.%s(%s)", 
            className, methodName, formatArgs(args)));
        
        long startTime = System.nanoTime();
        Object result = null;
        Throwable exception = null;
        
        try {
            result = callable.call();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            long duration = (System.nanoTime() - startTime) / 1_000_000; // ms
            
            // Log method exit
            if (exception != null) {
                logger.severe(String.format("[EXIT] %s.%s -> EXCEPTION: %s (took %dms)", 
                    className, methodName, exception.getClass().getSimpleName(), duration));
            } else {
                logger.info(String.format("[EXIT] %s.%s -> %s (took %dms)", 
                    className, methodName, formatResult(result), duration));
            }
        }
    }
    
    private static String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
            .map(arg -> arg == null ? "null" : arg.toString())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }
    
    private static String formatResult(Object result) {
        if (result == null) return "void";
        String str = result.toString();
        return str.length() > 100 ? str.substring(0, 100) + "..." : str;
    }
}
