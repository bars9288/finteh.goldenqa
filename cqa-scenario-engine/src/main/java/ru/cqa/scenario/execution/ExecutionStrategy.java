package ru.cqa.scenario.execution;

import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.result.ExecutionResult;

/** Политика порядка выполнения шагов сценария. */
public interface ExecutionStrategy {
    ExecutionResult execute(Scenario scenario, ExecutionContext context);
}
