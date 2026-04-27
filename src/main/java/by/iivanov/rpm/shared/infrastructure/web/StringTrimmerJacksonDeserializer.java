package by.iivanov.rpm.shared.infrastructure.web;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * A custom Jackson deserializer that trims leading and trailing whitespace from string values
 * and converts empty or whitespace-only strings into null.
 *
 * <p>This deserializer is useful for ensuring consistent handling of string inputs during JSON
 * deserialization, especially in cases where empty or blank strings should be treated as null.
 *
 * <p>This deserializer is annotated with {@link JacksonComponent}, enabling integration with the
 * Spring Boot autoconfiguration for Jackson. When registered, it will automatically apply to
 * all fields or parameters of type {@code String} encountered during deserialization.
 */
@JacksonComponent
class StringTrimmerJacksonDeserializer extends ValueDeserializer<String> {

    @Override
    public @Nullable String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        return StringUtils.trimToNull(p.getValueAsString());
    }
}
