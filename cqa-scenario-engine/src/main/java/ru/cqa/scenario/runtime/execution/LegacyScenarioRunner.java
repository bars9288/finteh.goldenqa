package ru.cqa.scenario.runtime.execution;

import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.execution.ExecutionResult;
import ru.cqa.model.scenario.Scenario;

/** Запускает сценарий, переданный как модель. */
public interface LegacyScenarioRunner {
    ExecutionResult execute(Scenario scenario, ExecutionContext context);
}
