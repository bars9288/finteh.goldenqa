package ru.cqa.server;

import org.apache.logging.slf4j.SLF4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;
import ru.cqa.server.web.echo.HelloClass;

@SpringBootApplication
public class CqaServerApplication {


    private static final Logger log = LoggerFactory.getLogger(CqaServerApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CqaServerApplication.class, args);
        {
            BuildProperties build = context.getBeanProvider(BuildProperties.class).getIfAvailable();
            String version = build != null ? build.getVersion() : null;
            log.info(HelloClass.getHello(version));
        }

    }
}
