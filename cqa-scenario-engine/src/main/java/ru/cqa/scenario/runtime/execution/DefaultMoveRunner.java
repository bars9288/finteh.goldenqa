package ru.cqa.scenario.runtime.execution;

import java.util.Objects;
import ru.cqa.api.execution.ExecutionContext;
import ru.cqa.api.execution.MoveExecutor;
import ru.cqa.model.execution.MoveResult;
import ru.cqa.model.execution.MoveStatus;
import ru.cqa.model.scenario.Move;
import ru.cqa.scenario.runtime.executor.MoveExecutorRegistry;

/** Находит executor по типу Move и преобразует ошибку executor-а в MoveResult. */
public final class DefaultMoveRunner implements MoveRunner {
    private final MoveExecutorRegistry executors;

    public DefaultMoveRunner(MoveExecutorRegistry executors) {
        this.executors = executors;
    }

    @Override
    public MoveResult execute(Move move, ExecutionContext context) {
        Objects.requireNonNull(move, "move");
        Objects.requireNonNull(context, "context");

        MoveExecutor<?> executor = executors.findByType(move.getType()).orElse(null);
        if (executor == null) {
            return error(move, "Не зарегистрирован executor для Move типа: " + move.getType(), 0);
        }
        if (!executor.getSupportedType().isInstance(move)) {
            return error(move, "Executor типа " + move.getType() + " не поддерживает "
                    + move.getClass().getSimpleName(), 0);
        }

        long startedAt = System.nanoTime();
        try {
            MoveResult result = executeTyped(executor, move, context);
            if (result == null) {
                return error(move, "Executor вернул пустой MoveResult", elapsedMs(startedAt));
            }
            if (!move.getId().equals(result.moveId())) {
                return error(move, "Executor вернул результат для другого Move: " + result.moveId(),
                        elapsedMs(startedAt));
            }
            return result;
        } catch (RuntimeException exception) {
            String message = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage();
            return error(move, "Ошибка executor-а: " + message, elapsedMs(startedAt));
        }
    }

    private static <T extends Move> MoveResult executeTyped(MoveExecutor<T> executor, Move move, ExecutionContext context) {
        T typedMove = executor.getSupportedType().cast(move);
        return executor.execute(typedMove, context);
    }

    private static MoveResult error(Move move, String message, long elapsedMs) {
        return new MoveResult(move.getId(), MoveStatus.ERROR, null, message, elapsedMs);
    }

    private static long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
