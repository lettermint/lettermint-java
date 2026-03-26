package co.lettermint;

import co.lettermint.models.SendEmailResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.ByteString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies OkHttp/Okio compatibility after downgrade to 4.9.3.
 * Exercises ByteString and request/response serialization paths
 * that previously caused NoSuchFieldError with Okio 3.x.
 */
class OkHttpCompatibilityTest {

    private MockWebServer server;
    private Lettermint lettermint;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        lettermint = new Lettermint("test-token", server.url("/v1").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void okioByteStringIsAccessible() {
        // This is the exact code path that fails with NoSuchFieldError
        // when Okio 2.x and 3.x jars conflict on the classpath
        ByteString bs = ByteString.encodeUtf8("hello");
        assertEquals("hello", bs.utf8());
    }

    @Test
    void fullRoundTripRequest() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_compat\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        SendEmailResponse response = lettermint.email()
                .from("test@example.com")
                .to("recipient@example.com")
                .subject("OkHttp 4.9.3 compat test")
                .text("Verifying request body serialization through Okio")
                .html("<p>With HTML body too</p>")
                .send();

        assertEquals("msg_compat", response.getMessageId());
        assertEquals("queued", response.getStatus());

        RecordedRequest recorded = server.takeRequest();
        assertEquals("POST", recorded.getMethod());

        // readUtf8() goes through Okio's Buffer/ByteString internally
        String body = recorded.getBody().readUtf8();
        assertTrue(body.contains("\"subject\":\"OkHttp 4.9.3 compat test\""));
        assertTrue(body.contains("\"text\":\"Verifying request body serialization through Okio\""));
        assertTrue(body.contains("\"html\":\"<p>With HTML body too</p>\""));
    }

    @Test
    void multipleSequentialRequests() throws Exception {
        for (int i = 0; i < 3; i++) {
            server.enqueue(new MockResponse()
                    .setBody("{\"message_id\": \"msg_" + i + "\", \"status\": \"queued\"}")
                    .setHeader("Content-Type", "application/json"));
        }

        for (int i = 0; i < 3; i++) {
            SendEmailResponse response = lettermint.email()
                    .from("test@example.com")
                    .to("r@example.com")
                    .subject("Msg " + i)
                    .text("Body " + i)
                    .send();

            assertEquals("msg_" + i, response.getMessageId());
        }

        // Verify all 3 requests went through with correct bodies
        for (int i = 0; i < 3; i++) {
            RecordedRequest req = server.takeRequest();
            String body = req.getBody().readUtf8();
            assertTrue(body.contains("\"subject\":\"Msg " + i + "\""));
        }
    }
}
