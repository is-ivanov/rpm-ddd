package by.iivanov.rpm.testing;

import by.iivanov.rpm.testing.api.WebApi;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Meta-annotation for web slice tests.
 * Loads all controllers with auto-mocked dependencies via
 * {@link ControllerDependencyAutoMockRegistrar}.
 * Discovers {@link WebApi} test helper beans and every {@link ApiExceptionHandler}
 * implementation across all subdomain packages, so error-mapping handlers are exercised
 * in the same shared web-slice context (no per-test {@code @Import}, no context fork).
 */
@WebMvcTest
@AutoConfigureRestTestClient
@ActiveProfiles("test")
@Import(ControllerDependencyAutoMockRegistrar.class)
@ComponentScan(
        basePackages = "by.iivanov.rpm",
        includeFilters = {
            @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = WebApi.class),
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApiExceptionHandler.class)
        },
        useDefaultFilters = false)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WebTest {}
