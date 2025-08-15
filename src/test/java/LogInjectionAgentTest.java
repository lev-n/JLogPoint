import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

public class LogInjectionAgentTest {

    @BeforeEach
    void reset() throws Exception {
        Field managerField = LogInjectionAgent.class.getDeclaredField("logPointManager");
        managerField.setAccessible(true);
        managerField.set(null, null);

        Field serverField = LogInjectionAgent.class.getDeclaredField("apiServer");
        serverField.setAccessible(true);
        HttpApiServer server = (HttpApiServer) serverField.get(null);
        if (server != null) {
            server.stop();
        }
        serverField.set(null, null);
    }

    @Test
    void test_GetLogPointManager_BeforeInitialization_ReturnsNull() {
        Assertions.assertNull(LogInjectionAgent.getLogPointManager());
    }

    @Test
    void test_GetLogPointManager_AfterPremain_ReturnsInitializedManager() {
        Instrumentation inst = Mockito.mock(Instrumentation.class);
        Mockito.when(inst.isRedefineClassesSupported()).thenReturn(true);
        Mockito.when(inst.isRetransformClassesSupported()).thenReturn(true);

        LogInjectionAgent.premain("apiPort=0", inst);
        Assertions.assertNotNull(LogInjectionAgent.getLogPointManager());
    }

    @Test
    void test_Agentmain_WithValidArgs_InitializesManager() {
        Instrumentation inst = Mockito.mock(Instrumentation.class);
        Mockito.when(inst.isRedefineClassesSupported()).thenReturn(true);
        Mockito.when(inst.isRetransformClassesSupported()).thenReturn(true);

        LogInjectionAgent.agentmain("apiPort=0", inst);
        Assertions.assertNotNull(LogInjectionAgent.getLogPointManager());
    }
}
