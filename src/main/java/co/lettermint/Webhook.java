package co.lettermint;

import co.lettermint.exceptions.LettermintConfigException;
import co.lettermint.exceptions.WebhookVerificationException;
import co.lettermint.exceptions.WebhookVerificationException.Reason;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifies Lettermint webhook deliveries: an HMAC-SHA256 signature over {@code "<t>." + raw body} with
 * the endpoint's signing secret ({@code whsec_...}, used as is), compared in constant time.
 * Immutable and thread-safe.
 *
 * <pre>{@code
 * Webhook webhook = new Webhook(System.getenv("LETTERMINT_WEBHOOK_SECRET"));
 * WebhookPayload event = webhook.verify(rawBody, headers);
 * }</pre>
 */
public final class Webhook {
    private static final String SIGNATURE_HEADER = "X-Lettermint-Signature";
    private static final String DELIVERY_HEADER = "X-Lettermint-Delivery";
    private static final Duration DEFAULT_TOLERANCE = Duration.ofSeconds(300);
    private static final Pattern PRINTABLE_ASCII = Pattern.compile("^[\\x20-\\x7e]*$");
    private static final Pattern DIGITS = Pattern.compile("^[0-9]+$");
    private static final Pattern HEX_SHA256 = Pattern.compile("^[0-9a-fA-F]{64}$");
    /** Number.MAX_SAFE_INTEGER, as in the other SDKs. */
    private static final BigInteger MAX_SAFE_INTEGER = BigInteger.valueOf(9007199254740991L);
    private static final TypeReference<Map<String, Object>> OBJECT = new TypeReference<>() {};

    private final SecretKeySpec key;
    private final long tolerance;
    private final Clock clock;

    /**
     * Verifies with the default tolerance of 300 seconds.
     *
     * @param secret the webhook's signing secret, including its {@code whsec_} prefix
     */
    public Webhook(String secret) {
        this(secret, DEFAULT_TOLERANCE, Clock.systemUTC());
    }

    /**
     * @param secret the webhook's signing secret, including its {@code whsec_} prefix
     * @param tolerance the maximum difference between the signed timestamp and now, in either
     *     direction, in whole seconds; {@code Duration.ZERO} accepts only the current second
     */
    public Webhook(String secret, Duration tolerance) {
        this(secret, tolerance, Clock.systemUTC());
    }

    /**
     * @param secret the webhook's signing secret, including its {@code whsec_} prefix
     * @param tolerance the maximum difference between the signed timestamp and now, in whole seconds
     * @param clock the clock to compare the timestamp with, for tests
     */
    public Webhook(String secret, Duration tolerance, Clock clock) {
        if (secret == null || secret.isEmpty()) {
            throw new LettermintConfigException("The webhook signing secret must be a non-empty string.");
        }
        if (tolerance == null || tolerance.isNegative() || tolerance.getNano() != 0) {
            throw new LettermintConfigException("tolerance must be a non-negative whole number of seconds.");
        }
        if (clock == null) {
            throw new LettermintConfigException("clock must not be null.");
        }
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.tolerance = tolerance.getSeconds();
        this.clock = clock;
    }

    /**
     * @return the timestamp tolerance
     */
    public Duration tolerance() {
        return Duration.ofSeconds(tolerance);
    }

    /**
     * Verifies a delivery from its raw body and request headers, and returns the payload. Requires
     * {@code X-Lettermint-Signature} and {@code X-Lettermint-Delivery} (which must equal the signed
     * timestamp). Header names are case-insensitive; values may be strings or lists of strings, as in
     * {@code Map<String, String>}, {@code Map<String, List<String>>}, Spring's {@code HttpHeaders} or
     * JAX-RS {@code MultivaluedMap}.
     *
     * @param rawBody the raw request body, exactly as received
     * @param headers the request headers
     * @return the payload
     * @throws WebhookVerificationException when the delivery is not genuine
     */
    public WebhookPayload verify(byte[] rawBody, Map<String, ?> headers) {
        return verifyHeaders(rawBody, name -> mapLookup(headers, name));
    }

    /**
     * @param rawBody the raw request body as a string (UTF-8 is signed)
     * @param headers the request headers
     * @return the payload
     * @throws WebhookVerificationException when the delivery is not genuine
     * @see #verify(byte[], Map)
     */
    public WebhookPayload verify(String rawBody, Map<String, ?> headers) {
        return verifyHeaders(bytes(rawBody), name -> mapLookup(headers, name));
    }

    /**
     * Verifies with a header lookup, for example {@code request::getHeader} of a servlet request.
     *
     * @param rawBody the raw request body, exactly as received
     * @param header returns the value of a header (case-insensitive name), or null when it is missing
     * @return the payload
     * @throws WebhookVerificationException when the delivery is not genuine
     */
    public WebhookPayload verify(byte[] rawBody, Function<String, String> header) {
        return verifyHeaders(rawBody, name -> {
            String value = header == null ? null : header.apply(name);
            return value == null ? Lookup.MISSING : new Lookup(value, false, false);
        });
    }

    private WebhookPayload verifyHeaders(byte[] rawBody, Function<String, Lookup> lookup) {
        Lookup signature = lookup.apply(SIGNATURE_HEADER);
        if (signature.missing) {
            throw new WebhookVerificationException(Reason.SIGNATURE_HEADER_MISSING, "The X-Lettermint-Signature header is missing.");
        }
        if (signature.ambiguous) {
            throw new WebhookVerificationException(Reason.SIGNATURE_HEADER_MALFORMED, "The request has more than one X-Lettermint-Signature header.");
        }
        Lookup delivery = lookup.apply(DELIVERY_HEADER);
        if (delivery.missing) {
            throw new WebhookVerificationException(Reason.DELIVERY_HEADER_MISSING, "The X-Lettermint-Delivery header is missing.");
        }
        if (delivery.ambiguous) {
            throw new WebhookVerificationException(Reason.DELIVERY_TIMESTAMP_MISMATCH, "The request has more than one X-Lettermint-Delivery header.");
        }
        return verifySignature(rawBody, signature.value, delivery.value);
    }

    /**
     * @param rawBody the raw request body as a string
     * @param header returns the value of a header, or null when it is missing
     * @return the payload
     * @throws WebhookVerificationException when the delivery is not genuine
     * @see #verify(byte[], Function)
     */
    public WebhookPayload verify(String rawBody, Function<String, String> header) {
        return verify(bytes(rawBody), header);
    }

    /**
     * Verifies the raw body against an {@code X-Lettermint-Signature} value, for setups where the
     * headers are not at hand.
     *
     * @param rawBody the raw request body
     * @param signatureHeader the {@code X-Lettermint-Signature} value
     * @return the payload
     * @throws WebhookVerificationException when the delivery is not genuine
     */
    public WebhookPayload verifySignature(byte[] rawBody, String signatureHeader) {
        return verifySignature(rawBody, signatureHeader, null);
    }

    /**
     * @param rawBody the raw request body as a string
     * @param signatureHeader the {@code X-Lettermint-Signature} value
     * @return the payload
     * @throws WebhookVerificationException when the delivery is not genuine
     */
    public WebhookPayload verifySignature(String rawBody, String signatureHeader) {
        return verifySignature(bytes(rawBody), signatureHeader, null);
    }

    /**
     * @param rawBody the raw request body as a string
     * @param signatureHeader the {@code X-Lettermint-Signature} value
     * @param deliveryHeader the {@code X-Lettermint-Delivery} value, which must equal the signed timestamp; or null
     * @return the payload
     * @throws WebhookVerificationException when the delivery is not genuine
     */
    public WebhookPayload verifySignature(String rawBody, String signatureHeader, String deliveryHeader) {
        return verifySignature(bytes(rawBody), signatureHeader, deliveryHeader);
    }

    /**
     * Verifies the raw body against an {@code X-Lettermint-Signature} value. When {@code deliveryHeader}
     * (the {@code X-Lettermint-Delivery} value) is given, it must equal the signed timestamp.
     *
     * @param rawBody the raw request body
     * @param signatureHeader the {@code X-Lettermint-Signature} value
     * @param deliveryHeader the {@code X-Lettermint-Delivery} value, or null
     * @return the payload
     * @throws WebhookVerificationException when the delivery is not genuine
     */
    public WebhookPayload verifySignature(byte[] rawBody, String signatureHeader, String deliveryHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new WebhookVerificationException(Reason.SIGNATURE_HEADER_MISSING, "The X-Lettermint-Signature header is missing.");
        }
        Parsed parsed = parse(signatureHeader);
        if (deliveryHeader != null && !deliveryHeader.trim().equals(parsed.timestamp)) {
            throw new WebhookVerificationException(Reason.DELIVERY_TIMESTAMP_MISMATCH, "The X-Lettermint-Delivery header does not match the signed timestamp.");
        }
        if (rawBody == null || rawBody.length == 0) {
            throw new WebhookVerificationException(Reason.BODY_INVALID, rawBody == null
                    ? "Pass the raw request body (a String or byte[]), not parsed JSON."
                    : "The raw request body is empty.");
        }
        long now = clock.instant().getEpochSecond();
        if (Math.abs(now - Long.parseLong(parsed.timestamp)) > tolerance) {
            throw new WebhookVerificationException(Reason.TIMESTAMP_OUT_OF_TOLERANCE, "The signed timestamp is outside the allowed tolerance.");
        }
        byte[] expected = sign(parsed.timestamp, rawBody);
        boolean matched = false;
        for (byte[] candidate : parsed.signatures) {
            matched |= MessageDigest.isEqual(candidate, expected);
        }
        if (!matched) {
            throw new WebhookVerificationException(Reason.SIGNATURE_MISMATCH, "The webhook signature does not match.");
        }
        JsonNode tree;
        try {
            tree = Transport.MAPPER.readTree(rawBody);
        } catch (IOException error) {
            throw new WebhookVerificationException(Reason.PAYLOAD_INVALID, "The webhook payload is not valid JSON.");
        }
        if (tree == null || !tree.isObject()) {
            throw new WebhookVerificationException(Reason.PAYLOAD_INVALID, "The webhook payload is not a JSON object.");
        }
        return new WebhookPayload(Transport.MAPPER.convertValue(tree, OBJECT));
    }

    private byte[] sign(String timestamp, byte[] body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            mac.update((timestamp + ".").getBytes(StandardCharsets.US_ASCII));
            return mac.doFinal(body);
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("HmacSHA256 is not available", error);
        }
    }

    private static byte[] bytes(String body) {
        return body == null ? null : body.getBytes(StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------ headers
    private static final class Lookup {
        static final Lookup MISSING = new Lookup(null, true, false);
        static final Lookup AMBIGUOUS = new Lookup(null, false, true);

        final String value;
        final boolean missing;
        final boolean ambiguous;

        Lookup(String value, boolean missing, boolean ambiguous) {
            this.value = value;
            this.missing = missing;
            this.ambiguous = ambiguous;
        }
    }

    private static Lookup mapLookup(Map<String, ?> headers, String name) {
        if (headers == null) {
            return Lookup.MISSING;
        }
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, ?> entry : headers.entrySet()) {
            if (entry.getKey() == null || !entry.getKey().equalsIgnoreCase(name) || entry.getValue() == null) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof Collection<?> collection) {
                values.addAll(collection);
            } else if (value instanceof Object[] array) {
                values.addAll(Arrays.asList(array));
            } else {
                values.add(value);
            }
        }
        values.removeIf(Objects::isNull);
        if (values.isEmpty()) {
            return Lookup.MISSING;
        }
        if (values.size() > 1 || !(values.get(0) instanceof String)) {
            return Lookup.AMBIGUOUS;
        }
        return new Lookup((String) values.get(0), false, false);
    }

    // ------------------------------------------------------------ signature header
    private record Parsed(String timestamp, List<byte[]> signatures) {
    }

    private static WebhookVerificationException malformed(String detail) {
        return new WebhookVerificationException(Reason.SIGNATURE_HEADER_MALFORMED, "The signature header is malformed: " + detail + ".");
    }

    private static Parsed parse(String header) {
        if (!PRINTABLE_ASCII.matcher(header).matches()) {
            throw malformed("it contains non-ASCII or control characters");
        }
        String timestamp = null;
        List<byte[]> signatures = new ArrayList<>();
        for (String part : header.split(",", -1)) {
            String entry = part.trim();
            int separator = entry.indexOf('=');
            if (separator == -1) {
                continue;
            }
            String name = entry.substring(0, separator);
            String value = entry.substring(separator + 1);
            if (name.equals("t")) {
                if (timestamp != null) {
                    throw malformed("it has more than one timestamp");
                }
                if (!DIGITS.matcher(value).matches() || new BigInteger(value).compareTo(MAX_SAFE_INTEGER) > 0) {
                    throw malformed("the timestamp is not a number of seconds");
                }
                timestamp = value;
            } else if (name.equals("v1") && HEX_SHA256.matcher(value).matches()) {
                signatures.add(hex(value));
            }
        }
        if (timestamp == null) {
            throw malformed("the timestamp (t=) is missing");
        }
        if (signatures.isEmpty()) {
            throw malformed("no v1 signature is present");
        }
        return new Parsed(timestamp, signatures);
    }

    private static byte[] hex(String value) {
        byte[] bytes = new byte[value.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    /** Contains no secret. */
    @Override
    public String toString() {
        return "Webhook{tolerance=" + tolerance + "s}";
    }
}
