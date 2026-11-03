package ru.cqa.scenario.plugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ru.cqa.scenario.action.ActionDefinition;
import ru.cqa.scenario.action.ActionDefinitionRegistry;
import ru.cqa.scenario.executor.MoveExecutor;
import ru.cqa.scenario.executor.MoveExecutorRegistry;

/** Ограниченная поверхность регистрации возможностей плагина. */
public final class PluginRegistry implements ActionDefinitionRegistry {
    private final MoveExecutorRegistry moveExecutors;
    private final Map<String, ActionDefinition> actions = new LinkedHashMap<>();

    public PluginRegistry(MoveExecutorRegistry moveExecutors) {
        this.moveExecutors = Objects.requireNonNull(moveExecutors, "moveExecutors");
    }

    public void registerMoveExecutor(MoveExecutor<?> executor) {
        moveExecutors.register(executor);
    }

    @Override
    public void register(ActionDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        ActionDefinition existing = actions.putIfAbsent(definition.id(), definition);
        if (existing != null) {
            throw new IllegalArgumentException("ActionDefinition уже зарегистрирован: " + definition.id());
        }
    }

    @Override
    public Optional<ActionDefinition> findById(String id) {
        return Optional.ofNullable(actions.get(Objects.requireNonNull(id, "id")));
    }

    public Collection<ActionDefinition> getActionDefinitions() {
        return java.util.List.copyOf(actions.values());
    }
}
