package ru.cqa.scenario.actionOLD;

import java.util.Objects;

/** Описание именованного действия, которое может добавлять плагин. */
public record ActionDefinitionOLD(
        String id,
        String version,
        String description) {

    public ActionDefinitionOLD {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        description = description == null ? "" : description;
    }
}
