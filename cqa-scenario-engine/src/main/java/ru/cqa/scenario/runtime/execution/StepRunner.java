package ru.cqa.scenario.runtime.execution;

import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.execution.StepResult;
import ru.cqa.model.scenario.Step;

/** Исполняет один шаг и собирает результаты его операций. */
public interface StepRunner {
    StepResult execute(Step step, ExecutionContext context);
}
