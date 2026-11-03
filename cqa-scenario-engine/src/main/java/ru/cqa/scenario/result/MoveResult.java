package ru.cqa.scenario.result;

import java.util.Objects;

/** Результат одной операции сценария. */
public record MoveResult(String moveId, MoveStatus status, Object response, String errorMessage, long executionTimeMs) {

    public MoveResult {
        Objects.requireNonNull(moveId, "moveId");
        Objects.requireNonNull(status, "status");
        if (executionTimeMs < 0) {
            throw new IllegalArgumentException("executionTimeMs не может быть отрицательным");
        }
    }
}
