package ru.cqa.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.business.greeting.GreetingService;
import ru.cqa.business.greeting.StandardGreetingService;

/**
 * Превращает классы доменного модуля в бины.
 *
 * <p>Именно здесь держится правило «Spring заперт в cqa-server»: аннотации
 * стоят в этом модуле, а cqa-business остаётся обычным Java-кодом без
 * зависимости от фреймворка.
 */
@Configuration
public class GreetingConfiguration {

    @Bean
    public GreetingService greetingService() {
        return new StandardGreetingService();
    }
}
