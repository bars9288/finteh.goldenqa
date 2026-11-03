package ru.cqa.scenario.validation;

import java.util.Objects;

/** Одна ошибка структуры или содержания сценария. */
public record ValidationIssue(String path, String code, String message) {

    public ValidationIssue {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }
}
