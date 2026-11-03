package ru.cqa.scenario.result;

import java.util.List;
import java.util.Objects;

/** Результаты операций и общий статус одного шага. */
public record StepResult(String stepId, StepStatus status, List<MoveResult> moves, long executionTimeMs) {

    public StepResult {
        Objects.requireNonNull(stepId, "stepId");
        Objects.requireNonNull(status, "status");
        moves = List.copyOf(Objects.requireNonNull(moves, "moves"));
        if (executionTimeMs < 0) {
            throw new IllegalArgumentException("executionTimeMs не может быть отрицательным");
        }
    }
}
