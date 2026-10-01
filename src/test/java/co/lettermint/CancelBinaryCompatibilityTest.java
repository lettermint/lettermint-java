package co.lettermint;

import co.lettermint.api.ApiClient;
import co.lettermint.models.api.CancelScheduledMessageResponse;
import co.lettermint.models.api.RescheduleMessageResponse;
import co.lettermint.models.api.RouteData;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CancelBinaryCompatibilityTest {
    @TempDir
    Path temporary;

    @Test
    void callerCompiledAgainstOriginalPr53DescriptorStillRuns() throws Exception {
        // PR #53 base: 8e3f4b1b0cbad79fbda6e6d0c724d18a899a2b0b.
        Path stub = temporary.resolve("co/lettermint/api/ApiClient.java");
        Files.createDirectories(stub.getParent());
        Files.write(stub, ("package co.lettermint.api; "
                + "import co.lettermint.models.api.RescheduleMessageResponse; "
                + "public class ApiClient { public static class MessagesEndpoint { "
                + "public RescheduleMessageResponse cancel(String id) { return null; } } }")
                .getBytes(StandardCharsets.UTF_8));
        Path caller = temporary.resolve("OldCaller.java");
        Files.write(caller, ("import co.lettermint.api.ApiClient; "
                + "import co.lettermint.models.api.RescheduleMessageResponse; "
                + "public class OldCaller { "
                + "public static RescheduleMessageResponse cancel(ApiClient.MessagesEndpoint endpoint) { "
                + "return endpoint.cancel(\"message_1\"); } }").getBytes(StandardCharsets.UTF_8));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "This test requires a JDK, not a JRE");
        assertEquals(0, compiler.run(null, null, null, "-classpath",
                System.getProperty("java.class.path"), "-d", temporary.toString(),
                stub.toString(), caller.toString()));

        try (MockWebServer server = new MockWebServer();
             URLClassLoader oldCaller = new URLClassLoader(new URL[]{temporary.toUri().toURL()},
                     ApiClient.class.getClassLoader())) {
            server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                    .setBody("{\"message_id\":\"message_1\",\"status\":\"canceled\"}"));
            server.start();
            ApiClient api = Lettermint.api("token", server.url("/v1").toString());
            Object value = oldCaller.loadClass("OldCaller")
                    .getMethod("cancel", ApiClient.MessagesEndpoint.class).invoke(null, api.messages());
            assertTrue(value instanceof CancelScheduledMessageResponse);
            assertTrue(value instanceof RescheduleMessageResponse);
            assertEquals("message_1", ((RescheduleMessageResponse) value).messageId);
            assertEquals("canceled", ((RescheduleMessageResponse) value).status);
            okhttp3.mockwebserver.RecordedRequest request = server.takeRequest();
            assertEquals("POST", request.getMethod());
            assertEquals("/v1/messages/message_1/cancel", request.getPath());
        }
    }

    @Test
    void currentReturnTypeAndOldSourceAssignmentBothCompile() {
        assertTrue(RescheduleMessageResponse.class.isAssignableFrom(CancelScheduledMessageResponse.class));
    }

    private static RescheduleMessageResponse oldSourceCaller(ApiClient api) {
        return api.messages().cancel("message_1");
    }

    @Test
    void inboundRouteDomainIsOptionalAndNullable() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RouteData populated = mapper.readValue("{\"inbound_route_domain\":\"incoming.example.com\"}", RouteData.class);
        assertEquals("incoming.example.com", populated.inboundRouteDomain);
        assertEquals("incoming.example.com",
                mapper.readValue(mapper.writeValueAsString(populated), RouteData.class).inboundRouteDomain);
        assertNull(mapper.readValue("{\"inbound_route_domain\":null}", RouteData.class).inboundRouteDomain);
        assertNull(mapper.readValue("{}", RouteData.class).inboundRouteDomain);
    }
}
