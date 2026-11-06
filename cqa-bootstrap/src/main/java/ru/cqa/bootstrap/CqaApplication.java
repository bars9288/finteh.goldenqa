package ru.cqa.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import ru.cqa.server.config.ServerConfiguration;

@SpringBootApplication
@Import(ServerConfiguration.class)
public class CqaApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(CqaApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CqaApplication.class, args);
        {
            BuildProperties build = context.getBeanProvider(BuildProperties.class).getIfAvailable();
            String version = build != null ? build.getVersion() : null;
            LOGGER.info(StartupBanner.text(version));
        }
    }
}
