package ru.cqa.validation;

import jakarta.validation.ConstraintViolation;
import java.util.List;
import java.util.Objects;
import ru.cqa.api.validation.FormValidator;

/** Адаптер Jakarta Bean Validation к общему контракту проверки формы. */
public final class BeanFormValidator implements FormValidator {
    private final jakarta.validation.Validator validator;

    public BeanFormValidator(jakarta.validation.Validator validator) {
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    @Override
    public List<String> validate(Object model) {
        return validator.validate(model).stream().map(BeanFormValidator::describe).sorted().toList();
    }

    private static String describe(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString().replace(".<list element>", "");
        return path + ": " + violation.getMessage();
    }
}
