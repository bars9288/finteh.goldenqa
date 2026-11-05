package ru.cqa.scenario.runtime.execution;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import ru.cqa.scenario.runtime.result.MoveResult;

/** Изменяемое состояние одного прогона; создаётся отдельно для каждого executionId. */
public final class ExecutionContext {
    private final UUID executionId;
    private final UUID scenarioId;
    private final String environmentId;
    private final Map<String, Object> variables;
    private final Map<String, MoveResult> results;

    public ExecutionContext(UUID executionId, UUID scenarioId, Map<String, Object> variables) {
        this(executionId, scenarioId, null, variables);
    }

    public ExecutionContext(UUID executionId, UUID scenarioId, String environmentId, Map<String, Object> variables) {
        this.executionId = Objects.requireNonNull(executionId, "executionId");
        this.scenarioId = Objects.requireNonNull(scenarioId, "scenarioId");
        this.environmentId = environmentId;
        this.variables = new LinkedHashMap<>(Objects.requireNonNull(variables, "variables"));
        this.results = new LinkedHashMap<>();
    }

    public UUID executionId() {
        return executionId;
    }

    public UUID scenarioId() {
        return scenarioId;
    }

    /** Идентификатор окружения, выбранного для запуска; секреты здесь не хранятся. */
    public String environmentId() {
        return environmentId;
    }

    public Map<String, Object> variables() {
        return Collections.unmodifiableMap(variables);
    }

    public Object variable(String name) {
        return variables.get(name);
    }

    public void putVariable(String name, Object value) {
        variables.put(Objects.requireNonNull(name, "name"), value);
    }

    public Map<String, MoveResult> results() {
        return Collections.unmodifiableMap(results);
    }

    public MoveResult result(String moveId) {
        return results.get(moveId);
    }

    public void putResult(MoveResult result) {
        Objects.requireNonNull(result, "result");
        results.put(result.moveId(), result);
    }
}
