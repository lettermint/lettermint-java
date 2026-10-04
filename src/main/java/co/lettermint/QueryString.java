package co.lettermint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serializes query parameters to the API's bracket syntax, exactly like the Node SDK's
 * {@code src/query.ts}: nested objects use {@code key[name]}, arrays of scalars are comma-joined,
 * arrays of objects are indexed ({@code key[0][name]}), booleans are {@code 1}/{@code 0}, and null
 * values are left out. Keys and values are form-encoded like {@code URLSearchParams}.
 */
final class QueryString {
    private QueryString() {
    }

    static String serialize(Map<String, Object> parameters, ObjectMapper mapper) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        List<String[]> pairs = new ArrayList<>();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                append(pairs, entry.getKey(), mapper.valueToTree(entry.getValue()));
            }
        }
        StringBuilder query = new StringBuilder();
        for (String[] pair : pairs) {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(encode(pair[0])).append('=').append(encode(pair[1]));
        }
        return query.toString();
    }

    private static void append(List<String[]> pairs, String key, JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return;
        }
        if (value.isArray()) {
            boolean scalars = true;
            for (JsonNode item : value) {
                scalars &= item.isValueNode() || item.isNull();
            }
            if (scalars) {
                List<String> items = new ArrayList<>();
                for (JsonNode item : value) {
                    if (!item.isNull()) {
                        items.add(scalar(item));
                    }
                }
                if (!items.isEmpty()) {
                    pairs.add(new String[] {key, String.join(",", items)});
                }
                return;
            }
            for (int index = 0; index < value.size(); index++) {
                append(pairs, key + "[" + index + "]", value.get(index));
            }
            return;
        }
        if (value.isValueNode()) {
            pairs.add(new String[] {key, scalar(value)});
            return;
        }
        for (Map.Entry<String, JsonNode> field : value.properties()) {
            append(pairs, key + "[" + field.getKey() + "]", field.getValue());
        }
    }

    private static String scalar(JsonNode value) {
        if (value.isBoolean()) {
            return value.booleanValue() ? "1" : "0";
        }
        return value.asText();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
