package ru.cqa.scenario.execution;

import ru.cqa.scenario.definition.model.Step;
import ru.cqa.scenario.result.StepResult;

/** Исполняет один шаг и собирает результаты его операций. */
public interface StepRunner {
    StepResult execute(Step step, ExecutionContext context);
}
