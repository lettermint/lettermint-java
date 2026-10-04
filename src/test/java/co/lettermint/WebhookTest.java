package co.lettermint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import co.lettermint.exceptions.LettermintConfigException;
import co.lettermint.exceptions.WebhookVerificationException;
import co.lettermint.exceptions.WebhookVerificationException.Reason;
import co.lettermint.types.WebhookEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class WebhookTest {
    private static final String SECRET = "whsec_test0123456789abcdefABCDEF0123";
    private static final long NOW = 1_767_225_600L;
    private static final String BODY = "{\"id\":\"dl_1\",\"event\":\"message.delivered\",\"created_at\":\"2026-01-01T00:00:00Z\",\"data\":{\"message_id\":\"m1\"}}";

    private static Clock at(long seconds) {
        return Clock.fixed(Instant.ofEpochSecond(seconds), ZoneOffset.UTC);
    }

    private static String sign(String secret, long timestamp, byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(mac.doFinal(body));
    }

    private static Map<String, String> headers(long timestamp, String body) throws Exception {
        return Map.of("X-Lettermint-Signature", "t=" + timestamp + ",v1=" + sign(SECRET, timestamp, body.getBytes(StandardCharsets.UTF_8)),
                "X-Lettermint-Delivery", String.valueOf(timestamp));
    }

    private static Reason reason(org.junit.jupiter.api.function.Executable executable) {
        return assertThrows(WebhookVerificationException.class, executable).getReason();
    }

    private final Webhook webhook = new Webhook(SECRET, Duration.ofSeconds(300), at(NOW));

    @Test
    void verifiesAndReturnsThePayload() throws Exception {
        WebhookPayload payload = webhook.verify(BODY, headers(NOW - 30, BODY));
        assertEquals("dl_1", payload.id());
        assertEquals(WebhookEvent.MESSAGE_DELIVERED, payload.event());
        assertEquals("2026-01-01T00:00:00Z", payload.timestamp());
        assertEquals(Map.of("message_id", "m1"), payload.data());
        assertEquals("message.delivered", payload.fields().get("event"));
        record Delivered(String message_id) {
        }
        assertEquals(new Delivered("m1"), payload.data(Delivered.class));
        assertEquals("dl_1", webhook.verify(BODY.getBytes(StandardCharsets.UTF_8), headers(NOW, BODY)).id());
    }

    @Test
    void acceptsCommonHeaderShapes() throws Exception {
        Map<String, String> single = headers(NOW, BODY);
        Map<String, List<String>> multi = new LinkedHashMap<>();
        single.forEach((name, value) -> multi.put(name.toLowerCase(), List.of(value)));
        webhook.verify(BODY, multi);
        Map<String, Object> mixed = new LinkedHashMap<>();
        single.forEach((name, value) -> mixed.put(name.toUpperCase(), new String[] {value}));
        webhook.verify(BODY, mixed);
        webhook.verify(BODY, name -> single.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(name)).map(Map.Entry::getValue).findFirst().orElse(null));
    }

    @Test
    void requiresBothHeaders() throws Exception {
        Map<String, String> valid = headers(NOW, BODY);
        assertEquals(Reason.SIGNATURE_HEADER_MISSING, reason(() -> webhook.verify(BODY, Map.of("X-Lettermint-Delivery", String.valueOf(NOW)))));
        assertEquals(Reason.DELIVERY_HEADER_MISSING, reason(() -> webhook.verify(BODY, Map.of("X-Lettermint-Signature", valid.get("X-Lettermint-Signature")))));
        assertEquals(Reason.DELIVERY_TIMESTAMP_MISMATCH, reason(() -> webhook.verify(BODY,
                Map.of("X-Lettermint-Signature", valid.get("X-Lettermint-Signature"), "X-Lettermint-Delivery", String.valueOf(NOW + 1)))));
        Map<String, List<String>> twice = Map.of("x-lettermint-signature", List.of(valid.get("X-Lettermint-Signature"), valid.get("X-Lettermint-Signature")),
                "x-lettermint-delivery", List.of(String.valueOf(NOW)));
        assertEquals(Reason.SIGNATURE_HEADER_MALFORMED, reason(() -> webhook.verify(BODY, twice)));
        Map<String, List<String>> deliveries = Map.of("x-lettermint-signature", List.of(valid.get("X-Lettermint-Signature")),
                "x-lettermint-delivery", List.of(String.valueOf(NOW), String.valueOf(NOW)));
        assertEquals(Reason.DELIVERY_TIMESTAMP_MISMATCH, reason(() -> webhook.verify(BODY, deliveries)));
        assertEquals(Reason.SIGNATURE_HEADER_MISSING, reason(() -> webhook.verify(BODY, (Map<String, ?>) null)));
    }

    @Test
    void checksTheToleranceInBothDirections() throws Exception {
        webhook.verify(BODY, headers(NOW - 300, BODY));
        webhook.verify(BODY, headers(NOW + 300, BODY));
        assertEquals(Reason.TIMESTAMP_OUT_OF_TOLERANCE, reason(() -> webhook.verify(BODY, headers(NOW - 301, BODY))));
        assertEquals(Reason.TIMESTAMP_OUT_OF_TOLERANCE, reason(() -> webhook.verify(BODY, headers(NOW + 301, BODY))));
        Webhook strict = new Webhook(SECRET, Duration.ZERO, at(NOW));
        strict.verify(BODY, headers(NOW, BODY));
        assertEquals(Reason.TIMESTAMP_OUT_OF_TOLERANCE, reason(() -> strict.verify(BODY, headers(NOW - 1, BODY))));
    }

    @Test
    void rejectsTamperingAndMalformedHeaders() throws Exception {
        String signature = sign(SECRET, NOW, BODY.getBytes(StandardCharsets.UTF_8));
        String other = sign("whsec_other", NOW, BODY.getBytes(StandardCharsets.UTF_8));
        assertEquals(Reason.SIGNATURE_MISMATCH, reason(() -> webhook.verifySignature(BODY.replace("m1", "m2"), "t=" + NOW + ",v1=" + signature)));
        webhook.verifySignature(BODY, "t=" + NOW + ",v1=" + other + ",v1=" + signature);
        webhook.verifySignature(BODY, "t=" + NOW + ",v1=" + signature + ",v1=" + other);
        webhook.verifySignature(BODY, "t=" + NOW + ",v0=abc,v1=" + signature, String.valueOf(NOW));
        assertEquals(Reason.SIGNATURE_MISMATCH, reason(() -> new Webhook("test0123456789abcdefABCDEF0123", Duration.ofSeconds(300), at(NOW))
                .verifySignature(BODY, "t=" + NOW + ",v1=" + signature)));
        assertEquals(Reason.SIGNATURE_HEADER_MALFORMED, reason(() -> webhook.verifySignature(BODY, "t=" + NOW + ",t=" + NOW + ",v1=" + signature)));
        assertEquals(Reason.SIGNATURE_HEADER_MALFORMED, reason(() -> webhook.verifySignature(BODY, "v1=" + signature)));
        assertEquals(Reason.SIGNATURE_HEADER_MALFORMED, reason(() -> webhook.verifySignature(BODY, "t=" + NOW)));
        assertEquals(Reason.SIGNATURE_HEADER_MALFORMED, reason(() -> webhook.verifySignature(BODY, "t=abc,v1=" + signature)));
        assertEquals(Reason.SIGNATURE_HEADER_MALFORMED, reason(() -> webhook.verifySignature(BODY, "t=99999999999999999999,v1=" + signature)));
        assertEquals(Reason.SIGNATURE_HEADER_MALFORMED, reason(() -> webhook.verifySignature(BODY, "t=" + NOW + ",v1=é" + signature.substring(1))));
        assertEquals(Reason.SIGNATURE_HEADER_MALFORMED, reason(() -> webhook.verifySignature(BODY, "t=１７６７２２５６００,v1=" + signature)));
        assertEquals(Reason.SIGNATURE_HEADER_MISSING, reason(() -> webhook.verifySignature(BODY, " ")));
        assertEquals(Reason.BODY_INVALID, reason(() -> webhook.verifySignature("", "t=" + NOW + ",v1=" + signature)));
        assertEquals(Reason.BODY_INVALID, reason(() -> webhook.verifySignature((byte[]) null, "t=" + NOW + ",v1=" + signature)));
    }

    @Test
    void rejectsPayloadsThatAreNotJsonObjects() throws Exception {
        for (String body : new String[] {"[1]", "not json", "\"text\""}) {
            String header = "t=" + NOW + ",v1=" + sign(SECRET, NOW, body.getBytes(StandardCharsets.UTF_8));
            assertEquals(Reason.PAYLOAD_INVALID, reason(() -> webhook.verifySignature(body, header)));
        }
    }

    @Test
    void validatesItsConfiguration() {
        assertThrows(LettermintConfigException.class, () -> new Webhook(""));
        assertThrows(LettermintConfigException.class, () -> new Webhook(null));
        assertThrows(LettermintConfigException.class, () -> new Webhook(SECRET, Duration.ofSeconds(-1)));
        assertThrows(LettermintConfigException.class, () -> new Webhook(SECRET, Duration.ofMillis(1500)));
        assertEquals(Duration.ofSeconds(300), new Webhook(SECRET).tolerance());
    }

    @Test
    void neverPrintsTheSecret() {
        String text = webhook.toString();
        assertEquals("Webhook{tolerance=300s}", text);
        WebhookVerificationException error = assertThrows(WebhookVerificationException.class,
                () -> webhook.verify("{}", Map.of("X-Lettermint-Signature", "t=1,v1=" + "0".repeat(64), "X-Lettermint-Delivery", "1")));
        assertFalse(error.toString().contains(SECRET));
        assertFalse(error.getMessage().contains(SECRET));
        assertEquals("timestamp_out_of_tolerance", error.getReason().code());
    }

    @TestFactory
    List<DynamicTest> sharedVectors() throws Exception {
        JsonNode vectors;
        try (InputStream in = getClass().getResourceAsStream("/webhooks.json")) {
            vectors = new ObjectMapper().readTree(in);
        }
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonNode vector : vectors.get("vectors")) {
            tests.add(DynamicTest.dynamicTest(vector.get("id").asText(), () -> {
                Webhook verifier = new Webhook(vector.get("secret").asText(), Duration.ofSeconds(vector.get("tolerance").asLong()), at(vector.get("now").asLong()));
                Map<String, String> headers = new LinkedHashMap<>();
                vector.get("headers").properties().forEach(entry -> headers.put(entry.getKey(), entry.getValue().asText()));
                byte[] body = Base64.getDecoder().decode(vector.get("body_base64").asText());
                String expect = vector.get("expect").asText();
                try {
                    verifier.verify(body, headers);
                    if (!expect.equals("valid")) {
                        fail("expected " + vector.get("reason").asText());
                    }
                } catch (WebhookVerificationException error) {
                    if (expect.equals("valid")) {
                        fail("expected valid, got " + error.getReason().code() + ": " + error.getMessage());
                    }
                    assertEquals(vector.get("reason").asText(), error.getReason().code());
                }
            }));
        }
        assertTrue(tests.size() >= 25);
        return tests;
    }
}
