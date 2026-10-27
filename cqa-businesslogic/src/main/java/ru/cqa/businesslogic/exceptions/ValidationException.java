package ru.cqa.businesslogic.exceptions;

import java.util.List;

// класс abstract, чтобы голый ValidationException нельзя было бросить.
public abstract class ValidationException extends RuntimeException {

    private final List<String> errors;

    protected ValidationException(List<String> errors) {
        super("Ошибка - " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
