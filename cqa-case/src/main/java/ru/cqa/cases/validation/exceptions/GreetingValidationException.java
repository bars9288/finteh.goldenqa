package ru.cqa.cases.validation.exceptions;

import java.util.List;

public final class GreetingValidationException extends ValidationException {

    public GreetingValidationException(List<String> errors) {
        super(errors);
    }
}
