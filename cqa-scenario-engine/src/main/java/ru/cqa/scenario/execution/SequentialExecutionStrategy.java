package ru.cqa.scenario.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.definition.model.Step;
import ru.cqa.scenario.result.ExecutionResult;
import ru.cqa.scenario.result.ExecutionStatus;
import ru.cqa.scenario.result.StepResult;
import ru.cqa.scenario.result.StepStatus;

/** Вызывает шаги строго в порядке сценария и прекращает прогон по политике ошибок. */
public final class SequentialExecutionStrategy implements ExecutionStrategy {
    private final StepRunner stepRunner;

    public SequentialExecutionStrategy(StepRunner stepRunner) {
        this.stepRunner = Objects.requireNonNull(stepRunner, "stepRunner");
    }

    @Override
    public ExecutionResult execute(Scenario scenario, ExecutionContext context) {
        Objects.requireNonNull(scenario, "scenario");
        Objects.requireNonNull(context, "context");
        long startedAt = System.nanoTime();
        List<StepResult> results = new ArrayList<>();
        ExecutionStatus status = ExecutionStatus.COMPLETED;

        for (Step step : scenario.steps()) {
            StepResult result = stepRunner.execute(step, context);
            results.add(result);
            if (result.status() == StepStatus.ERROR) {
                status = ExecutionStatus.ERROR;
            } else if (result.status() == StepStatus.FAILED && status != ExecutionStatus.ERROR) {
                status = ExecutionStatus.FAILED;
            }

            boolean continueAfterFailure = step.continueOnError() || scenario.execution().continueAfterFailure();
            if (result.status() != StepStatus.COMPLETED && !continueAfterFailure) {
                break;
            }
        }

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        return new ExecutionResult(context.executionId(), context.scenarioId(), status, results, elapsedMs);
    }
}
