package ru.cqa.scenario.runtime.executor;

import java.util.Optional;
import ru.cqa.api.execution.MoveExecutor;

/** Реестр исполнителей операций, собранных из ядра и плагинов. */
public interface MoveExecutorRegistry {

    void register(MoveExecutor<?> executor);

    Optional<MoveExecutor<?>> findByType(String type);
}
