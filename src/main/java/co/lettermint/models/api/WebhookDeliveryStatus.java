package co.lettermint.models.api;

public final class WebhookDeliveryStatus {
    public static final String PENDING = "pending";
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";
    public static final String CLIENTERROR = "client_error";
    public static final String SERVERERROR = "server_error";
    public static final String TIMEOUT = "timeout";

    private WebhookDeliveryStatus() {}
}
