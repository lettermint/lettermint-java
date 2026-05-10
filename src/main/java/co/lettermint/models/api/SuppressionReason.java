package co.lettermint.models.api;

public final class SuppressionReason {
    public static final String SPAMCOMPLAINT = "spam_complaint";
    public static final String HARDBOUNCE = "hard_bounce";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String MANUAL = "manual";

    private SuppressionReason() {}
}
