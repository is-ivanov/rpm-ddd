package by.iivanov.rpm.testing.api;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record FieldError(
        String code,
        String property,
        String message,
        @Nullable Object rejectedValue,
        String path) {

    public static FieldErrorPropertyBuilder size() {
        return builder().code("INVALID_SIZE");
    }

    public static FieldErrorPropertyBuilder notBlank() {
        return builder().code("REQUIRED_NOT_BLANK");
    }

    private FieldError(FieldErrorBuilder builder) {
        this(
                Objects.requireNonNull(builder.code),
                Objects.requireNonNull(builder.property),
                Objects.requireNonNull(builder.message),
                builder.rejectedValue,
                Objects.requireNonNull(builder.path));
    }

    public static FieldErrorCodeBuilder builder() {
        return new FieldErrorBuilder();
    }

    public sealed interface FieldErrorCodeBuilder permits FieldErrorBuilder {
        FieldErrorPropertyBuilder code(String code);
    }

    public sealed interface FieldErrorPropertyBuilder permits FieldErrorBuilder {
        FieldErrorMessageBuilder property(String property);
    }

    public sealed interface FieldErrorMessageBuilder permits FieldErrorBuilder {
        FieldErrorRejectedValueBuilder message(String message);
    }

    public sealed interface FieldErrorRejectedValueBuilder permits FieldErrorBuilder {
        FieldErrorPathBuilder rejectedValue(@Nullable Object rejectedValue);
    }

    public sealed interface FieldErrorPathBuilder permits FieldErrorBuilder {
        FieldError path(String path);
    }

    private static final class FieldErrorBuilder
            implements FieldErrorCodeBuilder,
                    FieldErrorPropertyBuilder,
                    FieldErrorMessageBuilder,
                    FieldErrorRejectedValueBuilder,
                    FieldErrorPathBuilder {

        private @Nullable String code;
        private @Nullable String property;
        private @Nullable String message;
        private @Nullable Object rejectedValue;
        private @Nullable String path;

        @Override
        public FieldErrorPropertyBuilder code(String code) {
            this.code = code;
            return this;
        }

        @Override
        public FieldErrorMessageBuilder property(String property) {
            this.property = property;
            return this;
        }

        @Override
        public FieldErrorRejectedValueBuilder message(String message) {
            this.message = message;
            return this;
        }

        @Override
        public FieldErrorPathBuilder rejectedValue(@Nullable Object rejectedValue) {
            this.rejectedValue = rejectedValue;
            return this;
        }

        @Override
        public FieldError path(String path) {
            this.path = path;
            return new FieldError(this);
        }
    }
}
