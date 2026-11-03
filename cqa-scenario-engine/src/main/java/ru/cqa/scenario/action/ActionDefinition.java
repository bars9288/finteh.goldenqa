package ru.cqa.scenario.action;

import java.util.Objects;

/** Описание именованного действия, которое может добавлять плагин. */
public record ActionDefinition(
        String id,
        String version,
        String description) {

    public ActionDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        description = description == null ? "" : description;
    }
}
