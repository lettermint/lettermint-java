package co.lettermint.exceptions;

/**
 * The API answered with a redirect (3xx). The SDK never follows redirects, so that tokens are never
 * sent to another location.
 */
public class RedirectException extends LettermintException {
    private static final long serialVersionUID = 1L;

    private final int status;

    /**
     * @param status the HTTP status
     */
    public RedirectException(int status) {
        super("The Lettermint API answered with a redirect (HTTP " + status + "). Redirects are not followed; check the baseUrl option.");
        this.status = status;
    }

    /**
     * @return the HTTP status code
     */
    public int getStatus() {
        return status;
    }
}
