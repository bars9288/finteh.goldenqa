package ru.cqa.scenario.precondition;

import ru.cqa.scenario.connection.ConnectionDefinition;
import ru.cqa.scenario.model.Dependency;

/** Проверяет доступность зависимости через предоставленное описание подключения. */
public interface ConnectionChecker {
    String getType();

    ConnectionCheckResult check(Dependency dependency, ConnectionDefinition connection);
}
