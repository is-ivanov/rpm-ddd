package by.iivanov.rpm.testing.assertj;

import java.util.List;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ObjectAssert;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.test.util.ReflectionTestUtils;

/// Custom AssertJ assertions for [AbstractAggregateRoot].
/// Allows fluent testing of domain events published by aggregates.
/// ```
/// then(user).hasEventsSize(1)
///           .containsEventType(UserRegisteredEvent.class)
///           .firstEvent(UserRegisteredEvent.class)
///           .satisfies(event -> {
///               then(event.userId()).isEqualTo(expectedId);
///           });
/// ```
public class AggregateRootAssert extends AbstractAssert<AggregateRootAssert, AbstractAggregateRoot<?>> {

    protected List<Object> events;

    protected AggregateRootAssert(AbstractAggregateRoot<?> actual) {
        super(actual, AggregateRootAssert.class);
        events = getEvents();
    }

    /**
     * Entry point: creates assertions for the given aggregate.
     */
    public static AggregateRootAssert assertThat(AbstractAggregateRoot<?> aggregate) {
        return new AggregateRootAssert(aggregate);
    }

    @SuppressWarnings("unchecked")
    private List<Object> getEvents() {
        var actualEvents = ReflectionTestUtils.invokeMethod(actual, "domainEvents");
        if (actualEvents == null) {
            return List.of();
        }
        return (List<Object>) actualEvents;
    }

    /**
     * Verifies that the aggregate has exactly the given number of domain events.
     *
     * @param expected expected event count
     */
    public AggregateRootAssert hasEventsSize(int expected) {
        isNotNull();
        if (events.size() != expected) {
            failWithMessage("Expected <%d> domain events but was <%d>", expected, events.size());
        }
        return this;
    }

    /**
     * Verifies that at least one domain event is of the given type.
     *
     * @param eventType the expected event type
     */
    public AggregateRootAssert containsEventType(Class<?> eventType) {
        isNotNull();
        boolean found = events.stream().anyMatch(eventType::isInstance);
        if (!found) {
            failWithMessage(
                    "Expected at least one event of type <%s> but found: %s", eventType.getSimpleName(), eventTypes());
        }
        return this;
    }

    /**
     * Returns a typed assertion for the first domain event.
     *
     * @param eventType the expected event type
     */
    public <E> ObjectAssert<E> firstEvent(Class<E> eventType) {
        isNotNull();
        if (events.isEmpty()) {
            failWithMessage("Expected at least one domain event but none were published");
        }
        var first = events.getFirst();
        if (!eventType.isInstance(first)) {
            failWithMessage(
                    "Expected first event to be of type <%s> but was <%s>",
                    eventType.getSimpleName(), first.getClass().getSimpleName());
        }
        @SuppressWarnings("unchecked")
        var typedEvent = (E) first;
        return new ObjectAssert<>(typedEvent);
    }

    /**
     * Verifies that the first domain event satisfies the given consumer.
     *
     * @param consumer assertions to apply to the first event
     */
    public AggregateRootAssert firstEventSatisfy(Consumer<Object> consumer) {
        isNotNull();
        if (events.isEmpty()) {
            failWithMessage("Expected at least one domain event but none were published");
        }
        consumer.accept(events.getFirst());
        return this;
    }

    private String eventTypes() {
        return events.stream().map(e -> e.getClass().getSimpleName()).toList().toString();
    }
}
