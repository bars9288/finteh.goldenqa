package ru.cqa.scenario.executor;

import java.util.Optional;

/** Реестр исполнителей операций, собранных из ядра и плагинов. */
public interface MoveExecutorRegistry {
    void register(MoveExecutor<?> executor);

    Optional<MoveExecutor<?>> findByType(String type);
}
