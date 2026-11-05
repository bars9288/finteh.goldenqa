package ru.cqa.scenario.runtime.execution;

import ru.cqa.scenario.definition.model.Step;
import ru.cqa.scenario.runtime.result.StepResult;

/** Исполняет один шаг и собирает результаты его операций. */
public interface StepRunner {
    StepResult execute(Step step, ExecutionContext context);
}
