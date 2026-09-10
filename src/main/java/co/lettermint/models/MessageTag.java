package co.lettermint.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;
import java.util.regex.Pattern;

/** A reusable exact-match message tag. */
public final class MessageTag {
    private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");
    private static final Pattern VALUE = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    @JsonProperty("name")
    private final String name;

    @JsonProperty("value")
    private final String value;

    public MessageTag(String name, String value) {
        if (name == null || !NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Message tag names must match ^[A-Za-z0-9_-]{1,32}$");
        }
        if (name.toLowerCase(Locale.ROOT).startsWith("__lettermint")) {
            throw new IllegalArgumentException("Message tag names must not start with __lettermint");
        }
        if (value == null || !VALUE.matcher(value).matches()) {
            throw new IllegalArgumentException("Message tag values must match ^[A-Za-z0-9_-]{1,64}$");
        }
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public String getValue() { return value; }
}
