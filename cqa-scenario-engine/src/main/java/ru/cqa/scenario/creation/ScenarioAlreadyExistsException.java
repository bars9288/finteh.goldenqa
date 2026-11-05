package ru.cqa.scenario.creation;

import java.util.UUID;

/** Сценарий с таким идентификатором и версией уже сохранён. */
public final class ScenarioAlreadyExistsException extends RuntimeException {
    public ScenarioAlreadyExistsException(UUID id, String version) {
        super("Сценарий " + id + " версии " + version + " уже существует");
    }
}
