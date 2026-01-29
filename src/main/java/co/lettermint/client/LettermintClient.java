package co.lettermint.client;

import co.lettermint.BuildInfo;
import co.lettermint.exceptions.HttpRequestException;
import co.lettermint.exceptions.LettermintException;
import co.lettermint.exceptions.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LettermintClient(String apiToken) {
        this(apiToken, DEFAULT_BASE_URL);
    }

    public LettermintClient(String apiToken, String baseUrl) {
        if (apiToken == null || apiToken.isEmpty()) {
            throw new IllegalArgumentException("API token is required");
        }

        this.apiToken = apiToken;
        this.baseUrl = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
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
                .header("x-lettermint-token", apiToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", buildUserAgent());

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
        String url = baseUrl + path;
        String jsonBody;

        try {
            jsonBody = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new LettermintException("Failed to serialize request body", e);
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON));

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

            return objectMapper.readValue(responseBody, responseClass);
        } catch (SocketTimeoutException e) {
            throw new LettermintException("Request timed out", e);
        } catch (IOException e) {
            throw new LettermintException("Request failed: " + e.getMessage(), e);
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
}
