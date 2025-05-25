import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.Map;

public class LogPointManager {
    private final ConcurrentHashMap<String, LogPoint> activeLogPoints = new ConcurrentHashMap<>();
    
    public void addLogPoint(String methodKey, boolean enabled) {
        activeLogPoints.put(methodKey, new LogPoint(methodKey, enabled));
        System.out.println("Added log point: " + methodKey + " (enabled: " + enabled + ")");
    }
    
    public void removeLogPoint(String methodKey) {
        LogPoint removed = activeLogPoints.remove(methodKey);
        if (removed != null) {
            System.out.println("Removed log point: " + methodKey);
        }
    }
    
    public void enableLogPoint(String methodKey) {
        LogPoint logPoint = activeLogPoints.get(methodKey);
        if (logPoint != null) {
            logPoint.setEnabled(true);
            System.out.println("Enabled log point: " + methodKey);
        }
    }
    
    public void disableLogPoint(String methodKey) {
        LogPoint logPoint = activeLogPoints.get(methodKey);
        if (logPoint != null) {
            logPoint.setEnabled(false);
            System.out.println("Disabled log point: " + methodKey);
        }
    }
    
    public boolean isLogPointActive(String methodKey) {
        LogPoint logPoint = activeLogPoints.get(methodKey);
        return logPoint != null && logPoint.isEnabled();
    }
    
    public Set<String> getActiveLogPoints() {
        return activeLogPoints.keySet();
    }
    
    public Map<String, LogPoint> getAllLogPoints() {
        return new ConcurrentHashMap<>(activeLogPoints);
    }
    
    public void clearAll() {
        activeLogPoints.clear();
        System.out.println("Cleared all log points");
    }
    
    public static class LogPoint {
        private final String methodKey;
        private volatile boolean enabled;
        private final long createdAt;
        
        public LogPoint(String methodKey, boolean enabled) {
            this.methodKey = methodKey;
            this.enabled = enabled;
            this.createdAt = System.currentTimeMillis();
        }
        
        public String getMethodKey() { return methodKey; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public long getCreatedAt() { return createdAt; }
    }
}
