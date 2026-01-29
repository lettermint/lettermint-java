package co.lettermint;

import co.lettermint.exceptions.webhook.InvalidSignatureException;
import co.lettermint.exceptions.webhook.TimestampToleranceException;
import co.lettermint.exceptions.webhook.WebhookVerificationException;
import co.lettermint.webhooks.Webhook;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebhookTest {

    private static final String TEST_SECRET = "whsec_test_secret_key";

    @Test
    void testValidSignature() {
        String payload = "{\"event\":\"email.sent\",\"data\":{\"messageId\":\"msg_123\"}}";
        long timestamp = System.currentTimeMillis() / 1000;
        String hash = computeHmac(timestamp + "." + payload, TEST_SECRET);
        String signature = "t=" + timestamp + ",v1=" + hash;

        Map<String, Object> result = Webhook.verify(payload, signature, TEST_SECRET);

        assertEquals("email.sent", result.get("event"));
        assertNotNull(result.get("data"));
    }

    @Test
    void testValidSignatureWithZeroTolerance() {
        String payload = "{\"event\":\"test\"}";
        long timestamp = 1000;
        String hash = computeHmac(timestamp + "." + payload, TEST_SECRET);
        String signature = "t=" + timestamp + ",v1=" + hash;

        Map<String, Object> result = Webhook.verify(payload, signature, TEST_SECRET, 0);

        assertEquals("test", result.get("event"));
    }

    @Test
    void testInvalidSignature() {
        String payload = "{\"event\":\"test\"}";
        long timestamp = System.currentTimeMillis() / 1000;
        String signature = "t=" + timestamp + ",v1=invalid_hash";

        assertThrows(InvalidSignatureException.class,
                () -> Webhook.verify(payload, signature, TEST_SECRET));
    }

    @Test
    void testTimestampOutsideTolerance() {
        String payload = "{\"event\":\"test\"}";
        long oldTimestamp = (System.currentTimeMillis() / 1000) - 600;
        String hash = computeHmac(oldTimestamp + "." + payload, TEST_SECRET);
        String signature = "t=" + oldTimestamp + ",v1=" + hash;

        TimestampToleranceException ex = assertThrows(TimestampToleranceException.class,
                () -> Webhook.verify(payload, signature, TEST_SECRET, 300));

        assertEquals(oldTimestamp, ex.getTimestamp());
        assertEquals(300, ex.getTolerance());
    }

    @Test
    void testCustomTolerance() {
        String payload = "{\"event\":\"test\"}";
        long oldTimestamp = (System.currentTimeMillis() / 1000) - 500;
        String hash = computeHmac(oldTimestamp + "." + payload, TEST_SECRET);
        String signature = "t=" + oldTimestamp + ",v1=" + hash;

        Map<String, Object> result = Webhook.verify(payload, signature, TEST_SECRET, 600);

        assertEquals("test", result.get("event"));
    }

    @Test
    void testMissingTimestamp() {
        String payload = "{\"event\":\"test\"}";
        String signature = "v1=somehash";

        WebhookVerificationException ex = assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify(payload, signature, TEST_SECRET));

        assertTrue(ex.getMessage().contains("timestamp"));
    }

    @Test
    void testMissingHash() {
        String payload = "{\"event\":\"test\"}";
        String signature = "t=123456";

        WebhookVerificationException ex = assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify(payload, signature, TEST_SECRET));

        assertTrue(ex.getMessage().contains("hash"));
    }

    @Test
    void testNullPayload() {
        assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify(null, "t=123,v1=abc", TEST_SECRET));
    }

    @Test
    void testEmptyPayload() {
        assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify("", "t=123,v1=abc", TEST_SECRET));
    }

    @Test
    void testNullSignature() {
        assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify("{}", null, TEST_SECRET));
    }

    @Test
    void testEmptySignature() {
        assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify("{}", "", TEST_SECRET));
    }

    @Test
    void testNullSecret() {
        assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify("{}", "t=123,v1=abc", null));
    }

    @Test
    void testEmptySecret() {
        assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify("{}", "t=123,v1=abc", ""));
    }

    @Test
    void testInvalidJsonPayload() {
        String payload = "not valid json";
        long timestamp = System.currentTimeMillis() / 1000;
        String hash = computeHmac(timestamp + "." + payload, TEST_SECRET);
        String signature = "t=" + timestamp + ",v1=" + hash;

        assertThrows(WebhookVerificationException.class,
                () -> Webhook.verify(payload, signature, TEST_SECRET));
    }

    @Test
    void testSignatureWithExtraWhitespace() {
        String payload = "{\"event\":\"test\"}";
        long timestamp = System.currentTimeMillis() / 1000;
        String hash = computeHmac(timestamp + "." + payload, TEST_SECRET);
        String signature = "t = " + timestamp + ", v1 = " + hash;

        Map<String, Object> result = Webhook.verify(payload, signature, TEST_SECRET);

        assertEquals("test", result.get("event"));
    }

    private String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
