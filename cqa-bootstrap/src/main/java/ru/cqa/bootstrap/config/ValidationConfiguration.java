package ru.cqa.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.api.validation.FormValidator;
import ru.cqa.validation.BeanFormValidator;

@Configuration
public class ValidationConfiguration {
    @Bean
    FormValidator formValidator(jakarta.validation.Validator validator) {
        return new BeanFormValidator(validator);
    }
}
