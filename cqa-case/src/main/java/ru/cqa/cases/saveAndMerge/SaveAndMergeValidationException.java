package ru.cqa.cases.saveAndMerge;

import java.util.List;

/**
 * Запрос saveAndMerge не прошёл проверку. Несёт сразу все найденные ошибки,
 * а не первую: клиент исправляет запрос за один заход.
 */
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
