package by.iivanov.rpm.testing;

import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;

@UtilityClass
public class TestContextValidator {

    /**
     * Print all beans.
     */
    public static List<String> getAllBeanDefinitions(ApplicationContext context) {
        // Print all bean names in the application context
        System.out.println("\n=== BEANS IN APPLICATION CONTEXT ===");
        String[] beanNames = context.getBeanDefinitionNames();
        System.out.println("Total beans: " + beanNames.length);
        for (String beanName : beanNames) {
            System.out.println(
                    beanName + " : " + context.getBean(beanName).getClass().getName());
        }
        System.out.println("Total beans: " + beanNames.length);
        System.out.println("==============================\n");
        return Arrays.asList(beanNames);
    }
}
