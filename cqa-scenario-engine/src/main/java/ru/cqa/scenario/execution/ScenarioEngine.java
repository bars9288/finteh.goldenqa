package ru.cqa.scenario.execution;

import java.util.UUID;
import ru.cqa.scenario.result.ExecutionResult;

/** Публичный вход для запуска сохранённого сценария. */
public interface ScenarioEngine {
    ExecutionResult execute(UUID scenarioId, ExecutionParameters parameters);
}
