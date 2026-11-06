package ru.cqa.scenario.legacy.action;

import java.util.Optional;

/** Контракт регистрации и поиска деклараций действий. */
public interface LegacyActionDefinitionRegistry {
    void register(LegacyActionDefinition definition);

    Optional<LegacyActionDefinition> findById(String id);
}
