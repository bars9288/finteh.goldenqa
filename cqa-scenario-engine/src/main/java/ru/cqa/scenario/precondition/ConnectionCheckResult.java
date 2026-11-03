package ru.cqa.scenario.precondition;

import java.util.Objects;

/** Итог проверки одной зависимости сценария. */
public record ConnectionCheckResult(String dependencyId, PreconditionStatus status, String message) {

    public ConnectionCheckResult {
        Objects.requireNonNull(dependencyId, "dependencyId");
        Objects.requireNonNull(status, "status");
        message = message == null ? "" : message;
    }
}
