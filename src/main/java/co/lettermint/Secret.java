package co.lettermint;

/**
 * A credential. {@code toString()} never shows it, so reflective dumps and string concatenation print
 * {@code [redacted]}; only the transport reads the value.
 */
final class Secret {
    static final String REDACTED = "[redacted]";

    private final String value;

    Secret(String value) {
        this.value = value;
    }

    String reveal() {
        return value;
    }

    @Override
    public String toString() {
        return REDACTED;
    }
}
