package ru.cqa.scenario.precondition;

import java.util.List;
import java.util.Objects;

/** Сводка предварительной проверки всех зависимостей сценария. */
public record PreconditionResult(PreconditionStatus status, List<ConnectionCheckResult> checks) {

    public PreconditionResult {
        Objects.requireNonNull(status, "status");
        checks = List.copyOf(Objects.requireNonNull(checks, "checks"));
    }
}
