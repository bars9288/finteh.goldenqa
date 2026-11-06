package ru.cqa.api.validation;

import java.util.List;

public final class GreetingValidatorException extends ValidationException {

    public GreetingValidatorException(List<String> errors) {
        super(errors);
    }
}
