package ru.cqa.scenario.actionOLD;

import java.util.Optional;

/** Контракт регистрации и поиска деклараций действий. */
public interface ActionDefinitionRegistryOLD {
    void register(ActionDefinitionOLD definition);

    Optional<ActionDefinitionOLD> findById(String id);
}
