package ru.cqa.bootstrap.config;

import java.util.List;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import ru.cqa.api.greeting.GreetingFacade;
import ru.cqa.api.validation.FormValidator;
import ru.cqa.api.validation.Validator;
import ru.cqa.cases.greeting.DefaultGreetingFacade;
import ru.cqa.cases.greeting.GreetingRegistry;
import ru.cqa.cases.greeting.GreetingService;
import ru.cqa.cases.greeting.externalservice.ExternalServiceGreetingService;
import ru.cqa.cases.greeting.operator.OperatorGreetingService;
import ru.cqa.cases.mdm.validation.rules.greeting.GreetingValidationRules;
import ru.cqa.model.greeting.OperatorDTO;
import ru.cqa.validators.GreetingValidator;

@Configuration
public class GreetingConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreetingConfiguration.class);

    @Bean @Order(1)
    public GreetingService<?> operatorGreetingService() {
        return new OperatorGreetingService();
    }

    @Bean @Order(2)
    public GreetingService<?> externalServiceGreetingService() {
        return new ExternalServiceGreetingService();
    }

    @Bean
    public GreetingRegistry greetingRegistry(List<GreetingService<?>> greetingServices) {
        GreetingRegistry greetingRegistry = new GreetingRegistry(greetingServices);
        StringJoiner supportedTypeNames = new StringJoiner(", ");
        greetingRegistry.supportedTypes().forEach(type -> supportedTypeNames.add(type.getSimpleName()));
        LOGGER.info("Реестр приветствий собран, обслуживаемые типы: {}", supportedTypeNames);
        return greetingRegistry;
    }

    @Bean
    public GreetingFacade greetingFacade(GreetingRegistry greetingRegistry) {
        return new DefaultGreetingFacade(greetingRegistry);
    }

    @Bean
    public Validator<OperatorDTO> greetingValidator(FormValidator formValidator) {
        LOGGER.info("Инициализация правил и валидации ../hello");
        return new GreetingValidator(formValidator, new GreetingValidationRules().rules());
    }
}
