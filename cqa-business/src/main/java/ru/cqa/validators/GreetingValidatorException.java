package ru.cqa.validators;

import ru.cqa.validation.ValidationException;

import java.util.List;

public final class GreetingValidatorException extends ValidationException {

    public GreetingValidatorException(List<String> errors) {
        super(errors);
    }
}
