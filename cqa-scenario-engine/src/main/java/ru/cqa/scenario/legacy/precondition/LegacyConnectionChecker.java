package ru.cqa.scenario.legacy.precondition;

import ru.cqa.model.scenario.Dependency;
import ru.cqa.scenario.connection.ConnectionDefinition;

/** Проверяет доступность зависимости через предоставленное описание подключения. */
public interface LegacyConnectionChecker {
    String getType();

    LegacyConnectionCheckResult check(Dependency dependency, ConnectionDefinition connection);
}
