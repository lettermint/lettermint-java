package co.lettermint.models.api;

public final class MessageEventType {
    public static final String SCHEDULED = "scheduled";
    public static final String RESCHEDULED = "rescheduled";
    public static final String CANCELED = "canceled";
    public static final String RELEASED = "released";
    public static final String QUEUED = "queued";
    public static final String PROCESSED = "processed";
    public static final String SUPPRESSED = "suppressed";
    public static final String DELIVERED = "delivered";
    public static final String AUTOREPLIED = "auto_replied";
    public static final String SOFTBOUNCED = "soft_bounced";
    public static final String HARDBOUNCED = "hard_bounced";
    public static final String SPAMCOMPLAINT = "spam_complaint";
    public static final String FAILED = "failed";
    public static final String BLOCKED = "blocked";
    public static final String POLICYREJECTED = "policy_rejected";
    public static final String UNSUBSCRIBED = "unsubscribed";
    public static final String OPENED = "opened";
    public static final String CLICKED = "clicked";
    public static final String INBOUNDRECEIVED = "inbound_received";
    public static final String INBOUNDQUEUED = "inbound_queued";
    public static final String INBOUNDSPAMBLOCKED = "inbound_spam_blocked";
    public static final String INBOUNDPROCESSED = "inbound_processed";
    public static final String INBOUNDRETRY = "inbound_retry";

    private MessageEventType() {}
}
