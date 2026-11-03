package ru.cqa.validation;

import jakarta.validation.ConstraintViolation;

import java.util.ArrayList;
import java.util.List;

/** Общая последовательность проверки Bean Validation и прикладных правил. */
public abstract class Validator<D, T, E extends ValidationException> {

    private final jakarta.validation.Validator validator;
    private final List<Rule<T>> rules;

    /** Вернуть null, если для запроса нет сущности, к которой применяются правила. */
    protected abstract T entity(D request);

    protected abstract E exception(List<String> errors);

    protected Validator(jakarta.validation.Validator validator, List<Rule<T>> rules) {
        this.validator = validator;
        this.rules = List.copyOf(rules);
    }

    protected final void checkForm(D request, List<String> errors) {
        validator.validate(request).stream()
                .map(Validator::describe)
                .sorted()
                .forEach(errors::add);
    }

    protected final void checkRules(T entity, List<String> errors) {
        rules.forEach(rule -> errors.addAll(rule.check(entity)));
    }

    public final void validateBody(D request) {
        List<String> errors = new ArrayList<>();
        checkForm(request, errors);
        T validatedEntity = entity(request);
        if (validatedEntity != null) {
            checkRules(validatedEntity, errors);
        }
        if (!errors.isEmpty()) {
            throw exception(errors);
        }
    }

    private static String describe(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString().replace(".<list element>", "");
        return path + ": " + violation.getMessage();
    }
}
