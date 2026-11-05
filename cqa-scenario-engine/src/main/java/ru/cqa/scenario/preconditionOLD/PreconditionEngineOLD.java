package ru.cqa.scenario.preconditionOLD;

import ru.cqa.scenario.runtime.execution.ExecutionContext;
import ru.cqa.scenario.definition.model.Scenario;

/** Контракт проверки доступности обязательных зависимостей до начала прогона. */
public interface PreconditionEngineOLD {
    PreconditionResultOLD check(Scenario scenario, ExecutionContext context);
}
