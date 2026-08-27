package co.lettermint.client;

import co.lettermint.BuildInfo;
import co.lettermint.exceptions.HttpRequestException;
import co.lettermint.exceptions.LettermintException;
import co.lettermint.exceptions.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client for Lettermint API requests.
 */
public class LettermintClient {

    private static final String DEFAULT_BASE_URL = "https://api.lettermint.co/v1";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String apiToken;
    private final String baseUrl;
    private final AuthMode authMode;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LettermintClient(String apiToken) {
        this(apiToken, DEFAULT_BASE_URL);
    }

    public LettermintClient(String apiToken, String baseUrl) {
        this(apiToken, baseUrl, AuthMode.SENDING);
    }

    public LettermintClient(String apiToken, String baseUrl, AuthMode authMode) {
        if (apiToken == null || apiToken.isEmpty()) {
            throw new IllegalArgumentException("API token is required");
        }

        this.apiToken = apiToken;
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
        this.authMode = authMode;
        this.objectMapper = new ObjectMapper();
        this.httpClient = buildHttpClient();
    }

    private OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(this::addDefaultHeaders)
                .build();
    }

    private Response addDefaultHeaders(Interceptor.Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", buildUserAgent());

        if (authMode == AuthMode.BEARER) {
            builder.header("Authorization", "Bearer " + apiToken);
            builder.removeHeader("x-lettermint-token");
        } else {
            builder.header("x-lettermint-token", apiToken);
            builder.removeHeader("Authorization");
        }

        return chain.proceed(builder.build());
    }

    private String buildUserAgent() {
        String javaVersion = System.getProperty("java.version", "unknown");
        return String.format("Lettermint/%s (Java; Java %s)", BuildInfo.VERSION, javaVersion);
    }

    public <T> T post(String path, Object payload, Class<T> responseClass) {
        return post(path, payload, responseClass, null);
    }

    public <T> T post(String path, Object payload, Class<T> responseClass, Map<String, String> headers) {
        return request("POST", url(path, null), payload, responseClass, null, headers);
    }

    public <T> T post(String path, Object payload, TypeReference<T> responseType) {
        return post(path, payload, responseType, null);
    }

    public <T> T post(String path, Object payload, TypeReference<T> responseType, Map<String, String> headers) {
        return request("POST", url(path, null), payload, null, responseType, headers);
    }

    public <T> T get(String path, Class<T> responseClass) {
        return get(path, responseClass, null);
    }

    public <T> T get(String path, Class<T> responseClass, Map<String, String> query) {
        return request("GET", url(path, query), null, responseClass, null, null);
    }

    public <T> T put(String path, Object payload, Class<T> responseClass) {
        return request("PUT", url(path, null), payload, responseClass, null, null);
    }

    public <T> T patch(String path, Object payload, Class<T> responseClass) {
        return request("PATCH", url(path, null), payload, responseClass, null, null);
    }

    public <T> T delete(String path, Class<T> responseClass) {
        return request("DELETE", url(path, null), null, responseClass, null, null);
    }

    public String getRaw(String path) {
        Request request = new Request.Builder().url(url(path, null)).get().build();
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                handleErrorResponse(response.code(), responseBody);
            }

            return responseBody;
        } catch (SocketTimeoutException e) {
            throw new LettermintException("Request timed out", e);
        } catch (IOException e) {
            throw new LettermintException("Request failed: " + e.getMessage(), e);
        }
    }

    private <T> T request(String method, String requestUrl, Object payload, Class<T> responseClass, TypeReference<T> responseType, Map<String, String> headers) {
        Request.Builder requestBuilder = new Request.Builder().url(requestUrl);
        RequestBody body = null;

        if (payload != null) {
            try {
                body = RequestBody.create(objectMapper.writeValueAsString(payload), JSON);
            } catch (JsonProcessingException e) {
                throw new LettermintException("Failed to serialize request body", e);
            }
        }

        if ("POST".equals(method)) {
            requestBuilder.post(body != null ? body : RequestBody.create(new byte[0], JSON));
        } else if ("PUT".equals(method)) {
            requestBuilder.put(body != null ? body : RequestBody.create(new byte[0], JSON));
        } else if ("PATCH".equals(method)) {
            requestBuilder.patch(body != null ? body : RequestBody.create(new byte[0], JSON));
        } else if ("DELETE".equals(method)) {
            requestBuilder.delete();
        } else {
            requestBuilder.get();
        }

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                handleErrorResponse(response.code(), responseBody);
            }

            if (responseClass != null) {
                return objectMapper.readValue(responseBody, responseClass);
            }
            return objectMapper.readValue(responseBody, responseType);
        } catch (SocketTimeoutException e) {
            throw new LettermintException("Request timed out", e);
        } catch (IOException e) {
            throw new LettermintException("Request failed: " + e.getMessage(), e);
        }
    }

    private String url(String path) {
        return url(path, null);
    }

    private String url(String path, Map<String, String> query) {
        if (isAbsolutePath(path)) {
            throw new IllegalArgumentException("Request path must be relative");
        }

        StringBuilder result = new StringBuilder(baseUrl.replaceAll("/+$", "") + path);
        if (query != null && !query.isEmpty()) {
            result.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : query.entrySet()) {
                if (!first) {
                    result.append("&");
                }
                first = false;
                result.append(encode(entry.getKey())).append("=").append(encode(entry.getValue()));
            }
        }
        return result.toString();
    }

    private boolean isAbsolutePath(String path) {
        return path.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*") || path.startsWith("//");
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (IOException e) {
            throw new LettermintException("Failed to encode query parameter", e);
        }
    }

    private void handleErrorResponse(int statusCode, String responseBody) {
        String message = extractErrorMessage(responseBody, statusCode);

        if (statusCode == 422) {
            throw new ValidationException(message, responseBody);
        }

        throw new HttpRequestException(message, statusCode, responseBody);
    }

    private String extractErrorMessage(String responseBody, int statusCode) {
        try {
            Map<?, ?> errorMap = objectMapper.readValue(responseBody, Map.class);
            if (errorMap.containsKey("message")) {
                return (String) errorMap.get("message");
            }
            if (errorMap.containsKey("error")) {
                return (String) errorMap.get("error");
            }
        } catch (Exception ignored) {
        }
        return "HTTP " + statusCode + " error";
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public enum AuthMode {
        SENDING,
        BEARER
    }
}
