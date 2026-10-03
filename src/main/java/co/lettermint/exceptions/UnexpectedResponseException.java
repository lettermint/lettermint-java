package co.lettermint.exceptions;

/**
 * The response could not be decoded: an empty or non-JSON body where JSON was expected, a body of the
 * wrong shape, an unexpected status, or an error status with a non-JSON body such as a proxy's HTML page.
 */
public class UnexpectedResponseException extends LettermintException {
    private static final long serialVersionUID = 1L;

    private static final int EXCERPT = 200;

    private final int status;
    private final String bodyExcerpt;

    /**
     * @param message the message
     * @param status the HTTP status
     * @param body the response body; only the first 200 characters are kept
     */
    public UnexpectedResponseException(String message, int status, String body) {
        super(message);
        this.status = status;
        String text = body == null ? "" : body;
        this.bodyExcerpt = text.length() > EXCERPT ? text.substring(0, EXCERPT) + "…" : text;
    }

    /**
     * @return the HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the first 200 characters of the response body
     */
    public String getBodyExcerpt() {
        return bodyExcerpt;
    }
}
