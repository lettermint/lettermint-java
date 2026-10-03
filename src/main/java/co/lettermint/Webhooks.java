package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.ListWebhooksQuery;
import co.lettermint.types.MessageResponse;
import co.lettermint.types.Operations;
import co.lettermint.types.StoreWebhookData;
import co.lettermint.types.TestWebhookResponse;
import co.lettermint.types.UpdateWebhookData;
import co.lettermint.types.WebhookData;
import co.lettermint.types.WebhookListData;
import co.lettermint.types.WebhookMutationResponse;
import co.lettermint.types.WebhookSecretResponse;

/** Webhook endpoints. Needs the team token. To verify incoming deliveries, use {@link Webhook}. Thread-safe. */
public final class Webhooks {
    private final Transport transport;
    private final WebhookDeliveries deliveries;

    Webhooks(Transport transport) {
        this.transport = transport;
        this.deliveries = new WebhookDeliveries(transport);
    }

    /**
     * @return delivery attempts of a webhook
     */
    public WebhookDeliveries deliveries() {
        return deliveries;
    }

    /**
     * Lists webhooks, one page at a time.
     *
     * @return the response
     */
    public CursorPage<WebhookListData> list() {
        return list(null, null);
    }

    /**
     * Lists webhooks, one page at a time.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<WebhookListData> list(ListWebhooksQuery query) {
        return list(query, null);
    }

    /**
     * Lists webhooks, one page at a time.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<WebhookListData> list(ListWebhooksQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_WEBHOOKS, Transport.call("webhooks.list").query(query).options(options));
    }

    /**
     * Iterates over every webhook, following {@code next_cursor}.
     *
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<WebhookListData> iterate() {
        return iterate(null, null);
    }

    /**
     * Iterates over every webhook, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<WebhookListData> iterate(ListWebhooksQuery query) {
        return iterate(query, null);
    }

    /**
     * Iterates over every webhook, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<WebhookListData> iterate(ListWebhooksQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_WEBHOOKS, Transport.call("webhooks.iterate").query(query).options(options));
    }

    /**
     * Creates a webhook. The response holds its signing secret once.
     *
     * @param body the request body
     * @return the response
     */
    public WebhookSecretResponse create(StoreWebhookData body) {
        return create(body, null);
    }

    /**
     * Creates a webhook. The response holds its signing secret once.
     *
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public WebhookSecretResponse create(StoreWebhookData body, RequestOptions options) {
        return transport.call(Operations.CREATE_WEBHOOK, Transport.call("webhooks.create").body(body).options(options));
    }

    /**
     * Retrieves a webhook.
     *
     * @param webhookId the webhook id
     * @return the response
     */
    public WebhookData retrieve(String webhookId) {
        return retrieve(webhookId, null);
    }

    /**
     * Retrieves a webhook.
     *
     * @param webhookId the webhook id
     * @param options per-call options, or null
     * @return the response
     */
    public WebhookData retrieve(String webhookId, RequestOptions options) {
        return transport.call(Operations.GET_WEBHOOK, Transport.call("webhooks.retrieve").path(webhookId).options(options));
    }

    /**
     * Updates a webhook.
     *
     * @param webhookId the webhook id
     * @param body the request body
     * @return the response
     */
    public WebhookMutationResponse update(String webhookId, UpdateWebhookData body) {
        return update(webhookId, body, null);
    }

    /**
     * Updates a webhook.
     *
     * @param webhookId the webhook id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public WebhookMutationResponse update(String webhookId, UpdateWebhookData body, RequestOptions options) {
        return transport.call(Operations.UPDATE_WEBHOOK, Transport.call("webhooks.update").path(webhookId).body(body).options(options));
    }

    /**
     * Deletes a webhook.
     *
     * @param webhookId the webhook id
     * @return the response
     */
    public MessageResponse delete(String webhookId) {
        return delete(webhookId, null);
    }

    /**
     * Deletes a webhook.
     *
     * @param webhookId the webhook id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageResponse delete(String webhookId, RequestOptions options) {
        return transport.call(Operations.DELETE_WEBHOOK, Transport.call("webhooks.delete").path(webhookId).options(options));
    }

    /**
     * Sends a {@code webhook.test} delivery.
     *
     * @param webhookId the webhook id
     * @return the response
     */
    public TestWebhookResponse test(String webhookId) {
        return test(webhookId, null);
    }

    /**
     * Sends a {@code webhook.test} delivery.
     *
     * @param webhookId the webhook id
     * @param options per-call options, or null
     * @return the response
     */
    public TestWebhookResponse test(String webhookId, RequestOptions options) {
        return transport.call(Operations.TEST_WEBHOOK, Transport.call("webhooks.test").path(webhookId).options(options));
    }

    /**
     * Replaces the signing secret. The response holds the new secret once.
     *
     * @param webhookId the webhook id
     * @return the response
     */
    public WebhookSecretResponse regenerateSecret(String webhookId) {
        return regenerateSecret(webhookId, null);
    }

    /**
     * Replaces the signing secret. The response holds the new secret once.
     *
     * @param webhookId the webhook id
     * @param options per-call options, or null
     * @return the response
     */
    public WebhookSecretResponse regenerateSecret(String webhookId, RequestOptions options) {
        return transport.call(Operations.REGENERATE_WEBHOOK_SECRET, Transport.call("webhooks.regenerateSecret").path(webhookId).options(options));
    }

    @Override
    public String toString() {
        return "Webhooks{}";
    }
}
