package ru.cqa.scenario.execution;

import java.util.Map;
import java.util.Objects;

/** Параметры старта; секреты подключений сюда не передаются. */
public record ExecutionParameters(String version, Map<String, Object> variables) {

    public ExecutionParameters {
        variables = Map.copyOf(Objects.requireNonNull(variables, "variables"));
    }
}
