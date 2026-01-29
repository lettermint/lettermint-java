package co.lettermint.exceptions;

/**
 * Exception thrown when an HTTP request fails.
 */
public class HttpRequestException extends LettermintException {

    private final int statusCode;
    private final String responseBody;

    public HttpRequestException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpRequestException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
