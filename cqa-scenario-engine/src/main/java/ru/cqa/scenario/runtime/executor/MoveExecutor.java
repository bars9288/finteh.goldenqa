package ru.cqa.scenario.runtime.executor;

import ru.cqa.scenario.runtime.execution.ExecutionContext;
import ru.cqa.scenario.definition.model.Move;
import ru.cqa.scenario.runtime.result.MoveResult;

/** Плагинная граница для транспорта или другого типа операции. */
public interface MoveExecutor<T extends Move> {
    String getType();

    Class<T> getSupportedType();

    MoveResult execute(T move, ExecutionContext context);
}
