package co.lettermint.models.api;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;

@JsonDeserialize(using = OptionalNullable.Deserializer.class)
public final class OptionalNullable<T> {
    private final T value;

    private OptionalNullable(T value) {
        this.value = value;
    }

    public static <T> OptionalNullable<T> of(T value) {
        return new OptionalNullable<>(value);
    }

    public static <T> OptionalNullable<T> nullValue() {
        return new OptionalNullable<>(null);
    }

    @JsonValue
    public T getValue() {
        return value;
    }

    public static final class Deserializer extends JsonDeserializer<OptionalNullable<?>> implements ContextualDeserializer {
        private final JavaType valueType;

        public Deserializer() { this(null); }

        private Deserializer(JavaType valueType) { this.valueType = valueType; }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            JavaType type = property == null ? context.getContextualType() : property.getType();
            return new Deserializer(type.containedType(0));
        }

        @Override
        public OptionalNullable<?> getNullValue(DeserializationContext context) {
            return OptionalNullable.nullValue();
        }

        @Override
        public OptionalNullable<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return OptionalNullable.of(context.readValue(parser, valueType));
        }
    }
}
