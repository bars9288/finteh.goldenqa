package ru.cqa.scenario.preconditionOLD;

import java.util.List;
import java.util.Objects;

/** Сводка предварительной проверки всех зависимостей сценария. */
public record PreconditionResultOLD(PreconditionStatusOLD status, List<ConnectionCheckResultOLD> checks) {

    public PreconditionResultOLD {
        Objects.requireNonNull(status, "status");
        checks = List.copyOf(Objects.requireNonNull(checks, "checks"));
    }
}
