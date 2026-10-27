package ru.cqa.server.config;

import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import ru.cqa.cases.greeting.externalservice.ExternalServiceGreetingService;
import ru.cqa.cases.greeting.GreetingRegistry;
import ru.cqa.cases.greeting.GreetingService;
import ru.cqa.cases.greeting.operator.OperatorGreetingService;
import ru.cqa.business.validation.rules.greeting.GreetingRules;
import ru.cqa.business.validation.validators.GreetingValidator;

@Configuration
public class GreetingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GreetingConfiguration.class);

    @Bean @Order(1)
    public GreetingService<?> operatorGreetingService() {
        return new OperatorGreetingService();
    }

    @Bean @Order(2)
    public GreetingService<?> externalServiceGreetingService() {
        return new ExternalServiceGreetingService();
    }

    @Bean
    public GreetingRegistry greetingRegistry(List<GreetingService<?>> services) {
        GreetingRegistry registry = new GreetingRegistry(services);
        StringJoiner types = new StringJoiner(", ");
        registry.supportedTypes().forEach(type -> types.add(type.getSimpleName()));
        logger.info("Реестр приветствий собран, обслуживаемые типы: {}", types);
        return registry;
    }

    @Bean
    public GreetingValidator greetingValidator(jakarta.validation.Validator validator) {
        logger.info("Инициализация правил и валидации ../hello");
        return new GreetingValidator(validator, new GreetingRules().ruleValues());
    }
}
