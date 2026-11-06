package ru.cqa.api.execution;

import java.util.Map;
import java.util.UUID;
import ru.cqa.model.execution.MoveResult;

/** Состояние одного запуска. Реализацией и временем жизни владеет движок. */
public interface ExecutionContext {
    UUID executionId();
    UUID scenarioId();
    String environmentId();
    Map<String, Object> variables();
    Object variable(String name);
    void putVariable(String name, Object value);
    Map<String, MoveResult> results();
    MoveResult result(String moveId);
    void putResult(MoveResult result);
}
