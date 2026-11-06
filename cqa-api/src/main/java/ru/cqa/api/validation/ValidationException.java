package ru.cqa.api.validation;

import java.util.List;

/** Базовая ошибка проверки с неизменяемым списком сообщений. */
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
