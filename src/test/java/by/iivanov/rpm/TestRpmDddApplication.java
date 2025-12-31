package by.iivanov.rpm;

import org.springframework.boot.SpringApplication;

public class TestRpmDddApplication {

    static void main(String[] args) {
        SpringApplication.from(RpmDddApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
