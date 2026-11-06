package ru.cqa.scenario.legacy.precondition;

import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.model.scenario.Scenario;

/** Контракт проверки доступности обязательных зависимостей до начала прогона. */
public interface LegacyPreconditionEngine {
    LegacyPreconditionResult check(Scenario scenario, ExecutionContext context);
}
