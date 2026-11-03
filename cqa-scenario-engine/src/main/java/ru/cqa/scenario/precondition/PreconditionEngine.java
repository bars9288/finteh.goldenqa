package ru.cqa.scenario.precondition;

import ru.cqa.scenario.execution.ExecutionContext;
import ru.cqa.scenario.model.Scenario;

/** Контракт проверки доступности обязательных зависимостей до начала прогона. */
public interface PreconditionEngine {
    PreconditionResult check(Scenario scenario, ExecutionContext context);
}
