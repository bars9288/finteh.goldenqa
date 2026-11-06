package ru.cqa.scenario.runtime.execution;

import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.execution.ExecutionResult;
import ru.cqa.model.scenario.Scenario;

/** Политика порядка выполнения шагов сценария. */
public interface ExecutionStrategy {
    ExecutionResult execute(Scenario scenario, ExecutionContext context);
}
