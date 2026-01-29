package co.lettermint.webhooks;

import co.lettermint.exceptions.webhook.InvalidSignatureException;
import co.lettermint.exceptions.webhook.TimestampToleranceException;
import co.lettermint.exceptions.webhook.WebhookVerificationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Utility class for verifying Lettermint webhook signatures.
 */
public class Webhook {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int DEFAULT_TOLERANCE_SECONDS = 300;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Webhook() {
    }

    /**
     * Verify a webhook signature and parse the payload.
     *
     * @param payload   The raw JSON payload string
     * @param signature The signature header value (format: "t={timestamp},v1={hash}")
     * @param secret    The webhook signing secret
     * @return The parsed payload as a Map
     * @throws WebhookVerificationException if verification fails
     */
    public static Map<String, Object> verify(String payload, String signature, String secret) {
        return verify(payload, signature, secret, DEFAULT_TOLERANCE_SECONDS);
    }

    /**
     * Verify a webhook signature and parse the payload.
     *
     * @param payload   The raw JSON payload string
     * @param signature The signature header value (format: "t={timestamp},v1={hash}")
     * @param secret    The webhook signing secret
     * @param tolerance Maximum allowed age in seconds (0 to disable timestamp check)
     * @return The parsed payload as a Map
     * @throws WebhookVerificationException if verification fails
     */
    public static Map<String, Object> verify(String payload, String signature, String secret, int tolerance) {
        if (payload == null || payload.isEmpty()) {
            throw new WebhookVerificationException("Payload is required");
        }
        if (signature == null || signature.isEmpty()) {
            throw new WebhookVerificationException("Signature is required");
        }
        if (secret == null || secret.isEmpty()) {
            throw new WebhookVerificationException("Secret is required");
        }

        SignatureParts parts = parseSignature(signature);
        verifyTimestamp(parts.timestamp, tolerance);
        verifySignature(payload, parts, secret);

        return parsePayload(payload);
    }

    private static SignatureParts parseSignature(String signature) {
        String timestamp = null;
        String hash = null;

        for (String part : signature.split(",")) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }

            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if ("t".equals(key)) {
                timestamp = value;
            } else if ("v1".equals(key)) {
                hash = value;
            }
        }

        if (timestamp == null || timestamp.isEmpty()) {
            throw new WebhookVerificationException("Missing timestamp in signature");
        }
        if (hash == null || hash.isEmpty()) {
            throw new WebhookVerificationException("Missing hash in signature");
        }

        return new SignatureParts(timestamp, hash);
    }

    private static void verifyTimestamp(String timestampStr, int tolerance) {
        if (tolerance <= 0) {
            return;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new WebhookVerificationException("Invalid timestamp format: " + timestampStr);
        }

        long now = System.currentTimeMillis() / 1000;

        // Reject future timestamps and timestamps older than tolerance
        if (timestamp > now) {
            throw new TimestampToleranceException(
                    String.format("Timestamp is in the future. Timestamp: %d, Current: %d",
                            timestamp, now),
                    timestamp,
                    tolerance
            );
        }

        long age = now - timestamp;
        if (age > tolerance) {
            throw new TimestampToleranceException(
                    String.format("Timestamp too old. Timestamp: %d, Current: %d, Age: %d seconds, Tolerance: %d seconds",
                            timestamp, now, age, tolerance),
                    timestamp,
                    tolerance
            );
        }
    }

    private static void verifySignature(String payload, SignatureParts parts, String secret) {
        String signedPayload = parts.timestamp + "." + payload;
        String expectedHash = computeHmacSha256(signedPayload, secret);

        if (!secureCompare(expectedHash, parts.hash)) {
            throw new InvalidSignatureException("Signature verification failed");
        }
    }

    private static String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new WebhookVerificationException("Failed to compute HMAC", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     */
    private static boolean secureCompare(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }

    private static Map<String, Object> parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new WebhookVerificationException("Failed to parse payload JSON", e);
        }
    }

    private static class SignatureParts {
        final String timestamp;
        final String hash;

        SignatureParts(String timestamp, String hash) {
            this.timestamp = timestamp;
            this.hash = hash;
        }
    }
}
