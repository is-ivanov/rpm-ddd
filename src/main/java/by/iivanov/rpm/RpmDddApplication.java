package by.iivanov.rpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RpmDddApplication {

    static void main(String[] args) {
        SpringApplication.run(RpmDddApplication.class, args);
    }
}
