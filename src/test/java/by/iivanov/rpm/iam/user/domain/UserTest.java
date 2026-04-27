package by.iivanov.rpm.iam.user.domain;

import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.testing.assertj.RpmSoftAssertions;
import java.util.UUID;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class UserTest {

    @InjectSoftAssertions
    private RpmSoftAssertions softly;

    @Nested
    @DisplayName("register()")
    class RegisterTest {

        private final UserId id = new UserId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        private final PersonName personName = new PersonName("Ivan", "Ivanovich", "Ivanov");
        private final EmailAddress email = new EmailAddress("ivan@example.com");
        private final Login login = new Login("ivanov");
        private final Password password = new Password("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        private final String plainPassword = "Temp@Pass1!";
        private final UserId createdBy = new UserId(UUID.fromString("123e4567-e89b-12d3-a456-426614174023"));

        @Test
        @DisplayName("WHEN register EXPECT status is PENDING")
        void when_register_expect_statusPending() {
            // WHEN:
            var user = User.register(id, personName, email, login, password, plainPassword, createdBy);
            // THEN:
            then(user.getStatus()).isEqualTo(UserStatus.PENDING);
        }

        @Test
        @DisplayName("WHEN register EXPECT createdBy is set")
        void when_register_expect_createdByIsSet() {
            // WHEN:
            var user = User.register(id, personName, email, login, password, plainPassword, createdBy);
            // THEN:
            then(user.getCreatedBy().getId()).isEqualTo(createdBy);
        }

        @Test
        @DisplayName("WHEN register EXPECT version is 0")
        void when_register_expect_versionIsZero() {
            // WHEN:
            var user = User.register(id, personName, email, login, password, plainPassword, createdBy);
            // THEN:
            then(user.getVersion()).isEqualTo(0);
        }

        @Test
        @DisplayName("WHEN register EXPECT UserRegisteredEvent is published")
        void when_register_expect_eventPublished() {
            // WHEN:
            var user = User.register(id, personName, email, login, password, plainPassword, createdBy);
            // THEN:
            softly.then(user)
                    .hasEventsSize(1)
                    .containsEventType(UserRegisteredEvent.class)
                    .firstEvent(UserRegisteredEvent.class)
                    .satisfies(event -> {
                        then(event.userId()).isEqualTo(id);
                        then(event.login()).isEqualTo(login);
                        then(event.email()).isEqualTo(email);
                        then(event.temporaryPasswordPlain()).isEqualTo(plainPassword);
                    });
        }
    }
}
