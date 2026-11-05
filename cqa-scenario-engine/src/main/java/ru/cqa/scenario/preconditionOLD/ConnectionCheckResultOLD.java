package ru.cqa.scenario.preconditionOLD;

import java.util.Objects;

/** Итог проверки одной зависимости сценария. */
public record ConnectionCheckResultOLD(String dependencyId, PreconditionStatusOLD status, String message) {

    public ConnectionCheckResultOLD {
        Objects.requireNonNull(dependencyId, "dependencyId");
        Objects.requireNonNull(status, "status");
        message = message == null ? "" : message;
    }
}
