package co.lettermint.exceptions;

/**
 * The SDK rejected a request before sending it, for example because of invalid message tags. Unlike
 * {@link ValidationException}, the API never saw this request.
 */
public class LettermintValidationException extends LettermintException {
    private static final long serialVersionUID = 1L;

    private final String field;

    /**
     * @param message the message
     * @param field the offending field, for example {@code tags} or {@code messages[2].tags}
     */
    public LettermintValidationException(String message, String field) {
        super(message);
        this.field = field;
    }

    /**
     * @return the offending field, for example {@code tags} or {@code messages[2].tags}, or null
     */
    public String getField() {
        return field;
    }
}
