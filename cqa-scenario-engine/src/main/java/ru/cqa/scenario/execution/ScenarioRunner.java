package ru.cqa.scenario.execution;

import ru.cqa.scenario.definition.model.Scenario;
import ru.cqa.scenario.result.ExecutionResult;

/** Запускает сценарий, переданный как модель. */
public interface ScenarioRunner {
    ExecutionResult execute(Scenario scenario, ExecutionContext context);
}
