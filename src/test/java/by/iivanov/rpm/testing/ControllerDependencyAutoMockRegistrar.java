package by.iivanov.rpm.testing;

import java.lang.reflect.Constructor;
import org.jspecify.annotations.Nullable;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * Automatically registers Mockito mock beans for all missing controller constructor dependencies.
 * Used by {@link WebTest} to eliminate manual mock declarations.
 *
 * <p>Uses {@code postProcessBeanFactory} (not {@code postProcessBeanDefinitionRegistry}) because
 * {@code @WebMvcTest} registers controller bean definitions between those two phases.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ControllerDependencyAutoMockRegistrar implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // Intentionally empty: controller bean definitions are not yet registered at this phase.
        // @WebMvcTest registers them between postProcessBeanDefinitionRegistry() and postProcessBeanFactory(),
        // so all scanning and mock registration happens in postProcessBeanFactory() instead.
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition bd = registry.getBeanDefinition(beanName);
            String className = bd.getBeanClassName();
            if (className == null) {
                continue;
            }

            Class<?> clazz = resolveClass(className);
            if (clazz == null || !isController(clazz)) {
                continue;
            }

            registerMissingMocks(registry, beanFactory, clazz);
        }
    }

    private void registerMissingMocks(
            BeanDefinitionRegistry registry, ConfigurableListableBeanFactory beanFactory, Class<?> controllerClass) {
        Constructor<?> constructor = resolveConstructor(controllerClass);
        if (constructor == null) {
            return;
        }

        for (Class<?> paramType : constructor.getParameterTypes()) {
            if (beanFactory.getBeanNamesForType(paramType).length == 0
                    && !registry.containsBeanDefinition(paramType.getName())) {
                registerMockBean(registry, paramType);
            }
        }
    }

    private <T> void registerMockBean(BeanDefinitionRegistry registry, Class<T> type) {
        registry.registerBeanDefinition(
                type.getName(),
                BeanDefinitionBuilder.genericBeanDefinition(type, () -> Mockito.mock(type))
                        .getBeanDefinition());
    }

    private @Nullable Constructor<?> resolveConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 0) {
            return null;
        }
        if (constructors.length == 1) {
            return constructors[0];
        }
        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() > 0) {
                return c;
            }
        }
        return constructors[0];
    }

    private @Nullable Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException _) {
            return null;
        }
    }

    private boolean isController(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class);
    }
}
