import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class LogInjectionAgentTest {

    private Instrumentation getInstrumentation() {
        return ByteBuddyAgent.install();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Stop the HTTP server and reset static fields
        Field serverField = LogInjectionAgent.class.getDeclaredField("apiServer");
        serverField.setAccessible(true);
        Object server = serverField.get(null);
        if (server != null) {
            server.getClass().getMethod("stop").invoke(server);
        }
        Field managerField = LogInjectionAgent.class.getDeclaredField("logPointManager");
        managerField.setAccessible(true);
        managerField.set(null, null);
        serverField.set(null, null);
    }

    @Test
    public void testPremainInitializesManager() {
        Instrumentation inst = getInstrumentation();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            LogInjectionAgent.premain("apiPort=0", inst);
        } finally {
            System.setOut(original);
        }
        assertTrue(out.toString().contains("Starting Log Injection Agent"));
        assertNotNull(LogInjectionAgent.getLogPointManager());
    }

    @Test
    public void testAgentmainDelegatesToPremain() {
        Instrumentation inst = getInstrumentation();
        LogInjectionAgent.agentmain("apiPort=0", inst);
        assertNotNull(LogInjectionAgent.getLogPointManager());
    }
}
