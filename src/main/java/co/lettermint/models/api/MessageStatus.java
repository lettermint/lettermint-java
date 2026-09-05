package co.lettermint.models.api;

public final class MessageStatus {
    public static final String SCHEDULED = "scheduled";
    public static final String PENDING = "pending";
    public static final String QUEUED = "queued";
    public static final String QUARANTINED = "quarantined";
    public static final String SUPPRESSED = "suppressed";
    public static final String PROCESSED = "processed";
    public static final String DELIVERED = "delivered";
    public static final String OPENED = "opened";
    public static final String CLICKED = "clicked";
    public static final String SOFTBOUNCED = "soft_bounced";
    public static final String HARDBOUNCED = "hard_bounced";
    public static final String SPAMCOMPLAINT = "spam_complaint";
    public static final String FAILED = "failed";
    public static final String BLOCKED = "blocked";
    public static final String POLICYREJECTED = "policy_rejected";
    public static final String UNSUBSCRIBED = "unsubscribed";
    public static final String CANCELED = "canceled";

    private MessageStatus() {}
}
