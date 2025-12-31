package by.iivanov.rpm.shared.web;

import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(EmptyStringToNullTestController.class)
@RequiredArgsConstructor
class StringTrimmerControllerAdviceTest {

    private static final String PATH = "/test";

    private final MockMvcTester mockMvc;
    private final ObjectMapper objectMapper;

    @Nested
    @DisplayName("test GET '/test' endpoint")
    class GetRequestTest {

        @ParameterizedTest
        @ValueSource(strings = {" ", "\t", "\n", "\r", "\f"})
        @DisplayName("WHEN GET request with parameter name is empty string EXPECT response with null field")
        void getRequestWithParameterNameIsEmptyString_responseWithNullField(String name) {
            // GIVEN:
            String expectedResponseBody = """
                {
                   "name": null
                }
                """;
            // WHEN:
            var response = sendGetRequest(name);
            // THEN:
            then(response).hasStatusOk().bodyJson().isEqualTo(expectedResponseBody);
        }

        @ParameterizedTest(name = "[{index}] case: {0}")
        @CsvSource(textBlock = """
            'trailing spaces    ',                  'trailing spaces'
            '    leading spaces',                   'leading spaces'
            '    leading and trailing spaces    ',  'leading and trailing spaces'
            """)
        @DisplayName("WHEN GET request with parameter name with spaces EXPECT parameter is trimmed")
        void when_getRequestWithParameterNameWithSpaces_expect_parameterIsTrimmed(
                String name, String expectedResponseName) {
            // GIVEN:
            String expectedResponseBody = """
                {
                   "name": "%s"
                }
                """.formatted(expectedResponseName);
            // WHEN:
            var response = sendGetRequest(name);
            // THEN:
            then(response).hasStatusOk().bodyJson().isEqualTo(expectedResponseBody);
        }

        private MvcTestResult sendGetRequest(String name) {
            return mockMvc.get().uri(PATH).queryParam("name", name).exchange();
        }
    }

    @Nested
    @DisplayName("test POST '/test' endpoint")
    class PostRequestTest {

        @ParameterizedTest
        @ValueSource(strings = {" ", "\t", "\n", "\r", "\f"})
        @DisplayName("WHEN POST request with empty string in body EXPECT response with null field")
        void getRequestWithParameterNameIsEmptyString_responseWithNullField(String name) {
            // GIVEN:
            String expectedResponseBody = """
                {
                   "name": null
                }
                """;
            // WHEN:
            var response = sendPostRequest(name);
            // THEN:
            then(response).hasStatusOk().bodyJson().isEqualTo(expectedResponseBody);
        }

        @ParameterizedTest(name = "[{index}] case: {0}")
        @CsvSource(textBlock = """
            'trailing spaces    ',                  'trailing spaces'
            '    leading spaces',                   'leading spaces'
            '    leading and trailing spaces    ',  'leading and trailing spaces'
            """)
        @DisplayName("WHEN GET request with parameter name with spaces EXPECT parameter is trimmed")
        void when_getRequestWithParameterNameWithSpaces_expect_parameterIsTrimmed(
                String name, String expectedResponseName) {
            // GIVEN:
            String expectedResponseBody = """
                {
                   "name": "%s"
                }
                """.formatted(expectedResponseName);
            // WHEN:
            var response = sendPostRequest(name);
            // THEN:
            then(response).hasStatusOk().bodyJson().isEqualTo(expectedResponseBody);
        }

        private MvcTestResult sendPostRequest(String name) {
            var payload = Map.of("name", name);
            String requestBody = objectMapper.writeValueAsString(payload);
            return mockMvc.post()
                    .uri(PATH)
                    .contentType(APPLICATION_JSON)
                    .content(requestBody)
                    .exchange();
        }
    }
}
