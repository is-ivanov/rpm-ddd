package by.iivanov.rpm.iam.user.application;

import static org.assertj.core.api.BDDAssertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import by.iivanov.rpm.iam.user.domain.Login;
import by.iivanov.rpm.iam.user.domain.PersonName;
import by.iivanov.rpm.shared.domain.errors.DomainValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RegisterUserCommandTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {

        @Test
        @DisplayName("WHEN valid fields EXPECT command created")
        void when_validFields_expect_commandCreated() {
            // GIVEN:
            PersonName userName = new PersonName("Ivan", "Ivanovich", "Ivanov");
            Login login = new Login("ivanov");
            EmailAddress email = new EmailAddress("ivan@example.com");
            // WHEN:
            var command = new RegisterUserCommand(userName, login, email);
            // THEN:
            then(command.userName()).isEqualTo(userName);
            then(command.login()).isEqualTo(login);
            then(command.email()).isEqualTo(email);
        }

        @Nested
        @DisplayName("null checks")
        class NullCheckTest {

            @Test
            @DisplayName("WHEN userName is null EXPECT DomainValidationException")
            void when_userNameNull_expect_exception() {
                // GIVEN:
                // WHEN:
                @SuppressWarnings("NullAway")
                var exception = catchException(
                        () -> new RegisterUserCommand(null, new Login("ivanov"), new EmailAddress("ivan@example.com")));
                // THEN:
                then(exception).isInstanceOf(DomainValidationException.class).hasMessage("userName must not be null");
            }

            @Test
            @DisplayName("WHEN login is null EXPECT DomainValidationException")
            void when_loginNull_expect_exception() {
                // GIVEN:
                // WHEN:
                @SuppressWarnings("NullAway")
                var exception = catchException(() -> new RegisterUserCommand(
                        new PersonName("Ivan", null, "Ivanov"), null, new EmailAddress("ivan@example.com")));
                // THEN:
                then(exception).isInstanceOf(DomainValidationException.class).hasMessage("login must not be null");
            }

            @Test
            @DisplayName("WHEN email is null EXPECT DomainValidationException")
            void when_emailNull_expect_exception() {
                // GIVEN:
                // WHEN:
                @SuppressWarnings("NullAway")
                var exception = catchException(() ->
                        new RegisterUserCommand(new PersonName("Ivan", null, "Ivanov"), new Login("ivanov"), null));
                // THEN:
                then(exception).isInstanceOf(DomainValidationException.class).hasMessage("email must not be null");
            }
        }
    }
}
