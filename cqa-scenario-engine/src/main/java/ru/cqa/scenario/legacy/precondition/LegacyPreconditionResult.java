package ru.cqa.scenario.legacy.precondition;

import java.util.List;
import java.util.Objects;

/** Сводка предварительной проверки всех зависимостей сценария. */
public record LegacyPreconditionResult(LegacyPreconditionStatus status, List<LegacyConnectionCheckResult> checks) {

    public LegacyPreconditionResult {
        Objects.requireNonNull(status, "status");
        checks = List.copyOf(Objects.requireNonNull(checks, "checks"));
    }
}
