package ru.cqa.server.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cqa.cases.saveAndMerge.SaveAndMergeValidator;

/**
 * Бины кейса saveAndMerge. Сам валидатор живёт в cqa-case без Spring —
 * здесь ему передаётся движок Bean Validation, который собрал Spring Boot.
 */
@Configuration
public class SaveAndMergeConfiguration {

    @Bean
    public SaveAndMergeValidator saveAndMergeValidator(Validator validator) {
        return new SaveAndMergeValidator(validator);
    }
}
