package ru.cqa.scenario.runtime.execution;

import java.util.UUID;
import ru.cqa.scenario.runtime.result.ExecutionResult;

/** Публичный вход для запуска сохранённого сценария. */
public interface ScenarioEngine {
    ExecutionResult execute(UUID scenarioId, ExecutionParameters parameters);
}
