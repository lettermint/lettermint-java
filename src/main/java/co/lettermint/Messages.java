package co.lettermint;

import co.lettermint.types.CursorPage;
import co.lettermint.types.ListMessageEventsQuery;
import co.lettermint.types.ListMessagesQuery;
import co.lettermint.types.MessageData;
import co.lettermint.types.MessageEventData;
import co.lettermint.types.MessageListData;
import co.lettermint.types.Operations;
import co.lettermint.types.ProcessInboundMessageResponse;
import co.lettermint.types.RescheduleMessageRequest;
import co.lettermint.types.ScheduledMessage;

/** Sent and received messages. Needs the team token; {@code reschedule} and {@code cancel} also accept the sending token when no team token is configured. Thread-safe. */
public final class Messages {
    private final Transport transport;

    Messages(Transport transport) {
        this.transport = transport;
    }

    /**
     * Lists messages, one page at a time.
     *
     * @return the response
     */
    public CursorPage<MessageListData> list() {
        return list(null, null);
    }

    /**
     * Lists messages, one page at a time.
     *
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<MessageListData> list(ListMessagesQuery query) {
        return list(query, null);
    }

    /**
     * Lists messages, one page at a time.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<MessageListData> list(ListMessagesQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_MESSAGES, Transport.call("messages.list").query(query).options(options));
    }

    /**
     * Iterates over every message, following {@code next_cursor}.
     *
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<MessageListData> iterate() {
        return iterate(null, null);
    }

    /**
     * Iterates over every message, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<MessageListData> iterate(ListMessagesQuery query) {
        return iterate(query, null);
    }

    /**
     * Iterates over every message, following {@code next_cursor}.
     *
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<MessageListData> iterate(ListMessagesQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_MESSAGES, Transport.call("messages.iterate").query(query).options(options));
    }

    /**
     * Retrieves a message.
     *
     * @param messageId the message id
     * @return the response
     */
    public MessageData retrieve(String messageId) {
        return retrieve(messageId, null);
    }

    /**
     * Retrieves a message.
     *
     * @param messageId the message id
     * @param options per-call options, or null
     * @return the response
     */
    public MessageData retrieve(String messageId, RequestOptions options) {
        return transport.call(Operations.GET_MESSAGE, Transport.call("messages.retrieve").path(messageId).options(options));
    }

    /**
     * Lists the events of a message, one page at a time.
     *
     * @param messageId the message id
     * @return the response
     */
    public CursorPage<MessageEventData> events(String messageId) {
        return events(messageId, null, null);
    }

    /**
     * Lists the events of a message, one page at a time.
     *
     * @param messageId the message id
     * @param query the query parameters, or null
     * @return the response
     */
    public CursorPage<MessageEventData> events(String messageId, ListMessageEventsQuery query) {
        return events(messageId, query, null);
    }

    /**
     * Lists the events of a message, one page at a time.
     *
     * @param messageId the message id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return the response
     */
    public CursorPage<MessageEventData> events(String messageId, ListMessageEventsQuery query, RequestOptions options) {
        return transport.call(Operations.LIST_MESSAGE_EVENTS, Transport.call("messages.events").path(messageId).query(query).options(options));
    }

    /**
     * Iterates over every event of a message, following {@code next_cursor}.
     *
     * @param messageId the message id
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<MessageEventData> iterateEvents(String messageId) {
        return iterateEvents(messageId, null, null);
    }

    /**
     * Iterates over every event of a message, following {@code next_cursor}.
     *
     * @param messageId the message id
     * @param query the query parameters, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<MessageEventData> iterateEvents(String messageId, ListMessageEventsQuery query) {
        return iterateEvents(messageId, query, null);
    }

    /**
     * Iterates over every event of a message, following {@code next_cursor}.
     *
     * @param messageId the message id
     * @param query the query parameters, or null
     * @param options per-call options, or null
     * @return every item, fetched page by page as you iterate
     */
    public CursorIterable<MessageEventData> iterateEvents(String messageId, ListMessageEventsQuery query, RequestOptions options) {
        return transport.paginate(Operations.LIST_MESSAGE_EVENTS, Transport.call("messages.iterateEvents").path(messageId).query(query).options(options));
    }

    /**
     * The raw RFC 822 source.
     *
     * @param messageId the message id
     * @return the body as text
     */
    public String source(String messageId) {
        return source(messageId, null);
    }

    /**
     * The raw RFC 822 source.
     *
     * @param messageId the message id
     * @param options per-call options, or null
     * @return the body as text
     */
    public String source(String messageId, RequestOptions options) {
        return transport.call(Operations.GET_MESSAGE_SOURCE, Transport.call("messages.source").path(messageId).options(options));
    }

    /**
     * The HTML body.
     *
     * @param messageId the message id
     * @return the body as text
     */
    public String html(String messageId) {
        return html(messageId, null);
    }

    /**
     * The HTML body.
     *
     * @param messageId the message id
     * @param options per-call options, or null
     * @return the body as text
     */
    public String html(String messageId, RequestOptions options) {
        return transport.call(Operations.GET_MESSAGE_HTML, Transport.call("messages.html").path(messageId).options(options));
    }

    /**
     * The plain-text body.
     *
     * @param messageId the message id
     * @return the body as text
     */
    public String text(String messageId) {
        return text(messageId, null);
    }

    /**
     * The plain-text body.
     *
     * @param messageId the message id
     * @param options per-call options, or null
     * @return the body as text
     */
    public String text(String messageId, RequestOptions options) {
        return transport.call(Operations.GET_MESSAGE_TEXT, Transport.call("messages.text").path(messageId).options(options));
    }

    /**
     * Moves a scheduled message to another delivery time. Uses the team token if configured, otherwise the sending token.
     *
     * @param messageId the message id
     * @param body the request body
     * @return the response
     */
    public ScheduledMessage reschedule(String messageId, RescheduleMessageRequest body) {
        return reschedule(messageId, body, null);
    }

    /**
     * Moves a scheduled message to another delivery time. Uses the team token if configured, otherwise the sending token.
     *
     * @param messageId the message id
     * @param body the request body
     * @param options per-call options, or null
     * @return the response
     */
    public ScheduledMessage reschedule(String messageId, RescheduleMessageRequest body, RequestOptions options) {
        return transport.call(Operations.RESCHEDULE_MESSAGE, Transport.call("messages.reschedule").path(messageId).body(body).options(options));
    }

    /**
     * Cancels a scheduled message. Uses the team token if configured, otherwise the sending token.
     *
     * @param messageId the message id
     * @return the response
     */
    public ScheduledMessage cancel(String messageId) {
        return cancel(messageId, null);
    }

    /**
     * Cancels a scheduled message. Uses the team token if configured, otherwise the sending token.
     *
     * @param messageId the message id
     * @param options per-call options, or null
     * @return the response
     */
    public ScheduledMessage cancel(String messageId, RequestOptions options) {
        return transport.call(Operations.CANCEL_SCHEDULED_MESSAGE, Transport.call("messages.cancel").path(messageId).options(options));
    }

    /**
     * Releases one quarantined inbound message for webhook delivery. Accepts an idempotency key.
     *
     * @param messageId the message id
     * @return the response
     */
    public ProcessInboundMessageResponse process(String messageId) {
        return process(messageId, null);
    }

    /**
     * Releases one quarantined inbound message for webhook delivery. Accepts an idempotency key.
     *
     * @param messageId the message id
     * @param options the idempotency key and timeout of this call, or null
     * @return the response
     */
    public ProcessInboundMessageResponse process(String messageId, SendOptions options) {
        return transport.call(Operations.PROCESS_INBOUND_MESSAGE, Transport.call("messages.process").path(messageId).options(options));
    }

    @Override
    public String toString() {
        return "Messages{}";
    }
}
