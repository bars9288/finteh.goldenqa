package ru.cqa.scenario.runtime.execution;

import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.runtime.result.ExecutionResult;

/** Политика порядка выполнения шагов сценария. */
public interface ExecutionStrategy {
    ExecutionResult execute(Scenario scenario, ExecutionContext context);
}
