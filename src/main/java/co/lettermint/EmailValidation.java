package co.lettermint;

import co.lettermint.exceptions.LettermintValidationException;
import co.lettermint.types.MessageTagInput;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The checks the SDK makes before a request, shared by {@code emails().send()},
 * {@code emails().sendBatch()}, {@link EmailMessage} and {@link EmailBuilder}. Pure: it only throws.
 */
final class EmailValidation {
    private static final Pattern TAG_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");
    private static final Pattern TAG_VALUE = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");
    private static final int MAX_TAGS = 20;

    private EmailValidation() {
    }

    static void validate(EmailMessage message, String prefix) {
        String field = prefix.isEmpty() ? "tags" : prefix + ".tags";
        String legacyTag = message.tag();
        validateTags(message.tags(), legacyTag != null && !legacyTag.isEmpty(), field);
    }

    static void validateTags(List<MessageTagInput> tags, boolean hasLegacyTag, String field) {
        if (tags == null) {
            return;
        }
        int maximum = hasLegacyTag ? MAX_TAGS - 1 : MAX_TAGS;
        if (tags.size() > maximum) {
            throw new LettermintValidationException(hasLegacyTag
                    ? "A legacy tag and no more than " + maximum + " message tags are permitted."
                    : "No more than " + maximum + " message tags are permitted.", field);
        }
        Set<String> names = new HashSet<>();
        for (MessageTagInput tag : tags) {
            if (tag == null || tag.name() == null || tag.value() == null) {
                throw new LettermintValidationException("Message tags must have a name and a value.", field);
            }
            if (!TAG_NAME.matcher(tag.name()).matches()) {
                throw new LettermintValidationException("Message tag names must match ^[A-Za-z0-9_-]{1,32}$.", field);
            }
            if (tag.name().toLowerCase(Locale.ROOT).startsWith("__lettermint")) {
                throw new LettermintValidationException("Message tag names must not start with __lettermint.", field);
            }
            if (!TAG_VALUE.matcher(tag.value()).matches()) {
                throw new LettermintValidationException("Message tag values must match ^[A-Za-z0-9_-]{1,64}$.", field);
            }
            if (!names.add(tag.name())) {
                throw new LettermintValidationException("Message tag names must be unique (case-sensitive).", field);
            }
        }
    }
}
