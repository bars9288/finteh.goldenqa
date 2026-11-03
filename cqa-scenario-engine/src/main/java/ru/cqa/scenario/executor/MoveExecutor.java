package ru.cqa.scenario.executor;

import ru.cqa.scenario.execution.ExecutionContext;
import ru.cqa.scenario.model.Move;
import ru.cqa.scenario.result.MoveResult;

/** Плагинная граница для транспорта или другого типа операции. */
public interface MoveExecutor<T extends Move> {
    String getType();

    Class<T> getSupportedType();

    MoveResult execute(T move, ExecutionContext context);
}
