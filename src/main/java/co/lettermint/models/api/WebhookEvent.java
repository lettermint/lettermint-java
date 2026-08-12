package co.lettermint.models.api;

public final class WebhookEvent {
    public static final String MESSAGECREATED = "message.created";
    public static final String MESSAGESENT = "message.sent";
    public static final String MESSAGEDELIVERED = "message.delivered";
    public static final String MESSAGEAUTOREPLIED = "message.auto_replied";
    public static final String MESSAGEHARDBOUNCED = "message.hard_bounced";
    public static final String MESSAGESOFTBOUNCED = "message.soft_bounced";
    public static final String MESSAGESPAMCOMPLAINT = "message.spam_complaint";
    public static final String MESSAGEFAILED = "message.failed";
    public static final String MESSAGESUPPRESSED = "message.suppressed";
    public static final String MESSAGEUNSUBSCRIBED = "message.unsubscribed";
    public static final String MESSAGEOPENED = "message.opened";
    public static final String MESSAGECLICKED = "message.clicked";
    public static final String MESSAGEINBOUND = "message.inbound";
    public static final String MESSAGEPOLICYREJECTED = "message.policy_rejected";
    public static final String SUPPRESSIONADDED = "suppression.added";
    public static final String SUPPRESSIONREMOVED = "suppression.removed";
    public static final String WEBHOOKTEST = "webhook.test";

    private WebhookEvent() {}
}
