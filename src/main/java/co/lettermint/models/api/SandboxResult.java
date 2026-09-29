package co.lettermint.models.api;

public final class SandboxResult {
    public static final String DELIVERED = "delivered";
    public static final String HARDBOUNCED = "hard_bounced";
    public static final String SOFTBOUNCED = "soft_bounced";
    public static final String DEFERRED = "deferred";
    public static final String FAILED = "failed";
    public static final String SUPPRESSED = "suppressed";
    public static final String SPAMCOMPLAINT = "spam_complaint";
    public static final String AUTOREPLIED = "auto_replied";
    public static final String OPENED = "opened";
    public static final String CLICKED = "clicked";
    public static final String UNSUBSCRIBED = "unsubscribed";

    private SandboxResult() {}
}
