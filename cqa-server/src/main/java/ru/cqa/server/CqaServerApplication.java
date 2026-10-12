package ru.cqa.server;

import org.apache.logging.slf4j.SLF4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.cqa.server.web.echo.HelloClass;

@SpringBootApplication
public class CqaServerApplication {



    private static final Logger log = LoggerFactory.getLogger(CqaServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CqaServerApplication.class, args);
        log.info(HelloClass.getHello("0.0.2"));
    }
}
