package ru.cqa.scenario.preconditionOLD;

import ru.cqa.scenario.connection.ConnectionDefinition;
import ru.cqa.scenario.definition.model.Dependency;

/** Проверяет доступность зависимости через предоставленное описание подключения. */
public interface ConnectionCheckerOLD {
    String getType();

    ConnectionCheckResultOLD check(Dependency dependency, ConnectionDefinition connection);
}
