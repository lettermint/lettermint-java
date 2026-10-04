package co.lettermint.exceptions;

/** A webhook delivery could not be verified. Reject the request; do not process its payload. */
public class WebhookVerificationException extends LettermintException {
    private static final long serialVersionUID = 1L;

    /** Why a webhook delivery failed verification. */
    public enum Reason {
        /** The {@code X-Lettermint-Signature} header is missing or empty. */
        SIGNATURE_HEADER_MISSING("signature_header_missing"),
        /** The signature header is malformed, non-ASCII, repeated or has no {@code t} or {@code v1}. */
        SIGNATURE_HEADER_MALFORMED("signature_header_malformed"),
        /** The {@code X-Lettermint-Delivery} header is missing. */
        DELIVERY_HEADER_MISSING("delivery_header_missing"),
        /** The {@code X-Lettermint-Delivery} header differs from the signed timestamp, or is repeated. */
        DELIVERY_TIMESTAMP_MISMATCH("delivery_timestamp_mismatch"),
        /** The signed timestamp is outside the tolerance. */
        TIMESTAMP_OUT_OF_TOLERANCE("timestamp_out_of_tolerance"),
        /** No {@code v1} signature matches. */
        SIGNATURE_MISMATCH("signature_mismatch"),
        /** The raw body is missing or empty. */
        BODY_INVALID("body_invalid"),
        /** The payload is not a JSON object. */
        PAYLOAD_INVALID("payload_invalid");

        private final String code;

        Reason(String code) {
            this.code = code;
        }

        /**
         * @return the reason code shared by every Lettermint SDK, for example {@code signature_mismatch}
         */
        public String code() {
            return code;
        }
    }

    private final Reason reason;

    /**
     * @param reason why verification failed
     * @param message the message
     */
    public WebhookVerificationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    /**
     * @return why verification failed
     */
    public Reason getReason() {
        return reason;
    }
}
