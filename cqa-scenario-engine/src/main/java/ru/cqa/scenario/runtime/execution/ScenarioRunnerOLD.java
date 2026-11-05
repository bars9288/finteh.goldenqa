package ru.cqa.scenario.runtime.execution;

import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.runtime.result.ExecutionResult;

/** Запускает сценарий, переданный как модель. */
public interface ScenarioRunnerOLD {
    ExecutionResult execute(Scenario scenario, ExecutionContext context);
}
