package ru.cqa.api.scenario;

import java.util.UUID;

/** Сценарий с таким идентификатором и версией уже сохранён. */
public final class ScenarioAlreadyExistsException extends RuntimeException {
    public ScenarioAlreadyExistsException(UUID scenarioId, String version) {
        super("Сценарий " + scenarioId + " версии " + version + " уже существует");
    }
}
