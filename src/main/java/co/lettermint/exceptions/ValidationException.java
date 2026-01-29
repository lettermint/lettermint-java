package co.lettermint.exceptions;

/**
 * Exception thrown when API returns validation errors (HTTP 422).
 */
public class ValidationException extends HttpRequestException {

    public ValidationException(String message, String responseBody) {
        super(message, 422, responseBody);
    }

    public ValidationException(String message, String responseBody, Throwable cause) {
        super(message, 422, responseBody, cause);
    }
}
