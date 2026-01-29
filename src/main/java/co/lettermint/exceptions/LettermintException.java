package co.lettermint.exceptions;

/**
 * Base exception for all Lettermint SDK errors.
 */
public class LettermintException extends RuntimeException {

    public LettermintException(String message) {
        super(message);
    }

    public LettermintException(String message, Throwable cause) {
        super(message, cause);
    }
}
