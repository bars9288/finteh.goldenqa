package ru.cqa.cases.validation;

import java.util.List;


public class SaveAndMergeValidationException extends RuntimeException {

    private final List<String> errors;

    public SaveAndMergeValidationException(List<String> errors) {
        super("Ошибка - " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
