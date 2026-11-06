package ru.cqa.scenario.legacy.action;

import java.util.Objects;

/** Описание именованного действия, которое может добавлять плагин. */
public record LegacyActionDefinition(
        String id,
        String version,
        String description) {

    public LegacyActionDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        description = description == null ? "" : description;
    }
}
