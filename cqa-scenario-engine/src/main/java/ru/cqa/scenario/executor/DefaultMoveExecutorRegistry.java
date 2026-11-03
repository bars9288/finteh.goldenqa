package ru.cqa.scenario.executor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Базовый реестр исполнителей, допускающий регистрацию встроенных и плагинных реализаций. */
public final class DefaultMoveExecutorRegistry implements MoveExecutorRegistry {
    private final Map<String, MoveExecutor<?>> executors = new LinkedHashMap<>();

    public DefaultMoveExecutorRegistry(Collection<? extends MoveExecutor<?>> builtInExecutors) {
        Objects.requireNonNull(builtInExecutors, "builtInExecutors").forEach(this::register);
    }

    @Override
    public void register(MoveExecutor<?> executor) {
        Objects.requireNonNull(executor, "executor");
        String type = Objects.requireNonNull(executor.getType(), "executor type");
        if (type.isBlank()) {
            throw new IllegalArgumentException("Тип исполнителя не должен быть пустым");
        }
        MoveExecutor<?> existing = executors.putIfAbsent(type, executor);
        if (existing != null) {
            throw new IllegalArgumentException("Исполнитель для типа уже зарегистрирован: " + type);
        }
    }

    @Override
    public Optional<MoveExecutor<?>> findByType(String type) {
        return Optional.ofNullable(executors.get(Objects.requireNonNull(type, "type")));
    }
}
