package by.iivanov.rpm.shared.infrastructure.events;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEvent;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;

/**
 * In-memory fake of {@link IncompleteEventPublications} for pure-unit testing of the resubmit
 * selection predicate. Candidate publications are registered with an age relative to the test clock;
 * when the job applies its filter via {@link #resubmitIncompletePublications(Predicate)}, the labels of
 * the selected publications are captured for assertion.
 */
@NullMarked
class RecordingIncompletePublications implements IncompleteEventPublications {

    private static final String NOT_EXERCISED_BY_JOB = "not exercised by ResubmitIncompletePublicationsJob";

    private final List<LabelledPublication> candidates = new ArrayList<>();
    private final List<String> resubmittedLabels = new ArrayList<>();

    void register(String label, Instant publicationDate) {
        candidates.add(new LabelledPublication(label, publicationDate));
    }

    List<String> resubmittedLabels() {
        return List.copyOf(resubmittedLabels);
    }

    @Override
    public void resubmitIncompletePublications(Predicate<EventPublication> filter) {
        resubmittedLabels.clear();
        candidates.stream().filter(filter).map(LabelledPublication::label).forEach(resubmittedLabels::add);
    }

    @Override
    public void resubmitIncompletePublications(ResubmissionOptions options) {
        throw new UnsupportedOperationException(NOT_EXERCISED_BY_JOB);
    }

    @Override
    public void resubmitIncompletePublicationsOlderThan(Duration duration) {
        throw new UnsupportedOperationException(NOT_EXERCISED_BY_JOB);
    }

    private record LabelledPublication(String label, Instant publicationDate) implements EventPublication {

        @Override
        public UUID getIdentifier() {
            return UUID.nameUUIDFromBytes(label.getBytes(UTF_8));
        }

        @Override
        public Object getEvent() {
            return label;
        }

        @Override
        public Instant getPublicationDate() {
            return publicationDate;
        }

        @Override
        public Optional<Instant> getCompletionDate() {
            return Optional.empty();
        }

        @Override
        public Status getStatus() {
            return Status.PUBLISHED;
        }

        @Override
        public @Nullable Instant getLastResubmissionDate() {
            return null;
        }

        @Override
        public int getCompletionAttempts() {
            return 0;
        }

        @Override
        public ApplicationEvent getApplicationEvent() {
            throw new UnsupportedOperationException("not exercised by the resubmit selection predicate");
        }
    }
}
