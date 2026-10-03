package co.lettermint.exceptions;

/** The request could not be sent or the connection failed (DNS, TLS, refused, reset). */
public class ConnectionException extends LettermintException {
    private static final long serialVersionUID = 1L;

    /**
     * @param cause the I/O failure
     */
    public ConnectionException(Throwable cause) {
        super("Could not reach the Lettermint API" + (cause != null && cause.getMessage() != null ? ": " + cause.getMessage() : "."), cause);
    }
}
