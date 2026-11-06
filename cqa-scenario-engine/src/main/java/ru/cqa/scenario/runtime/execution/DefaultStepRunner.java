package ru.cqa.scenario.runtime.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.assertion.AssertionResult;
import ru.cqa.model.assertion.AssertionStatus;
import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.execution.MoveStatus;
import ru.cqa.model.execution.StepResult;
import ru.cqa.model.execution.StepStatus;
import ru.cqa.model.scenario.Move;
import ru.cqa.model.scenario.Step;
import ru.cqa.scenario.extract.ExtractEngine;
import ru.cqa.scenario.runtime.assertion.AssertionEngine;

/** Исполняет операции шага, проверяет ответы и сохраняет извлечённые переменные. */
public final class DefaultStepRunner implements StepRunner {
    private final MoveRunner moveRunner;
    private final AssertionEngine assertionEngine;
    private final ExtractEngine extractEngine;

    public DefaultStepRunner(MoveRunner moveRunner, AssertionEngine assertionEngine, ExtractEngine extractEngine) {
        this.moveRunner = Objects.requireNonNull(moveRunner, "moveRunner");
        this.assertionEngine = Objects.requireNonNull(assertionEngine, "assertionEngine");
        this.extractEngine = Objects.requireNonNull(extractEngine, "extractEngine");
    }

    @Override
    public StepResult execute(Step step, ExecutionContext context) {
        long startedAt = System.nanoTime();
        List<MoveResult> results = new ArrayList<>();
        StepStatus status = StepStatus.COMPLETED;

        for (Move move : step.moves()) {
            MoveResult result = moveRunner.execute(move, context);
            if (result.status() == MoveStatus.SUCCESS) {
                List<AssertionResult> assertions = assertionEngine.evaluate(
                        move.assertions() == null ? List.of() : move.assertions(), result, context);
                result = withAssertions(result, assertions);
                status = combine(status, assertionStatus(assertions));

                try {
                    Map<String, Object> extracted = extractEngine.extract(
                            move.extracts() == null ? List.of() : move.extracts(), result);
                    extracted.forEach(context::putVariable);
                } catch (RuntimeException exception) {
                    String message = exception.getMessage() == null
                            ? exception.getClass().getSimpleName()
                            : exception.getMessage();
                    result = new MoveResult(result.moveId(), MoveStatus.ERROR, result.response(),
                            "Ошибка Extract: " + message, result.executionTimeMs(), assertions);
                    status = combine(status, StepStatus.ERROR);
                }
            } else if (result.status() == MoveStatus.ERROR || result.status() == MoveStatus.TIMEOUT) {
                status = combine(status, StepStatus.ERROR);
            } else if (result.status() == MoveStatus.FAILED) {
                status = combine(status, StepStatus.FAILED);
            }

            context.putResult(result);
            results.add(result);
        }

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        return new StepResult(step.id(), status, results, elapsedMs);
    }

    private static MoveResult withAssertions(MoveResult result, List<AssertionResult> assertions) {
        return new MoveResult(result.moveId(), result.status(), result.response(), result.errorMessage(),
                result.executionTimeMs(), assertions);
    }

    private static StepStatus assertionStatus(List<AssertionResult> assertions) {
        if (assertions.stream().anyMatch(result -> result.status() == AssertionStatus.ERROR)) {
            return StepStatus.ERROR;
        }
        if (assertions.stream().anyMatch(result -> result.status() == AssertionStatus.FAILED)) {
            return StepStatus.FAILED;
        }
        return StepStatus.COMPLETED;
    }

    private static StepStatus combine(StepStatus current, StepStatus next) {
        if (current == StepStatus.ERROR || next == StepStatus.ERROR) {
            return StepStatus.ERROR;
        }
        if (current == StepStatus.FAILED || next == StepStatus.FAILED) {
            return StepStatus.FAILED;
        }
        return StepStatus.COMPLETED;
    }
}
