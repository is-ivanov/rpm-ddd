package by.iivanov.rpm.testing.api;

import static net.javacrumbs.jsonunit.spring.RestTestClientJsonMatcher.json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import net.javacrumbs.jsonunit.core.Option;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

public class AssertionResponse {

    private final RestTestClient.ResponseSpec responseSpec;

    public AssertionResponse(RestTestClient.ResponseSpec responseSpec) {
        this.responseSpec = responseSpec;
    }

    public AssertionResponse assertStatus(HttpStatusCode status) {
        responseSpec.expectStatus().isEqualTo(status);
        return this;
    }

    public AssertionResponse assertOk() {
        return assertStatus(HttpStatus.OK);
    }

    public AssertionResponse assertOk(String expectedJson) {
        return assertOk().assertBodyMatches(expectedJson);
    }

    public AssertionResponse assertOk(String expectedJson, Option... options) {
        return assertOk().assertBodyMatches(expectedJson, options);
    }

    public AssertionResponse assertCreated() {
        return assertStatus(HttpStatus.CREATED);
    }

    public AssertionResponse assertCreated(String expectedJson) {
        return assertCreated().assertBodyMatches(expectedJson);
    }

    public AssertionResponse assertCreated(String expectedJson, Option... options) {
        return assertCreated().assertBodyMatches(expectedJson, options);
    }

    public AssertionResponse assertBindingError(String expectedBody) {
        assertUnprocessableProblemJson();
        return assertBodyMatches(expectedBody, Option.IGNORING_ARRAY_ORDER);
    }

    /**
     * Asserts that the response indicates a binding error, validating error details
     * and structure against the provided list of {@code FieldError} instances.
     */
    public void assertBindingError(FieldError... fieldErrors) {
        assertUnprocessableProblemJson();
        assertBodyMatches("""
                {
                  "status": 422,
                  "title": "Unprocessable Content",
                  "type": "https://www.rpm-ddd.my/problem/validation-failed"
                }
                """, Option.IGNORING_EXTRA_FIELDS);
        assertFieldErrors(fieldErrors);
    }

    private void assertUnprocessableProblemJson() {
        assertStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        responseSpec.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    private void assertFieldErrors(FieldError... fieldErrors) {
        responseSpec
                .expectBody()
                .jsonPath("$.detail")
                .value(
                        String.class,
                        d -> Assertions.assertThat(d).contains("Error count: %d".formatted(fieldErrors.length)))
                .consumeWith(json().node("fieldErrors")
                        .when(Option.IGNORING_ARRAY_ORDER)
                        .isEqualTo(fieldErrors));
    }

    public AssertionResponse assertBodyMatches(String expected) {
        return assertBodyMatches(expected, new Option[0]);
    }

    /**
     * Asserts that the body of the response matches the expected JSON structure.
     *
     * @param expected the expected JSON structure, either as a raw JSON string or a file path
     * @param options  optional settings to customize the JSON comparison behavior
     */
    public AssertionResponse assertBodyMatches(String expected, Option... options) {
        String resolved = resolveExpected(expected);
        var matcher = json();
        if (options.length > 0) {
            matcher = matcher.when(options[0], tail(options));
        }
        responseSpec.expectBody().consumeWith(matcher.isEqualTo(resolved));
        return this;
    }

    private static Option[] tail(Option[] arr) {
        if (arr.length <= 1) {
            return new Option[0];
        }
        Option[] rest = new Option[arr.length - 1];
        System.arraycopy(arr, 1, rest, 0, rest.length);
        return rest;
    }

    public <T> T extractBodyAs(Class<T> type) {
        T responseBody = responseSpec.returnResult(type).getResponseBody();
        Assertions.assertThat(responseBody).as("Response body must be not null").isNotNull();
        return Objects.requireNonNull(responseBody, "that assert only for NullAway");
    }

    public String extractBodyAsString() {
        return extractBodyAs(String.class);
    }

    public @Nullable String extractHeader(String headerName) {
        return responseSpec.returnResult().getResponseHeaders().getFirst(headerName);
    }

    public @Nullable String extractLocation() {
        return extractHeader(HttpHeaders.LOCATION);
    }

    /**
     * Extracts the unique identifier from a location header returned in a response,
     * after validating that the location header exists and its value starts with
     * the specified path prefix.
     *
     * @param pathPrefix the expected prefix of the location header value
     * @return the extracted unique identifier with the path prefix removed
     */
    public String extractCreatedId(String pathPrefix) {
        String location = extractLocation();
        Assertions.assertThat(location)
                .as("Location header must be present")
                .isNotNull()
                .as("Location header must starts with <%s>", pathPrefix)
                .startsWith(pathPrefix);
        return Objects.requireNonNull(location, "that assert only for NullAway").substring(pathPrefix.length());
    }

    /**
     * Asserts that the location ID extracted from the response matches the expected format.
     *
     * @param pathPrefix the expected prefix of the location header value
     * @param parser     a function to parse the extracted location ID into a specific type
     * @param validator  a predicate to validate the parsed location ID
     */
    public <T> AssertionResponse assertLocationIdMatches(
            String pathPrefix, Function<String, T> parser, Predicate<T> validator) {
        String rawId = extractCreatedId(pathPrefix);
        Assertions.assertThatCode(() -> parser.apply(rawId))
                .as("Location id <%s> must be parseable", rawId)
                .doesNotThrowAnyException();
        T id = parser.apply(rawId);
        Assertions.assertThat(id)
                .as("Location id <%s> must pass validation", rawId)
                .matches(validator, "is valid ID");
        return this;
    }

    public RestTestClient.ResponseSpec unwrap() {
        return responseSpec;
    }

    private String resolveExpected(String expected) {
        if (expected.startsWith("{") || expected.startsWith("[")) {
            return expected;
        }
        try {
            return Files.readString(Path.of("src/test/resources/" + expected));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
