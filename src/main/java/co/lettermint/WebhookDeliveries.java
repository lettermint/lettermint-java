package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.ListWebhookDeliveriesQuery;
import co.lettermint.types.Operations;
import co.lettermint.types.WebhookDeliveryData;
import co.lettermint.types.WebhookDeliveryListData;

/** Delivery attempts of a webhook. Needs the team token. Thread-safe. */
public final class WebhookDeliveries {
    private final Transport transport;

    WebhookDeliveries(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists the deliveries of a webhook, one page at a time.
     *
     * @param webhookId the webhook id
     * @return the response
     */
    public CursorPage<WebhookDeliveryListData> list(String webhookId) {
        return list(webhookId, null, null);
    }

    /**
     * Lists the deliveries of a webhook, one page at a time.
     *
     * @param webhookId the webhook id
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<WebhookDeliveryListData> list(String webhookId, ListWebhookDeliveriesQuery query) {
        return list(webhookId, query, null);
    }

    /**
     * Lists the deliveries of a webhook, one page at a time.
     *
     * @param webhookId the webhook id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<WebhookDeliveryListData> list(String webhookId, ListWebhookDeliveriesQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_WEBHOOK_DELIVERIES, Transport.call("webhooks.deliveries.list").path(webhookId).query(query).options(options));
    }

    /**
     * Iterates over every delivery of a webhook, following {@code next_cursor}.
     *
     * @param webhookId the webhook id
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<WebhookDeliveryListData> iterate(String webhookId) {
        return iterate(webhookId, null, null);
    }

    /**
     * Iterates over every delivery of a webhook, following {@code next_cursor}.
     *
     * @param webhookId the webhook id
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<WebhookDeliveryListData> iterate(String webhookId, ListWebhookDeliveriesQuery query) {
        return iterate(webhookId, query, null);
    }

    /**
     * Iterates over every delivery of a webhook, following {@code next_cursor}.
     *
     * @param webhookId the webhook id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<WebhookDeliveryListData> iterate(String webhookId, ListWebhookDeliveriesQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_WEBHOOK_DELIVERIES, Transport.call("webhooks.deliveries.iterate").path(webhookId).query(query).options(options));
    }

    /**
     * Retrieves one delivery attempt.
     *
     * @param webhookId the webhook id
     * @param deliveryId the delivery id
     * @return the response
     */
    public WebhookDeliveryData retrieve(String webhookId, String deliveryId) {
        return retrieve(webhookId, deliveryId, null);
    }

    /**
     * Retrieves one delivery attempt.
     *
     * @param webhookId the webhook id
     * @param deliveryId the delivery id
     * @param options per-call options, or null
     * @return the response
     */
    public WebhookDeliveryData retrieve(String webhookId, String deliveryId, RequestOptions options) {
        return transport.call(Operations.GET_WEBHOOK_DELIVERY, Transport.call("webhooks.deliveries.retrieve").path(webhookId, deliveryId).options(options));
    }

    @Override
    public String toString() {
        return "WebhookDeliveries{}";
    }
}
