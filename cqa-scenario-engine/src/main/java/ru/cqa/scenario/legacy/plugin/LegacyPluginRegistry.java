package ru.cqa.scenario.legacy.plugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ru.cqa.api.execution.MoveExecutor;
import ru.cqa.scenario.legacy.action.LegacyActionDefinition;
import ru.cqa.scenario.legacy.action.LegacyActionDefinitionRegistry;
import ru.cqa.scenario.runtime.executor.MoveExecutorRegistry;

/** Ограниченная поверхность регистрации возможностей плагина. */
public final class LegacyPluginRegistry implements LegacyActionDefinitionRegistry {
    private final MoveExecutorRegistry moveExecutors;
    private final Map<String, LegacyActionDefinition> actions = new LinkedHashMap<>();

    public LegacyPluginRegistry(MoveExecutorRegistry moveExecutors) {
        this.moveExecutors = Objects.requireNonNull(moveExecutors, "moveExecutors");
    }

    public void registerMoveExecutor(MoveExecutor<?> executor) {
        moveExecutors.register(executor);
    }

    @Override
    public void register(LegacyActionDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        LegacyActionDefinition existing = actions.putIfAbsent(definition.id(), definition);
        if (existing != null) {
            throw new IllegalArgumentException("ActionDefinitionOLD уже зарегистрирован: " + definition.id());
        }
    }

    @Override
    public Optional<LegacyActionDefinition> findById(String id) {
        return Optional.ofNullable(actions.get(Objects.requireNonNull(id, "id")));
    }

    public Collection<LegacyActionDefinition> getActionDefinitions() {
        return java.util.List.copyOf(actions.values());
    }
}
