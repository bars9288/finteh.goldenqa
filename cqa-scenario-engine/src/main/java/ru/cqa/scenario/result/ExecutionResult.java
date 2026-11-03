package ru.cqa.scenario.result;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Итог выполнения сценария с результатами всех завершённых шагов. */
public record ExecutionResult(
        UUID executionId,
        UUID scenarioId,
        ExecutionStatus status,
        List<StepResult> steps,
        long executionTimeMs) {

    public ExecutionResult {
        Objects.requireNonNull(executionId, "executionId");
        Objects.requireNonNull(scenarioId, "scenarioId");
        Objects.requireNonNull(status, "status");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
        if (executionTimeMs < 0) {
            throw new IllegalArgumentException("executionTimeMs не может быть отрицательным");
        }
    }
}
