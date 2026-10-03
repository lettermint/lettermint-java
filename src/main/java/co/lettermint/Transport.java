package co.lettermint;

import co.lettermint.exceptions.ApiException;
import co.lettermint.exceptions.AuthenticationException;
import co.lettermint.exceptions.ConflictException;
import co.lettermint.exceptions.ConnectionException;
import co.lettermint.exceptions.LettermintConfigException;
import co.lettermint.exceptions.LettermintException;
import co.lettermint.exceptions.LettermintValidationException;
import co.lettermint.exceptions.NotFoundException;
import co.lettermint.exceptions.PermissionException;
import co.lettermint.exceptions.RateLimitException;
import co.lettermint.exceptions.RedirectException;
import co.lettermint.exceptions.ServerException;
import co.lettermint.exceptions.TimeoutException;
import co.lettermint.exceptions.UnexpectedResponseException;
import co.lettermint.exceptions.ValidationException;
import co.lettermint.types.CursorPage;
import co.lettermint.types.Operations.AuthSurface;
import co.lettermint.types.Operations.Operation;
import co.lettermint.types.Operations.ResponseKind;
import co.lettermint.types.QueryParameters;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sends requests for one client. Holds the tokens; nothing outside this package can read them.
 * Immutable and thread-safe.
 */
final class Transport {
    static final String DEFAULT_BASE_URL = "https://api.lettermint.co/v1";
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    static final String USER_AGENT = "lettermint-java/" + BuildInfo.VERSION;
    static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");
    private static final TypeReference<Map<String, List<String>>> FIELD_ERRORS = new TypeReference<>() {};

    private final Secret sendingToken;
    private final Secret teamToken;
    private final HttpClient http;
    private final String baseUrl;
    private final Duration timeout;

    Transport(Secret sendingToken, Secret teamToken, HttpClient http, String baseUrl, Duration timeout) {
        this.sendingToken = sendingToken;
        this.teamToken = teamToken;
        this.http = http;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
    }

    String baseUrl() {
        return baseUrl;
    }

    Duration timeout() {
        return timeout;
    }

    boolean hasSendingToken() {
        return sendingToken != null;
    }

    boolean hasTeamToken() {
        return teamToken != null;
    }

    // ------------------------------------------------------------ checks
    static Duration checkTimeout(Duration value, String option) {
        if (value == null || value.isNegative() || value.isZero()) {
            throw new LettermintConfigException(option + " must be a positive duration.");
        }
        return value;
    }

    static String checkBaseUrl(String value) {
        if (value == null) {
            throw new LettermintConfigException("baseUrl must not be null.");
        }
        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException error) {
            throw new LettermintConfigException("baseUrl must be an absolute http(s) URL.");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("https") || scheme.equalsIgnoreCase("http")) || uri.getHost() == null) {
            throw new LettermintConfigException("baseUrl must be an absolute http(s) URL.");
        }
        if (uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new LettermintConfigException("baseUrl must not contain credentials, a query string or a fragment.");
        }
        return value.replaceAll("/+$", "");
    }

    /** Throws if the token for {@code auth} is not configured. */
    void assertAuth(String label, AuthSurface auth) {
        authHeader(label, auth);
    }

    private String[] authHeader(String label, AuthSurface auth) {
        boolean useTeam = auth == AuthSurface.TEAM || (auth == AuthSurface.EITHER && teamToken != null);
        if (useTeam) {
            if (teamToken == null) {
                throw new LettermintConfigException(label + " needs teamToken; set it with Lettermint.builder().teamToken(...).");
            }
            return new String[] {"Authorization", "Bearer " + teamToken.reveal()};
        }
        if (sendingToken == null) {
            throw new LettermintConfigException(label + " needs sendingToken; set it with Lettermint.builder().sendingToken(...).");
        }
        return new String[] {"x-lettermint-token", sendingToken.reveal()};
    }

    private static String encodePathParam(String label, String name, String value) {
        if (value == null || value.isEmpty() || value.equals(".") || value.equals("..")) {
            throw new LettermintConfigException(label + ": " + name + " must be a non-empty string other than \".\" and \"..\".");
        }
        StringBuilder encoded = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xff);
            // encodeURIComponent: unreserved characters plus !'()*
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || "-_.!~*'()".indexOf(c) >= 0) {
                encoded.append(c);
            } else {
                encoded.append('%').append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xf, 16)))
                        .append(Character.toUpperCase(Character.forDigit(b & 0xf, 16)));
            }
        }
        return encoded.toString();
    }

    private static void checkIdempotencyKey(String key) {
        boolean valid = !key.isEmpty();
        for (int i = 0; valid && i < key.length(); i++) {
            char c = key.charAt(i);
            valid = (c >= 0x20 || c == '\t') && c != 0x7f && c <= 0xff;
        }
        if (!valid) {
            throw new LettermintValidationException("idempotencyKey must be a non-empty string without line breaks or control characters.", "idempotencyKey");
        }
    }

    // ------------------------------------------------------------ calls
    /** One call: the public method name for messages, path values in order, query, body and options. */
    static final class Call {
        final String label;
        String[] path = new String[0];
        QueryParameters query;
        Object body;
        String idempotencyKey;
        Duration timeout;
        AuthSurface auth;

        Call(String label) {
            this.label = label;
        }

        Call path(String... values) {
            this.path = values;
            return this;
        }

        Call query(QueryParameters value) {
            this.query = value;
            return this;
        }

        Call body(Object value) {
            this.body = value;
            return this;
        }

        Call auth(AuthSurface value) {
            this.auth = value;
            return this;
        }

        Call options(RequestOptions options) {
            this.timeout = options == null ? null : options.timeout();
            return this;
        }

        Call options(SendOptions options) {
            this.timeout = options == null ? null : options.timeout();
            this.idempotencyKey = options == null ? null : options.idempotencyKey();
            return this;
        }
    }

    static Call call(String label) {
        return new Call(label);
    }

    <R> R call(Operation<R> operation, Call call) {
        Prepared prepared = prepare(operation, call);
        return execute(operation, call, prepared, call.query == null ? null : call.query.toParameters());
    }

    /** Follows {@code next_cursor} through every page. Checks the token and path eagerly. */
    <T> CursorIterable<T> paginate(Operation<CursorPage<T>> operation, Call call) {
        if (operation.pagination() == null) {
            throw new IllegalStateException(operation.key() + " is not cursor-paginated");
        }
        String cursorParam = operation.pagination().cursorParam();
        Prepared prepared = prepare(operation, call);
        return new CursorIterable<>(cursor -> {
            Map<String, Object> parameters = call.query == null ? new LinkedHashMap<>() : call.query.toParameters();
            if (cursor != null) {
                if (cursorParam.indexOf('[') < 0) {
                    parameters.remove(cursorParam); // like the Node SDK: a flat cursor goes last
                }
                parameters.put(cursorParam, cursor);
            }
            CursorPage<T> page = execute(operation, call, prepared, parameters);
            if (page == null || page.data() == null) {
                throw new UnexpectedResponseException(call.label + ": the API returned a page without a data array.", 200, "");
            }
            return page;
        });
    }

    private record Prepared(String path, String[] auth, Duration timeout) {
    }

    private Prepared prepare(Operation<?> operation, Call call) {
        String[] auth = authHeader(call.label, call.auth != null ? call.auth : operation.auth());
        List<String> names = operation.pathParams();
        if (call.path.length != names.size()) {
            throw new IllegalStateException(call.label + ": expected " + names.size() + " path parameters");
        }
        Matcher matcher = PLACEHOLDER.matcher(operation.path());
        StringBuilder path = new StringBuilder();
        while (matcher.find()) {
            int index = names.indexOf(matcher.group(1));
            matcher.appendReplacement(path, Matcher.quoteReplacement(encodePathParam(call.label, matcher.group(1), call.path[index])));
        }
        matcher.appendTail(path);
        Duration timeout = call.timeout == null ? this.timeout : checkTimeout(call.timeout, "timeout");
        if (call.idempotencyKey != null) {
            checkIdempotencyKey(call.idempotencyKey);
        }
        return new Prepared(path.toString(), auth, timeout);
    }

    private <R> R execute(Operation<R> operation, Call call, Prepared prepared, Map<String, Object> parameters) {
        String query = QueryString.serialize(parameters, MAPPER);
        URI uri = URI.create(baseUrl + prepared.path() + (query.isEmpty() ? "" : "?" + query));
        HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT);
        if (call.idempotencyKey != null) {
            request.header("Idempotency-Key", call.idempotencyKey);
        }
        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.noBody();
        if (call.body != null) {
            try {
                publisher = HttpRequest.BodyPublishers.ofByteArray(MAPPER.writeValueAsBytes(call.body));
            } catch (JsonProcessingException error) {
                throw new LettermintException(call.label + ": the request body could not be encoded as JSON.", error);
            }
            request.header("Content-Type", "application/json");
        }
        request.header(prepared.auth()[0], prepared.auth()[1]);
        request.method(operation.method(), publisher);

        HttpResponse<byte[]> response = send(request.build(), prepared.timeout());
        int status = response.statusCode();
        if (status >= 300 && status < 400) {
            throw new RedirectException(status);
        }
        String text = new String(response.body() == null ? new byte[0] : response.body(), StandardCharsets.UTF_8);
        return decode(operation, response, status, text);
    }

    private HttpResponse<byte[]> send(HttpRequest request, Duration timeout) {
        CompletableFuture<HttpResponse<byte[]>> future;
        try {
            future = http.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IllegalArgumentException error) {
            throw new ConnectionException(error);
        }
        try {
            // The deadline covers connecting, sending, the headers and the whole body.
            return future.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (java.util.concurrent.TimeoutException error) {
            future.cancel(true);
            throw new TimeoutException(timeout);
        } catch (InterruptedException error) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            CancellationException cancelled = new CancellationException("The Lettermint request was cancelled because the thread was interrupted.");
            cancelled.initCause(error);
            throw cancelled;
        } catch (ExecutionException error) {
            Throwable cause = error.getCause();
            if (cause instanceof HttpTimeoutException) {
                throw new TimeoutException(timeout);
            }
            throw new ConnectionException(cause);
        } catch (CancellationException error) {
            throw new ConnectionException(error);
        }
    }

    private static <R> R decode(Operation<R> operation, HttpResponse<byte[]> response, int status, String text) {
        if (status >= 200 && status < 300) {
            if (operation.responseKind() == ResponseKind.EMPTY || status == 204 || status == 205) {
                return null;
            }
            if (operation.responseKind() == ResponseKind.TEXT) {
                @SuppressWarnings("unchecked")
                R result = (R) text;
                return result;
            }
            if (text.isBlank()) {
                throw new UnexpectedResponseException("The Lettermint API answered with HTTP " + status + " and an empty body where JSON was expected.", status, text);
            }
            JsonNode tree;
            try {
                tree = MAPPER.readTree(text);
            } catch (JsonProcessingException error) {
                throw new UnexpectedResponseException("The Lettermint API answered with HTTP " + status + " and a body that is not valid JSON.", status, text);
            }
            try {
                return MAPPER.readerFor(MAPPER.getTypeFactory().constructType(operation.responseType())).readValue(tree);
            } catch (IOException | IllegalArgumentException error) {
                throw new UnexpectedResponseException("The Lettermint API answered with HTTP " + status + " and a body that does not match " + operation.key() + ".", status, text);
            }
        }
        if (status < 400) {
            throw new UnexpectedResponseException("The Lettermint API answered with an unexpected HTTP status " + status + ".", status, text);
        }
        JsonNode body = null;
        if (!text.isBlank()) {
            try {
                body = MAPPER.readTree(text);
            } catch (JsonProcessingException error) {
                String contentType = response.headers().firstValue("content-type").map(value -> " (" + value.split(";")[0].trim() + ")").orElse("");
                throw new UnexpectedResponseException("The Lettermint API answered with HTTP " + status + " and a body that is not JSON" + contentType + ".", status, text);
            }
        }
        throw apiException(status, body, response.headers().firstValue("retry-after").orElse(null));
    }

    static ApiException apiException(int status, JsonNode body, String retryAfter) {
        String message = null;
        String code = null;
        Object details = null;
        Map<String, List<String>> errors = null;
        if (body != null && body.isObject()) {
            JsonNode error = body.get("error");
            if (error != null && error.isObject()) {
                if (error.path("code").isTextual()) {
                    code = error.get("code").textValue();
                }
                if (error.path("message").isTextual()) {
                    message = error.get("message").textValue();
                }
                details = error.has("details") ? MAPPER.convertValue(error.get("details"), Object.class) : null;
            } else if (error != null && error.isTextual()) {
                code = error.textValue();
            }
            if ((message == null || message.isEmpty()) && body.path("message").isTextual()) {
                message = body.get("message").textValue();
            }
            if (body.path("errors").isObject()) {
                try {
                    errors = MAPPER.convertValue(body.get("errors"), FIELD_ERRORS);
                } catch (IllegalArgumentException ignored) {
                    errors = null;
                }
            }
        }
        if (message == null || message.isEmpty()) {
            message = "HTTP " + status;
        }
        Object decoded = body == null ? null : MAPPER.convertValue(body, Object.class);
        switch (status) {
            case 401:
                return new AuthenticationException(status, message, code, details, decoded);
            case 403:
                return new PermissionException(status, message, code, details, decoded);
            case 404:
                return new NotFoundException(status, message, code, details, decoded);
            case 409:
                return new ConflictException(status, message, code, details, decoded);
            case 422:
                return new ValidationException(status, message, code, details, decoded, errors);
            case 429:
                return new RateLimitException(status, message, code, details, decoded, parseRetryAfter(retryAfter));
            default:
                return status >= 500 ? new ServerException(status, message, code, details, decoded) : new ApiException(status, message, code, details, decoded);
        }
    }

    static Duration parseRetryAfter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (DIGITS.matcher(trimmed).matches()) {
            try {
                return Duration.ofSeconds(Long.parseLong(trimmed));
            } catch (NumberFormatException error) {
                return null;
            }
        }
        try {
            ZonedDateTime date = ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME);
            long millis = date.toInstant().toEpochMilli() - System.currentTimeMillis();
            return Duration.ofSeconds(Math.max(0, (millis + 999) / 1000));
        } catch (DateTimeParseException error) {
            return null;
        }
    }
}
