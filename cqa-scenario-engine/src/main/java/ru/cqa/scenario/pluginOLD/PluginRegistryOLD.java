package ru.cqa.scenario.pluginOLD;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import ru.cqa.scenario.actionOLD.ActionDefinitionOLD;
import ru.cqa.scenario.actionOLD.ActionDefinitionRegistryOLD;
import ru.cqa.scenario.runtime.executor.MoveExecutor;
import ru.cqa.scenario.runtime.executor.MoveExecutorRegistry;

/** Ограниченная поверхность регистрации возможностей плагина. */
public final class PluginRegistryOLD implements ActionDefinitionRegistryOLD {
    private final MoveExecutorRegistry moveExecutors;
    private final Map<String, ActionDefinitionOLD> actions = new LinkedHashMap<>();

    public PluginRegistryOLD(MoveExecutorRegistry moveExecutors) {
        this.moveExecutors = Objects.requireNonNull(moveExecutors, "moveExecutors");
    }

    public void registerMoveExecutor(MoveExecutor<?> executor) {
        moveExecutors.register(executor);
    }

    @Override
    public void register(ActionDefinitionOLD definition) {
        Objects.requireNonNull(definition, "definition");
        ActionDefinitionOLD existing = actions.putIfAbsent(definition.id(), definition);
        if (existing != null) {
            throw new IllegalArgumentException("ActionDefinitionOLD уже зарегистрирован: " + definition.id());
        }
    }

    @Override
    public Optional<ActionDefinitionOLD> findById(String id) {
        return Optional.ofNullable(actions.get(Objects.requireNonNull(id, "id")));
    }

    public Collection<ActionDefinitionOLD> getActionDefinitions() {
        return java.util.List.copyOf(actions.values());
    }
}
