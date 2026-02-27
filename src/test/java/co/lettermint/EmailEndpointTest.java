package co.lettermint;

import co.lettermint.models.SendEmailResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmailEndpointTest {

    private MockWebServer mockWebServer;
    private Lettermint lettermint;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String baseUrl = mockWebServer.url("/v1").toString();
        lettermint = new Lettermint("test-api-token", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testSimpleEmail() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_123\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        SendEmailResponse response = lettermint.email()
                .from("sender@example.com")
                .to("recipient@example.com")
                .subject("Test Subject")
                .text("Hello World")
                .send();

        assertEquals("msg_123", response.getMessageId());
        assertEquals("queued", response.getStatus());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v1/send", request.getPath());
        assertTrue(request.getHeader("Content-Type").startsWith("application/json"));
        assertEquals("test-api-token", request.getHeader("x-lettermint-token"));
        assertTrue(request.getHeader("User-Agent").startsWith("Lettermint/"));
    }

    @Test
    void testEmailWithAllOptions() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_456\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "custom-value");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", "user_123");
        metadata.put("campaign", "welcome");

        SendEmailResponse response = lettermint.email()
                .from("John Doe <sender@example.com>")
                .to("recipient1@example.com", "recipient2@example.com")
                .cc("cc@example.com")
                .bcc("bcc@example.com")
                .replyTo("reply@example.com")
                .subject("Welcome!")
                .html("<p>Hello <b>World</b></p>")
                .text("Hello World")
                .headers(headers)
                .attach("document.pdf", "base64content")
                .attach("logo.png", "base64logo", "logo-cid")
                .route("route-slug-123")
                .metadata(metadata)
                .tag("welcome", "onboarding")
                .idempotencyKey("unique-key-123")
                .send();

        assertEquals("msg_456", response.getMessageId());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("unique-key-123", request.getHeader("Idempotency-Key"));

        String body = request.getBody().readUtf8();
        assertTrue(body.contains("\"from\":\"John Doe <sender@example.com>\""));
        assertTrue(body.contains("\"to\":[\"recipient1@example.com\",\"recipient2@example.com\"]"));
        assertTrue(body.contains("\"cc\":[\"cc@example.com\"]"));
        assertTrue(body.contains("\"bcc\":[\"bcc@example.com\"]"));
        assertTrue(body.contains("\"reply_to\":[\"reply@example.com\"]"));
        assertTrue(body.contains("\"subject\":\"Welcome!\""));
        assertTrue(body.contains("\"html\":\"<p>Hello <b>World</b></p>\""));
        assertTrue(body.contains("\"text\":\"Hello World\""));
        assertTrue(body.contains("\"route\":\"route-slug-123\""));
        assertTrue(body.contains("\"tags\":[\"welcome\",\"onboarding\"]"));
    }

    @Test
    void testRecipientsReplaceWhenCalledMultipleTimes() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_789\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        lettermint.email()
                .from("sender@example.com")
                .to("first@example.com")
                .to("replaced@example.com")
                .subject("Test")
                .text("Test")
                .send();

        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();

        assertTrue(body.contains("\"to\":[\"replaced@example.com\"]"));
        assertFalse(body.contains("first@example.com"));
    }

    @Test
    void testEndpointResetsAfterSend() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_1\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_2\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        lettermint.email()
                .from("sender@example.com")
                .to("recipient@example.com")
                .subject("First Email")
                .text("First content")
                .send();

        lettermint.email()
                .from("sender@example.com")
                .to("other@example.com")
                .subject("Second Email")
                .text("Second content")
                .send();

        mockWebServer.takeRequest();
        RecordedRequest secondRequest = mockWebServer.takeRequest();
        String body = secondRequest.getBody().readUtf8();

        assertTrue(body.contains("\"subject\":\"Second Email\""));
        assertFalse(body.contains("First Email"));
    }

    @Test
    void testSingleHeaderMethod() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_123\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        lettermint.email()
                .from("sender@example.com")
                .to("recipient@example.com")
                .subject("Test")
                .text("Test")
                .header("X-First", "value1")
                .header("X-Second", "value2")
                .send();

        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();

        assertTrue(body.contains("\"X-First\":\"value1\""));
        assertTrue(body.contains("\"X-Second\":\"value2\""));
    }

    @Test
    void testReplyToSingleString() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_123\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        lettermint.email()
                .from("sender@example.com")
                .to("recipient@example.com")
                .subject("Test")
                .text("Test")
                .replyTo("reply@example.com")
                .send();

        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();

        assertTrue(body.contains("\"reply_to\":[\"reply@example.com\"]"));
    }

    @Test
    void testReplyToMultipleAddresses() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_123\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        lettermint.email()
                .from("sender@example.com")
                .to("recipient@example.com")
                .subject("Test")
                .text("Test")
                .replyTo("reply1@example.com", "reply2@example.com")
                .send();

        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();

        assertTrue(body.contains("\"reply_to\":[\"reply1@example.com\",\"reply2@example.com\"]"));
    }

    @Test
    void testSingleMetadataMethod() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message_id\": \"msg_123\", \"status\": \"queued\"}")
                .setHeader("Content-Type", "application/json"));

        lettermint.email()
                .from("sender@example.com")
                .to("recipient@example.com")
                .subject("Test")
                .text("Test")
                .metadata("key1", "value1")
                .metadata("key2", 123)
                .send();

        RecordedRequest request = mockWebServer.takeRequest();
        String body = request.getBody().readUtf8();

        assertTrue(body.contains("\"key1\":\"value1\""));
        assertTrue(body.contains("\"key2\":123"));
    }
}
