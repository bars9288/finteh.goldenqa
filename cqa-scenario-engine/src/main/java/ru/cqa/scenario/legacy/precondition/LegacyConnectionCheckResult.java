package ru.cqa.scenario.legacy.precondition;

import java.util.Objects;

/** Итог проверки одной зависимости сценария. */
public record LegacyConnectionCheckResult(String dependencyId, LegacyPreconditionStatus status, String message) {

    public LegacyConnectionCheckResult {
        Objects.requireNonNull(dependencyId, "dependencyId");
        Objects.requireNonNull(status, "status");
        message = message == null ? "" : message;
    }
}
