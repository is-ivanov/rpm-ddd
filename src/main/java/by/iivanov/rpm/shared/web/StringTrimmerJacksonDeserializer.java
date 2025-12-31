package by.iivanov.rpm.shared.web;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@JacksonComponent
class StringTrimmerJacksonDeserializer extends ValueDeserializer<String> {

    @Override
    public @Nullable String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        return StringUtils.trimToNull(p.getValueAsString());
    }
}
