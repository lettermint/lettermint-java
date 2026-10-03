package co.lettermint.exceptions;

/**
 * The client was configured or called incorrectly: a missing or unrecognised token, a token that the
 * called method cannot use, an invalid option or an invalid path parameter. Thrown before any request.
 */
public class LettermintConfigException extends LettermintException {
    private static final long serialVersionUID = 1L;

    /**
     * @param message the message
     */
    public LettermintConfigException(String message) {
        super(message);
    }
}
