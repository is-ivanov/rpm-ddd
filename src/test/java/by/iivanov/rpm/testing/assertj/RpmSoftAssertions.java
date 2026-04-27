package by.iivanov.rpm.testing.assertj;

import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.BDDSoftAssertionsProvider;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert;
import org.springframework.data.domain.AbstractAggregateRoot;

/// Soft assertions entry point for testing.
/// ```
/// SomeTest {
///     RpmSoftAssertions softly;
///          test() {
///         softly.then(user).hasEventsSize(1);
///         softly.then(user).containsEventType(UserRegisteredEvent.class);
///         softly.assertAll();
///     }
/// }
/// ```
public class RpmSoftAssertions extends SoftAssertions implements BDDSoftAssertionsProvider {

    public static ConstraintViolationException catchConstraintViolation(
            ThrowableAssert.ThrowingCallable throwingCallable) {
        return BDDAssertions.catchThrowableOfType(ConstraintViolationException.class, throwingCallable);
    }

    public AggregateRootAssert then(AbstractAggregateRoot<?> aggregate) {
        return proxy(AggregateRootAssert.class, AbstractAggregateRoot.class, aggregate);
    }

    //    @Override
    //    public <T extends Throwable> ThrowableAssert<T> then(T actual) {
    //        return switch (actual) {
    //            case ConstraintViolationException cve ->
    //                    proxy(ConstraintViolationExceptionAssert.class, ConstraintViolationException.class, cve);
    //            default -> BDDSoftAssertionsProvider.super.then(actual);
    //        };
    //    }

    public ConstraintViolationExceptionAssert then(ConstraintViolationException exception) {
        return proxy(ConstraintViolationExceptionAssert.class, ConstraintViolationException.class, exception);
    }
}
