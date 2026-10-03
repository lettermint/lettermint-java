package co.lettermint.exceptions;

/**
 * Base class of every exception the SDK throws. Unchecked.
 *
 * <p>No exception carries request headers or API tokens, and no message contains them.
 */
public class LettermintException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the message
     */
    public LettermintException(String message) {
        super(message);
    }

    /**
     * @param message the message
     * @param cause the cause
     */
    public LettermintException(String message, Throwable cause) {
        super(message, cause);
    }
}
