package ru.cqa.api.execution;

import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.scenario.Move;

/** Плагинная граница для транспорта или другого типа операции. */
public interface MoveExecutor<T extends Move> {
    String getType();

    Class<T> getSupportedType();

    MoveResult execute(T move, ExecutionContext context);
}
