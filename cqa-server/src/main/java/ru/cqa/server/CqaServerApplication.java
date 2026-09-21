package ru.cqa.server;

import org.apache.logging.slf4j.SLF4JLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CqaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CqaServerApplication.class, args);
    }
}
