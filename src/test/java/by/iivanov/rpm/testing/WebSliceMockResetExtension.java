package by.iivanov.rpm.testing;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Resets every auto-registered controller-dependency mock before each web-slice test, so stubbing
 * and recorded invocations from one test never leak into another through the shared cached
 * {@code @WebMvcTest} context. Pairs with {@link ControllerDependencyAutoMockRegistrar}, whose mocks
 * are plain {@code Mockito.mock(...)} singletons that Spring's reset listener does not track.
 */
public class WebSliceMockResetExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        for (Object bean : applicationContext.getBeansOfType(Object.class).values()) {
            if (Mockito.mockingDetails(bean).isMock()) {
                Mockito.reset(bean);
            }
        }
    }
}
