package co.lettermint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.lettermint.exceptions.LettermintValidationException;
import co.lettermint.types.MessageTagInput;
import co.lettermint.types.SandboxResult;
import co.lettermint.types.SendMailRequestSettings;
import co.lettermint.types.SendMailResponse;
import co.lettermint.types.TlsPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmailsTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    private FakeServer server;
    private Lettermint client;

    @BeforeEach
    void start() throws Exception {
        server = new FakeServer();
        server.fallback(request -> request.path().equals("/v1/send/batch")
                ? FakeServer.Response.json(202, "[{\"message_id\":\"m1\",\"status\":\"pending\"},{\"message_id\":\"m2\",\"status\":\"scheduled\",\"scheduled_at\":\"2026-10-20T09:00:00Z\"}]")
                : FakeServer.Response.json(202, "{\"message_id\":\"m1\",\"status\":\"pending\",\"sandbox\":true,\"sandbox_result\":\"hard_bounced\"}"));
        client = Lettermint.builder().sendingToken(ClientTest.SENDING).baseUrl(server.baseUrl()).build();
    }

    @AfterEach
    void stop() {
        server.close();
    }

    private JsonNode lastBody() throws Exception {
        return JSON.readTree(server.last().body());
    }

    @Test
    void sendsEveryFieldInWireFormat() throws Exception {
        byte[] pdf = "%PDF-1.4".getBytes(StandardCharsets.UTF_8);
        SendMailResponse response = client.emails().compose()
                .from("Acme <hello@acme.com>")
                .to("jane@example.com", "john@example.com")
                .cc("cc@example.com")
                .bcc(List.of("bcc@example.com"))
                .replyTo("support@acme.com")
                .subject("Welcome")
                .html("<p>Hi</p>")
                .text("Hi")
                .headers(Map.of("X-Campaign", "welcome"))
                .metadata(Map.of("order_id", "1234"))
                .tag("legacy")
                .tags(new MessageTagInput("campaign", "welcome"))
                .route("transactional")
                .scheduledAt(Instant.parse("2026-10-20T09:00:00Z"))
                .settings(SendMailRequestSettings.builder().trackOpens(false).tls(TlsPolicy.of("required")).build())
                .sandboxResult(SandboxResult.of("hard_bounced"))
                .attach(EmailAttachment.of("invoice.pdf", pdf).contentType("application/pdf"))
                .attach(EmailAttachment.ofBase64("logo.png", "aGVsbG8=").contentId("logo"))
                .send(SendOptions.idempotencyKey("welcome-jane"));
        assertEquals("m1", response.messageId());
        assertTrue(response.sandbox());
        assertEquals("hard_bounced", response.sandboxResult().value());

        FakeServer.Request request = server.last();
        assertEquals("/v1/send", request.path());
        assertEquals("welcome-jane", request.header("idempotency-key"));
        assertEquals(ClientTest.SENDING, request.header("x-lettermint-token"));
        JsonNode body = lastBody();
        assertEquals("Acme <hello@acme.com>", body.get("from").asText());
        assertEquals(List.of("jane@example.com", "john@example.com"), JSON.convertValue(body.get("to"), List.class));
        assertEquals("support@acme.com", body.get("reply_to").get(0).asText());
        assertEquals("2026-10-20T09:00:00Z", body.get("scheduled_at").asText());
        assertEquals("hard_bounced", body.get("sandbox_result").asText());
        assertEquals("{\"track_opens\":false,\"tls\":\"required\"}", body.get("settings").toString());
        assertEquals("[{\"name\":\"campaign\",\"value\":\"welcome\"}]", body.get("tags").toString());
        assertEquals("legacy", body.get("tag").asText());
        assertEquals("{\"filename\":\"invoice.pdf\",\"content\":\"" + Base64.getEncoder().encodeToString(pdf) + "\",\"content_type\":\"application/pdf\"}",
                body.get("attachments").get(0).toString());
        assertEquals("{\"filename\":\"logo.png\",\"content\":\"aGVsbG8=\",\"content_id\":\"logo\"}", body.get("attachments").get(1).toString());
        assertEquals(17, body.size());
    }

    @Test
    void sendsPlainMessages() throws Exception {
        EmailMessage message = EmailMessage.create().from("a@b.c").to("d@e.f").subject("s").html("<p>x</p>");
        client.emails().send(message);
        assertEquals("{\"from\":\"a@b.c\",\"to\":[\"d@e.f\"],\"subject\":\"s\",\"html\":\"<p>x</p>\"}", server.last().body());
        client.emails().send(message.html(null).text("x"));
        assertEquals("{\"from\":\"a@b.c\",\"to\":[\"d@e.f\"],\"subject\":\"s\",\"text\":\"x\"}", server.last().body());
    }

    @Test
    void buildersAreImmutable() {
        EmailBuilder base = client.emails().compose().from("hello@acme.com").subject("Welcome");
        EmailBuilder jane = base.to("jane@example.com").html("<p>Jane</p>");
        EmailBuilder john = base.to("john@example.com");
        assertNotSame(base, jane);
        assertEquals(List.of(), base.build().to());
        assertNull(base.build().html());
        assertEquals(List.of("jane@example.com"), jane.build().to());
        assertEquals(List.of("john@example.com"), john.build().to());
        assertNull(john.build().html());

        jane.send();
        jane.send();
        assertEquals(2, server.requests().size());
        assertEquals(server.requests().get(0).body(), server.requests().get(1).body());

        EmailMessage message = EmailMessage.create().to(new ArrayList<>(List.of("a@b.c")));
        assertThrows(UnsupportedOperationException.class, () -> message.to().add("x@y.z"));
        assertSame(EmailMessage.create(), EmailMessage.create());
        assertEquals(message.to(), client.emails().compose(message).build().to());
    }

    @Test
    void aThrowingSetterLeavesTheBuilderUnchanged() {
        EmailBuilder base = client.emails().compose().from("a@b.c").tags(new MessageTagInput("ok", "1"));
        assertThrows(LettermintValidationException.class, () -> base.tags(new MessageTagInput("not a valid tag name!", "x")));
        assertEquals(List.of(new MessageTagInput("ok", "1")), base.build().tags());
        base.to("d@e.f").subject("s").send();
        assertEquals("[{\"name\":\"ok\",\"value\":\"1\"}]", server.last().body().replaceAll(".*\"tags\":(\\[[^]]*]).*", "$1"));
    }

    @Test
    void concurrentSendsFromOneTemplateDoNotMix() throws Exception {
        EmailBuilder template = client.emails().compose().from("hello@acme.com").subject("Welcome");
        int count = 16;
        CyclicBarrier barrier = new CyclicBarrier(count);
        ExecutorService pool = Executors.newFixedThreadPool(count);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int index = i;
                futures.add(pool.submit(() -> {
                    EmailBuilder email = template.to("user" + index + "@example.com");
                    barrier.await();
                    email = email.html("<p>" + index + "</p>");
                    barrier.await();
                    return email.send(SendOptions.idempotencyKey("key-" + index));
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdown();
        }
        assertEquals(count, server.requests().size());
        for (FakeServer.Request request : server.requests()) {
            JsonNode body = JSON.readTree(request.body());
            String index = body.get("to").get(0).asText().replaceAll("\\D", "");
            assertEquals("<p>" + index + "</p>", body.get("html").asText());
            assertEquals("key-" + index, request.header("idempotency-key"));
        }
    }

    @Test
    void validatesTags() {
        EmailMessage base = EmailMessage.create();
        List<MessageTagInput> twenty = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            twenty.add(new MessageTagInput("t" + i, "v"));
        }
        base.tags(twenty);
        List<MessageTagInput> twentyOne = new ArrayList<>(twenty);
        twentyOne.add(new MessageTagInput("t20", "v"));
        assertEquals("No more than 20 message tags are permitted.",
                assertThrows(LettermintValidationException.class, () -> base.tags(twentyOne)).getMessage());
        assertEquals("A legacy tag and no more than 19 message tags are permitted.",
                assertThrows(LettermintValidationException.class, () -> base.tags(twenty).tag("legacy")).getMessage());
        assertEquals("A legacy tag and no more than 19 message tags are permitted.",
                assertThrows(LettermintValidationException.class, () -> base.tag("legacy").tags(twenty)).getMessage());
        assertThrows(LettermintValidationException.class, () -> base.tags(new MessageTagInput("x".repeat(33), "v")));
        assertThrows(LettermintValidationException.class, () -> base.tags(new MessageTagInput("a", "v".repeat(65))));
        assertThrows(LettermintValidationException.class, () -> base.tags(new MessageTagInput("a", "")));
        assertThrows(LettermintValidationException.class, () -> base.tags(new MessageTagInput("a b", "v")));
        assertEquals("Message tag names must not start with __lettermint.",
                assertThrows(LettermintValidationException.class, () -> base.tags(new MessageTagInput("__LetterMint_x", "v"))).getMessage());
        LettermintValidationException duplicate = assertThrows(LettermintValidationException.class,
                () -> base.tags(new MessageTagInput("a", "1"), new MessageTagInput("a", "2")));
        assertEquals("tags", duplicate.getField());
        base.tags(new MessageTagInput("a", "1"), new MessageTagInput("A", "2"));
        assertThrows(LettermintValidationException.class, () -> base.tags(new MessageTagInput(null, "1")));
        assertTrue(server.requests().isEmpty());
    }

    @Test
    void validatesAttachments() {
        assertThrows(LettermintValidationException.class, () -> EmailAttachment.of("", new byte[] {1}));
        assertThrows(LettermintValidationException.class, () -> EmailAttachment.of("a.txt", null));
        assertThrows(LettermintValidationException.class, () -> EmailAttachment.ofBase64(null, "AA=="));
        byte[] bytes = {1, 2, 3};
        EmailAttachment attachment = EmailAttachment.of("a.bin", bytes);
        bytes[0] = 9;
        assertEquals("AQID", attachment.base64Content());
        assertEquals("EmailAttachment{filename=a.bin}", attachment.toString());
    }

    @Test
    void sendsBatches() throws Exception {
        EmailBuilder welcome = client.emails().compose().from("hello@acme.com").subject("Welcome");
        List<SendMailResponse> results = client.emails().sendBatch(List.of(
                EmailMessage.create().from("a@b.c").to("d@e.f").subject("One").text("1"),
                welcome.to("john@example.com").html("<p>2</p>").build()), SendOptions.idempotencyKey("batch-1"));
        assertEquals(2, results.size());
        assertEquals("scheduled", results.get(1).status().value());
        assertEquals("/v1/send/batch", server.last().path());
        assertEquals("batch-1", server.last().header("idempotency-key"));
        JsonNode body = lastBody();
        assertEquals(2, body.size());
        assertEquals("Welcome", body.get(1).get("subject").asText());

        List<EmailMessage> withNull = new ArrayList<>();
        withNull.add(null);
        assertEquals("messages[0]", assertThrows(LettermintValidationException.class, () -> client.emails().sendBatch(withNull)).getField());
        assertThrows(LettermintValidationException.class, () -> client.emails().sendBatch(null));
    }

    @Test
    void reschedulesFromAString() throws Exception {
        client.emails().send(EmailMessage.create().from("a@b.c").scheduledAt("tomorrow 9am"));
        assertEquals("tomorrow 9am", lastBody().get("scheduled_at").asText());
        client.emails().send(EmailMessage.create().from("a@b.c").scheduledAt("tomorrow").scheduledAt((String) null));
        assertNull(lastBody().get("scheduled_at"));
    }

    @Test
    void toStringShowsTheMessageWithoutContent() {
        EmailBuilder builder = client.emails().compose().from("a@b.c").to("d@e.f").html("<p>secret html</p>")
                .attach(EmailAttachment.ofBase64("a.txt", "c2VjcmV0"));
        String text = builder.toString();
        assertTrue(text.startsWith("EmailBuilder{EmailMessage{from=a@b.c, to=[d@e.f], html=(18 characters)"), text);
        assertTrue(!text.contains("c2VjcmV0") && !text.contains("secret html"), text);
        assertEquals("Emails{}", client.emails().toString());
    }
}
