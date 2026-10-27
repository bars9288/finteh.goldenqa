package ru.cqa.business.exceptions;

import java.util.List;

public final class GreetingValidationException extends ValidationException {

    public GreetingValidationException(List<String> errors) {
        super(errors);
    }
}
