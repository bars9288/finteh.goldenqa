package ru.cqa.scenario.action;

import java.util.Optional;

/** Контракт регистрации и поиска деклараций действий. */
public interface ActionDefinitionRegistry {
    void register(ActionDefinition definition);

    Optional<ActionDefinition> findById(String id);
}
