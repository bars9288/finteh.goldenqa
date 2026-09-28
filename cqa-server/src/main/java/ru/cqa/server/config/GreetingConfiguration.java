package ru.cqa.server.config;

import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.business.greeting.externalservice.ExternalServiceGreetingService;
import ru.cqa.business.greeting.GreetingRegistry;
import ru.cqa.business.greeting.GreetingService;
import ru.cqa.business.greeting.operator.OperatorGreetingService;

/**
 * Превращает классы доменного модуля в бины.
 *
 * <p>Именно здесь держится правило «Spring заперт в cqa-server»: аннотации
 * стоят в этом модуле, а cqa-business остаётся обычным Java-кодом без
 * зависимости от фреймворка.
 *
 * <p>Точка роста: новая реализация приветствия — это новый {@code @Bean}-метод
 * рядом с существующими. Реестр собирается из списка автоматически, его самого
 * править не нужно.
 */
@Configuration
public class GreetingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GreetingConfiguration.class);

    @Bean
    public GreetingService<?> operatorGreetingService() {
        return new OperatorGreetingService();
    }

    @Bean
    public GreetingService<?> externalServiceGreetingService() {
        return new ExternalServiceGreetingService();
    }

    /**
     * Spring сам собирает сюда все бины типа {@link GreetingService} — список
     * не перечисляется руками, поэтому добавление реализации не требует правок.
     */
    @Bean
    public GreetingRegistry greetingRegistry(List<GreetingService<?>> services) {
        GreetingRegistry registry = new GreetingRegistry(services);
        StringJoiner types = new StringJoiner(", ");
        registry.supportedTypes().forEach(type -> types.add(type.getSimpleName()));
        log.info("Реестр приветствий собран, обслуживаемые типы: {}", types);
        return registry;
    }
}
