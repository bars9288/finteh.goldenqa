package ru.cqa.api.scenario;

import java.util.UUID;
import ru.cqa.model.execution.ExecutionParameters;
import ru.cqa.model.execution.ExecutionResult;

/** Публичный вход для запуска сохранённого сценария. */
public interface ScenarioEngine {
    ExecutionResult execute(UUID scenarioId, ExecutionParameters parameters);
}
