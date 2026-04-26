package by.iivanov.rpm.shared.infrastructure;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

/** Utility class for generating UUIDs. */
public final class UuidGenerator {

    /** Generates and returns a time-ordered UUID based on the Unix Epoch (UUIDv7). */
    public static UUID generateEntityId() {
        return UuidCreator.getTimeOrderedEpochFast();
    }

    private UuidGenerator() {}
}
