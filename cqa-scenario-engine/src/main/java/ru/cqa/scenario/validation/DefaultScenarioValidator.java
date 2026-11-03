package ru.cqa.scenario.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import ru.cqa.scenario.model.Scenario;

/** Bean Validation реализация проверки структуры сценария. */
public final class DefaultScenarioValidator implements ScenarioValidator {

    private final Validator validator;

    public DefaultScenarioValidator(Validator validator) {
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    @Override
    public List<ValidationIssue> validate(Scenario scenario) {
        Objects.requireNonNull(scenario, "scenario");
        return validator.validate(scenario).stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                .map(this::toIssue)
                .toList();
    }

    private ValidationIssue toIssue(ConstraintViolation<Scenario> violation) {
        return new ValidationIssue(
                violation.getPropertyPath().toString(),
                violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                violation.getMessage());
    }
}
