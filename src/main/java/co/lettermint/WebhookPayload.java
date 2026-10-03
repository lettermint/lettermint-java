package co.lettermint;

import co.lettermint.types.WebhookEvent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A verified webhook delivery. Unknown fields are kept in {@link #fields()}.
 *
 * <pre>{@code
 * WebhookPayload event = webhook.verify(rawBody, headers);
 * if (WebhookEvent.MESSAGE_DELIVERED.equals(event.event())) {
 *     String messageId = (String) event.data().get("message_id");
 * }
 * }</pre>
 */
public final class WebhookPayload {
    private final Map<String, Object> fields;

    WebhookPayload(Map<String, Object> fields) {
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
    }

    /**
     * @return the delivery id, or null
     */
    public String id() {
        return fields.get("id") instanceof String value ? value : null;
    }

    /**
     * @return the event, for example {@link WebhookEvent#MESSAGE_DELIVERED}; unknown events keep their
     *     raw name. Null when the payload has no string {@code event}.
     */
    public WebhookEvent event() {
        return fields.get("event") instanceof String value ? WebhookEvent.of(value) : null;
    }

    /**
     * @return when the event occurred (ISO 8601), from {@code timestamp} or else {@code created_at}; or null
     */
    public String timestamp() {
        if (fields.get("timestamp") instanceof String value) {
            return value;
        }
        return fields.get("created_at") instanceof String value ? value : null;
    }

    /**
     * @return the event data (maps, lists and scalars as decoded by Jackson), or an empty map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> data() {
        Object data = fields.get("data");
        return data instanceof Map<?, ?> map ? Collections.unmodifiableMap((Map<String, Object>) map) : Map.of();
    }

    /**
     * Converts the event data into your own type with Jackson, for example a record.
     *
     * @param type the target type
     * @param <T> the target type
     * @return the converted data
     */
    public <T> T data(Class<T> type) {
        return Transport.MAPPER.convertValue(data(), type);
    }

    /**
     * @return every top-level field of the payload
     */
    public Map<String, Object> fields() {
        return fields;
    }

    @Override
    public String toString() {
        return "WebhookPayload{id=" + id() + ", event=" + event() + ", timestamp=" + timestamp() + "}";
    }
}
